package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.request.ShippingOrderRequest;
import com.oneDev.ecommerce.model.request.ShippingRateRequest;
import com.oneDev.ecommerce.model.response.ShippingOrderResponse;
import com.oneDev.ecommerce.model.response.ShippingRateResponse;

import java.math.BigDecimal;

public interface ShippingService {

    ShippingRateResponse calculateShippingRate(ShippingRateRequest shippingRateRequest);
    ShippingOrderResponse createShippingOrder(ShippingOrderRequest shippingOrderRequest);
    String generateAwbNumber(Long orderId);
    BigDecimal calculateTotalWeight(Long orderId);
}
