package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.*;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.response.OrderResponse;
import com.oneDev.ecommerce.model.response.PaymentResponse;
import com.oneDev.ecommerce.model.response.ShippingRateResponse;
import com.oneDev.ecommerce.repository.*;
import com.oneDev.ecommerce.service.InventoryService;
import com.oneDev.ecommerce.service.PaymentService;
import com.oneDev.ecommerce.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserAddressesRepository userAddressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShippingService shippingService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CheckOutRequest checkoutRequest;
    private List<CartItem> cartItems;
    private UserAddresses userAddress;
    private Product product;
    private UserAddresses sellerAddress;
    private User seller;
    private User buyer;


    @BeforeEach
    void setUp() {
        checkoutRequest = new CheckOutRequest();
        checkoutRequest.setUserId(1L);
        checkoutRequest.setUserAddressId(1L);
        checkoutRequest.setSelectedCartItemIds(Arrays.asList(1L, 2L));

        cartItems = new ArrayList<>();
        CartItem cartItem1 = new CartItem();
        cartItem1.setCartId(1L);
        cartItem1.setProductId(1L);
        cartItem1.setQuantity(2);
        cartItem1.setPrice(new BigDecimal("100.00"));
        cartItems.add(cartItem1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setCartId(2L);
        cartItem2.setProductId(2L);
        cartItem2.setQuantity(1);
        cartItem2.setPrice(new BigDecimal("50.00"));
        cartItems.add(cartItem2);

        userAddress = new UserAddresses();
        userAddress.setUserAddressId(1L);

        seller = new User();
        seller.setUserId(1L);
        buyer = new User();
        buyer.setUserId(2L);

        product = new Product();
        product.setProductId(1L);
        product.setWeight(new BigDecimal("0.5"));
        product.setUserId(seller.getUserId());

        sellerAddress = new UserAddresses();
        sellerAddress.setUserAddressId(2L);
        sellerAddress.setUserId(seller.getUserId());

    }

    @Test
    void testCheckout_WhenCartIsEmpty() {
        // Arrange: Mock repository untuk mengembalikan daftar kosong
        when(cartItemRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        // Act & Assert: Pastikan ApplicationException dengan tipe RESOURCE_NOT_FOUND dilempar
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            orderService.checkOut(checkoutRequest);
        });

        // Assert: Validasi tipe exception dan pesan
        assertEquals(ExceptionType.RESOURCE_NOT_FOUND, exception.getType());
    }

    @Test
    void testCheckout_SuccessfulCheckout() {
        // Arrange
        when(cartItemRepository.findAllById(anyList())).thenReturn(cartItems);
        when(userAddressRepository.findById(anyLong())).thenReturn(Optional.of(userAddress));
        when(inventoryService.checkAndLockInventory(anyMap())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ShippingRateResponse shippingRateResponse = new ShippingRateResponse();
        shippingRateResponse.setShippingFee(new BigDecimal("10.00"));
        when(shippingService.calculateShippingRate(any())).thenReturn(shippingRateResponse);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setXenditInvoiceId("payment123");
        paymentResponse.setXenditInvoiceStatus("PENDING");
        paymentResponse.setXenditPaymentUrl("http://payment.url");
        when(paymentService.create(any())).thenReturn(paymentResponse);

        // Act
        OrderResponse result = orderService.checkOut(checkoutRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals("payment123", result.getXenditInvoiceId());
        assertEquals("http://payment.url", result.getXenditPaymentUrl());

        verify(cartItemRepository).findAllById(checkoutRequest.getSelectedCartItemIds());
        verify(userAddressRepository).findById(checkoutRequest.getUserAddressId());
        verify(inventoryService).checkAndLockInventory(anyMap());
        verify(orderRepository, times(3)).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(cartItemRepository).deleteAll(cartItems);
        verify(shippingService, times(1)).calculateShippingRate(any());
        verify(paymentService).create(any());
        verify(inventoryService).decreaseProductQuantity(anyMap());
    }
}