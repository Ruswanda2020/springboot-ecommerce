package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.*;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.request.ShippingRateRequest;
import com.oneDev.ecommerce.model.response.OrderItemResponse;
import com.oneDev.ecommerce.model.response.ShippingRateResponse;
import com.oneDev.ecommerce.repository.*;
import com.oneDev.ecommerce.service.OrderService;
import com.oneDev.ecommerce.service.ShippingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class OrderServiceImpl implements OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserAddressesRepository userAddressesRepository;
    private final ProductRepository productRepository;
    private final ShippingService shippingService;

    private final BigDecimal Tax_RATE = BigDecimal.valueOf(0.03);

    @Override
    @Transactional
    public Order checkOut(CheckOutRequest checkOutRequest) {

        // Ambil item di keranjang berdasarkan ID yang dipilih pengguna.
        List<CartItem> cartItems = cartItemRepository
                .findAllById(checkOutRequest.getSelectedCartItemIds());

        // Jika item keranjang yang dipilih kosong, lempar exception bahwa data tidak ditemukan.
        if (cartItems.isEmpty()) {
            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                    "Cart items not found for the given IDs.");
        }

        // Cari alamat pengiriman pengguna berdasarkan userId dalam request.
        UserAddresses userShippingAddress = userAddressesRepository.findById(checkOutRequest.getUserId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User address not found for the given user ID."));

        // Jika request valid, buat objek Order baru
        Order newOrder = Order.builder()
                .userId(checkOutRequest.getUserId())
                .status("PENDING")
                .orderDate(LocalDateTime.now())
                .taxFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .build();

        // Simpan data pesanan ke database dan dapatkan objek pesanan yang sudah disimpan.
        Order savedOrder = orderRepository.save(newOrder);

        // Buat daftar `OrdersItems` dari item keranjang yang dipilih.
        List<OrdersItems> orderItems = cartItems.stream()
                .map(cartItem -> {
                    // Untuk setiap item di keranjang, buat item pesanan.
                    return OrdersItems.builder()
                            .orderId(savedOrder.getOrderId())
                            .productId(cartItem.getProductId())
                            .quantity(cartItem.getQuantity())
                            .userAddressId(userShippingAddress.getUserId())
                            .build();
                }).toList();

        // Simpan semua item pesanan ke database.
        orderItemRepository.saveAll(orderItems);

        // Hapus item yang sudah dipesan dari keranjang pengguna.
        cartItemRepository.deleteAll(cartItems);

        // Hitung total pesanan berdasarkan harga dan kuantitas setiap item.
        BigDecimal subTotal = orderItems.stream()
                .map(orderItem ->
                        // Harga per item dikalikan kuantitas, kemudian diakumulasikan.
                        orderItem.getPrice()
                                .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                ).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Hitung biaya pengiriman dari setiap item pesanan.
        BigDecimal shippingFee = orderItems.stream()
                .map(orderItem -> {
                    // Cari data produk berdasarkan productId.
                    Optional<Product> product = productRepository.findById(orderItem.getProductId());
                    if (product.isEmpty()) {
                        return BigDecimal.ZERO;
                    }

                    // Cari alamat penjual produk.
                    Optional<UserAddresses> sellerAddresses = userAddressesRepository.findByUserIdAndIsDefaultTrue(
                            product.get().getUserId()
                    );

                    if (sellerAddresses.isEmpty()) {
                        return BigDecimal.ZERO;
                    }

                    // Hitung total berat barang berdasarkan jumlah item.
                    BigDecimal totalWeight = product.get().getWeight()
                            .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

                    // Buat request untuk menghitung biaya pengiriman.
                    ShippingRateRequest shippingRateRequest = ShippingRateRequest.builder()
                            .fromAddress(ShippingRateRequest.from(sellerAddresses.get()))
                            .toAddress(ShippingRateRequest.from(userShippingAddress))
                            .totalWeightInGrams(totalWeight)
                            .build();

                    // Hitung biaya pengiriman menggunakan layanan eksternal.
                    ShippingRateResponse shippingRateResponse = shippingService.calculateShippingRate(shippingRateRequest);
                    return shippingRateResponse.getShippingFee();
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Hitung pajak dari subtotal.
        BigDecimal taxFee = subTotal.multiply(Tax_RATE);

        // Hitung total pesanan (subtotal + pajak + biaya pengiriman).
        BigDecimal totalAmount = subTotal.add(taxFee).add(shippingFee);

        // Perbarui total pesanan di objek Order.
        savedOrder.setSubtotal(subTotal);
        savedOrder.setShippingFee(shippingFee);
        savedOrder.setTotalAmount(totalAmount);

        // Simpan pesanan yang diperbarui dan kembalikan hasilnya.
        return orderRepository.save(savedOrder);
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
    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Override @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Order with id " + orderId + " not found."));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be cancelled.");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
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


    @Override
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Order with id " + orderId + " not found."));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be cancelled.");
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public Double calculateTotalPrice(Long orderId) {
        return orderItemRepository.calculateTotalOrder(orderId);
    }
}
