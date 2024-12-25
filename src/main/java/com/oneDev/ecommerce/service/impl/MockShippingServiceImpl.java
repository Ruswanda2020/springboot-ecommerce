package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.entity.OrdersItems;
import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.request.ShippingOrderRequest;
import com.oneDev.ecommerce.model.request.ShippingRateRequest;
import com.oneDev.ecommerce.model.response.ShippingOrderResponse;
import com.oneDev.ecommerce.model.response.ShippingRateResponse;
import com.oneDev.ecommerce.repository.OrderItemRepository;
import com.oneDev.ecommerce.repository.OrderRepository;
import com.oneDev.ecommerce.repository.ProductRepository;
import com.oneDev.ecommerce.service.ShippingService;
import com.oneDev.ecommerce.utils.OrderStateTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockShippingServiceImpl implements ShippingService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    private static final BigDecimal RATE_PER_KG = BigDecimal.valueOf(2500);
    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(10000);

    @Override
    public ShippingRateResponse calculateShippingRate(ShippingRateRequest shippingRateRequest) {
        //shipping fee = base_rate + (weight * rate per kg)
        BigDecimal shippingRate = BASE_RATE
                .add(shippingRateRequest.getTotalWeightInGrams()
                        .divide(BigDecimal.valueOf(1000))
                        .multiply(RATE_PER_KG))
                .setScale(2, RoundingMode.HALF_UP);

        String estimateDeliveryTime = "3 - 5 hari kerja";
        return ShippingRateResponse.builder()
                .shippingFee(shippingRate)
                .estimateDeliveryTime(estimateDeliveryTime)
                .build();
    }

    @Override
    public ShippingOrderResponse createShippingOrder(ShippingOrderRequest shippingOrderRequest) {

        String awbNumber = generateAwbNumber(shippingOrderRequest.getOrderId());

        Order order = orderRepository.findById(shippingOrderRequest.getOrderId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Order Id  not found"));

        if (!OrderStateTransaction.isValidTransaction(order.getStatus(), OrderStatus.SHIPPED)) {
            throw new IllegalStateException(
                    "Invalid order status transition from " + order.getStatus() + " to SHIPPED"
            );
        }
        order.setStatus(OrderStatus.SHIPPED);
        order.setAwbNumber(awbNumber);
        orderRepository.save(order);
        String estimateDeliveryTime = "3 - 5 hari kerja";

        return ShippingOrderResponse.builder()
                .awbNumber(awbNumber)
                .estimatedDeliveryTime(estimateDeliveryTime)
                .build();
    }

    @Override
    public String generateAwbNumber(Long orderId) {
        Random random = new Random();
        String prefix = "AWB";
        return String.format("%s%11d", prefix, random.nextInt(1000000000));
    }

    @Override
    public BigDecimal calculateTotalWeight(Long orderId) {
        List<OrdersItems> ordersItems = orderItemRepository.findByOrderId(orderId);

        if (ordersItems.isEmpty()) {
            throw new ApplicationException(
                    ExceptionType.RESOURCE_NOT_FOUND, "No items found for order with id: " + orderId);
        }

        // Ambil semua productId dari ordersItems untuk query batch.
        List<Long> productIds = ordersItems
                .stream()
                .map(OrdersItems::getProductId)
                .toList();

        // Ambil semua produk dalam satu query.
        List<Product> products = productRepository.findAllById(productIds);

        // Map productId ke entity Product untuk akses cepat.
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        return ordersItems.stream()
                .map(item -> Optional.ofNullable(productMap.get(item.getProductId()))
                            .map(product -> product.getWeight().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .orElseThrow(() -> new ApplicationException(
                                    ExceptionType.RESOURCE_NOT_FOUND,
                                    "Product not found with id: " + item.getProductId()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
