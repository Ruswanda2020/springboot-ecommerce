package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.model.response.PaymentNotification;
import com.oneDev.ecommerce.model.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse create(Order order);
    PaymentResponse getByPaymentId(String paymentId);
    boolean verifyByPaymentId(String paymentId);
    void handelPaymentNotification(PaymentNotification paymentNotification);
}
