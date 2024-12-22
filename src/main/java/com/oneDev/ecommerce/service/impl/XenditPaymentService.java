package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.entity.User;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.response.PaymentNotification;
import com.oneDev.ecommerce.model.response.PaymentResponse;
import com.oneDev.ecommerce.repository.OrderRepository;
import com.oneDev.ecommerce.repository.UserRepository;
import com.oneDev.ecommerce.service.PaymentService;
import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class XenditPaymentService implements PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public PaymentResponse create(Order order) {

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "User with order id " + order.getUserId() + " not found"));

        Map<String, Object> params = new HashMap<>();
        params.put("external_id", order.getOrderId().toString());
        params.put("amount", order.getTotalAmount().doubleValue());
        params.put("payer_email", user.getEmail());
        params.put("description", "payment order # "+order.getOrderId());

        try {
            Invoice invoice = Invoice.create(params);

            return PaymentResponse.builder()
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .xenditInvoiceId(invoice.getId())
                    .amount(order.getTotalAmount())
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        } catch (XenditException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentResponse getByPaymentId(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return PaymentResponse.builder()
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .xenditInvoiceId(invoice.getId())
                    .amount(BigDecimal.valueOf(invoice.getAmount().doubleValue()))
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyByPaymentId(String paymentId) {

        try {
            Invoice invoice = Invoice.getById(paymentId);
            return "PAID".equals(invoice.getStatus());
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handelPaymentNotification(PaymentNotification paymentNotification) {
        String invoiceId = paymentNotification.getId();
        String status = paymentNotification.getStatus();

        Order order = orderRepository.findByXenditInvoiceId(invoiceId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Order not found for xendit invoice ID: " + invoiceId ));

        order.setXenditPaymentStatus(status);
        switch (status){
            case "PAID":
                order.setStatus(OrderStatus.PAID);
                break;
            case "EXPIRED":
                order.setStatus(OrderStatus.CANCELLED);
                break;
            case "FAILED":
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                break;
            case "PENDING":
                order.setStatus(OrderStatus.PENDING);
                break;
                default:
        }

        if (paymentNotification.getPaymentMethod() != null) {
            order.setXenditPaymentMethod(paymentNotification.getPaymentMethod());
        }
        orderRepository.save(order);
    }
}
