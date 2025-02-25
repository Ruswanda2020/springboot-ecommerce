package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.*;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.request.ShippingRateRequest;
import com.oneDev.ecommerce.model.response.*;
import com.oneDev.ecommerce.repository.*;
import com.oneDev.ecommerce.service.InventoryService;
import com.oneDev.ecommerce.service.OrderService;
import com.oneDev.ecommerce.service.PaymentService;
import com.oneDev.ecommerce.service.ShippingService;
import com.oneDev.ecommerce.utils.OrderStateTransaction;
import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserAddressesRepository userAddressesRepository;
    private final ProductRepository productRepository;
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    private final BigDecimal Tax_RATE = BigDecimal.valueOf(0.03);

    @Override
    @Transactional
    public OrderResponse checkOut(CheckOutRequest checkOutRequest) {
        // 1. Ambil item di keranjang berdasarkan ID yang dipilih pengguna.
        List<CartItem> selecttedItems = cartItemRepository
                .findAllById(checkOutRequest.getSelectedCartItemIds());
        if (selecttedItems.isEmpty()) {
            // Jika item kosong, lempar exception.
            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Cart items not found for the given IDs.");
        }

        // 2. Ambil alamat pengiriman pengguna berdasarkan ID.
        UserAddresses userShippingAddress = userAddressesRepository
                .findById(checkOutRequest.getUserAddressId())
                .orElseThrow(
                        () -> new ApplicationException(
                                ExceptionType.RESOURCE_NOT_FOUND, "Shipping address with id: "+ checkOutRequest.getUserAddressId()+ "is not found"));

        Map<Long, Integer> productQuantities = selecttedItems.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));
        if(!inventoryService.checkAndLockInventory(productQuantities)){
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Insufficient inventory for one more products");
        }

        // 3. Buat objek Order baru dengan nilai default.
        Order newOrder = Order.builder()
                .userId(checkOutRequest.getUserId())
                .status(OrderStatus.PENDING) // Status default: PENDING
                .orderDate(LocalDateTime.now()) // Tanggal pesanan dibuat
                .taxFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .build();

        // 4. Simpan order ke database.
        Order savedOrder = orderRepository.save(newOrder);

        // 5. Konversi item keranjang ke orderItems dan simpan ke database.
        List<OrdersItems> orderItems = selecttedItems.stream()
                .map(cartItem -> OrdersItems.builder()
                        .orderId(savedOrder.getOrderId()) // ID order yang baru dibuat
                        .productId(cartItem.getProductId())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .userAddressId(userShippingAddress.getUserAddressId())
                        .build())
                .toList();
        orderItemRepository.saveAll(orderItems);

        // 6. Hapus item keranjang yang sudah diproses.
        cartItemRepository.deleteAll(selecttedItems);

        // 7. Hitung subtotal berdasarkan harga * kuantitas setiap item.
        BigDecimal subTotal = orderItems.stream()
                .map(orderItem -> orderItem.getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 8. Hitung total berat produk untuk biaya pengiriman.
        BigDecimal totalWeight = orderItems.stream()
                .map(orderItem -> {
                    Product product = productRepository.findById(orderItem.getProductId())
                            .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Product not found."));
                    if (product.getWeight() == null) {
                        throw new ApplicationException(ExceptionType.BAD_REQUEST, "Product weight cannot be null");
                    }
                    return product.getWeight().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 9. Buat request biaya pengiriman berdasarkan total berat.
        ShippingRateRequest shippingRateRequest = ShippingRateRequest.builder()
                .totalWeightInGrams(totalWeight)
                .fromAddress(ShippingRateRequest.from(userShippingAddress))
                .toAddress(ShippingRateRequest.from(userShippingAddress))
                .build();

        // 10. Hitung biaya pengiriman menggunakan layanan shippingService.
        BigDecimal shippingFee = shippingService.calculateShippingRate(shippingRateRequest).getShippingFee();

        // 11. Hitung pajak (TAX_RATE * subtotal).
        BigDecimal taxFee = subTotal.multiply(Tax_RATE);

        // 12. Hitung total pesanan (subtotal + pajak + biaya pengiriman).
        BigDecimal totalAmount = subTotal.add(taxFee).add(shippingFee);

        // 13. Perbarui nilai subtotal, shippingFee, taxFee, dan totalAmount di order.
        savedOrder.setSubtotal(subTotal);
        savedOrder.setShippingFee(shippingFee);
        savedOrder.setTaxFee(taxFee);
        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        //interact wit xendit api
        //generate payment url
        String paymentUrl;
        try {
            PaymentResponse paymentResponse = paymentService.create(savedOrder);
            savedOrder.setXenditInvoiceId(paymentResponse.getXenditInvoiceId());
            savedOrder.setXenditPaymentStatus(paymentResponse.getXenditInvoiceStatus());
            paymentUrl = paymentResponse.getXenditPaymentUrl();

            orderRepository.save(savedOrder);
            inventoryService.decreaseProductQuantity(productQuantities);
        } catch (Exception e) {
            log.error("Payment creation for order {} failed Reason{}", savedOrder.getOrderId(), e.getMessage());
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(savedOrder);
             return OrderResponse.from(savedOrder);
        }

        OrderResponse orderResponse = OrderResponse.from(savedOrder);
        orderResponse.setXenditPaymentUrl(paymentUrl);

        return orderResponse;
    }


    @Override
    public Optional<Order> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Page<OrderResponse> findByUserIdAndPageable(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdByPageable(userId, pageable)
                .map(OrderResponse::from);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Order with id " + orderId + " not found."));

        if (!OrderStateTransaction.isValidTransaction(order.getStatus(), OrderStatus.CANCELLED)){
            throw new IllegalStateException("Only PENDING orders can be cancelled.");
        }

        List<OrdersItems> orderItemList = orderItemRepository.findByOrderId(orderId);
        Map<Long, Integer> productQuantities = orderItemList.stream()
                        .collect(Collectors.toMap(OrdersItems::getProductId, OrdersItems::getQuantity));

        order.setStatus(OrderStatus.CANCELLED );
        orderRepository.save(order);
        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            cancelXenditInvoice(order);
            inventoryService.increaseProductQuantity(productQuantities);
        }

    }

    @Override
    public List<OrderItemResponse> findOrderItemByOrderId(Long orderId) {

        // Ambil item pesanan berdasarkan ID pesanan.
        List<OrdersItems> orderItemList = orderItemRepository.findByOrderId(orderId);

        // Jika tidak ada item pesanan ditemukan, kembalikan list kosong.
        if (orderItemList.isEmpty()) {
            return Collections.emptyList();
        }

        // Ekstrak ID produk dari daftar item pesanan.
        List<Long> productIdList = orderItemList.stream()
                .map(OrdersItems::getProductId)
                .toList();

        // Ekstrak ID alamat pengiriman pengguna dari daftar item pesanan.
        List<Long> userAddressIdList = orderItemList.stream()
                .map(OrdersItems::getUserAddressId)
                .toList();

        // Ambil detail produk berdasarkan ID produk yang diekstrak.
        List<Product> productList = productRepository.findAllById(productIdList);

        // Ambil detail alamat pengguna berdasarkan ID alamat yang diekstrak.
        List<UserAddresses> userAddressList = userAddressesRepository.findAllById(userAddressIdList);

        // Buat peta produk untuk mempermudah pencarian detail berdasarkan ID produk.
        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // Buat peta alamat pengguna untuk mempermudah pencarian detail berdasarkan ID alamat.
        Map<Long, UserAddresses> userAddressMap = userAddressList.stream()
                .collect(Collectors.toMap(UserAddresses::getUserId, Function.identity()));

        // Transformasi item pesanan menjadi respons DTO dengan detail lengkap.
        return orderItemList.stream()
                .map(orderItem -> {
                    // Ambil detail produk dari peta produk.
                    Product productDetails = productMap.get(orderItem.getProductId());

                    // Ambil detail alamat pengguna dari peta alamat pengguna.
                    UserAddresses userAddressDetails = userAddressMap.get(orderItem.getUserAddressId());

                    // Lempar exception jika detail produk tidak ditemukan.
                    if (productDetails == null) {
                        throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Product not found for the given ID.");
                    }

                    // Lempar exception jika detail alamat pengguna tidak ditemukan.
                    if (userAddressDetails == null) {
                        throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "User address not found for the given ID.");
                    }

                    // Kembalikan respons DTO dengan detail produk dan alamat pengguna.
                    return OrderItemResponse.from(orderItem, userAddressDetails, productDetails);
                })
                .toList();
    }


    @Override @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Order with id " + orderId + " not found."));

        if (!OrderStateTransaction.isValidTransaction(order.getStatus(), newStatus)){
            throw new IllegalStateException("Order with current status " + order.getStatus()
                    + "can not be updated into " + newStatus);
        }
        order.setStatus(newStatus);
        orderRepository.save(order);

        List<OrdersItems> orderItemList = orderItemRepository.findByOrderId(orderId);
        Map<Long, Integer> productQuantities = orderItemList.stream()
                .collect(Collectors.toMap(OrdersItems::getProductId, OrdersItems::getQuantity));
        inventoryService.increaseProductQuantity(productQuantities);

        if (newStatus.equals(OrderStatus.CANCELLED)) {
            cancelXenditInvoice(order);
        }
    }

    @Override
    public Double calculateTotalPrice(Long orderId) {
        return orderItemRepository.calculateTotalOrder(orderId);
    }

    @Override
    public PaginatedOrderResponse convertToOrderPage(Page<OrderResponse> orderResponses) {
        return PaginatedOrderResponse.builder()
                .data(orderResponses.getContent())
                .pageNo(orderResponses.getNumber())
                .pageSize(orderResponses.getSize())
                .totalElements(orderResponses.getTotalElements())
                .totalPages(orderResponses.getTotalPages())
                .last(orderResponses.isLast())
                .build();
    }


    private void cancelXenditInvoice(Order order) {
        try {
            Invoice invoice = Invoice.expire(order.getXenditInvoiceId());
            order.setXenditPaymentStatus(invoice.getStatus());
            orderRepository.save(order);
        } catch (XenditException e) {
            log.error("Error while request invoice cancellation for order with xendit id : {}", order.getOrderId());
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    @Transactional
    public void cancelUnpaidOrder() {
        LocalDateTime cancelThreshold = LocalDateTime.now().minusDays(1);
        List<Order> unpaidOrders = orderRepository.findByStatusAndOrderDateBefore(OrderStatus.PENDING,
                cancelThreshold);

        for (Order order : unpaidOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            cancelXenditInvoice(order);
        }
    }

}
