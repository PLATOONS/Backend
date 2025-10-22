package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.platoons.e_commerce.dto.CheckoutDto;
import com.platoons.e_commerce.entity.Coupon;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.entity.OrderProduct;
import com.platoons.e_commerce.entity.OrderStatus;
import com.platoons.e_commerce.entity.Payment;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.repository.CouponRepository;
import com.platoons.e_commerce.repository.CustomerRepository;
import com.platoons.e_commerce.repository.OrderProductRepository;
import com.platoons.e_commerce.repository.OrderRepository;
import com.platoons.e_commerce.repository.OrderStatusRepository;
import com.platoons.e_commerce.repository.PaymentRepository;
import com.platoons.e_commerce.service.impl.PaymentServiceImpl;

public class CheckoutServiceTests {

    private CustomerRepository customerRepository;
    private OrderStatusRepository orderStatusRepository;
    private OrderRepository orderRepository;
    private OrderProductRepository orderProductRepository;
    private CouponRepository couponRepository;
    private PaymentRepository paymentRepository;
    private PaymentServiceImpl paymentServiceImpl;

    private CheckoutDto checkoutDto;
    private Customer customer;
    private OrderStatus cartStatus;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        orderStatusRepository = mock(OrderStatusRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderProductRepository = mock(OrderProductRepository.class);
        couponRepository = mock(CouponRepository.class);
        paymentRepository = mock(PaymentRepository.class);

        paymentServiceImpl = new PaymentServiceImpl(paymentRepository, customerRepository,
                orderRepository, orderStatusRepository, orderProductRepository, couponRepository);

        // Setup CheckoutDto
        checkoutDto = new CheckoutDto();
        checkoutDto.setFirstName("John");
        checkoutDto.setLastName("Doe");
        checkoutDto.setEmail("john.doe@example.com");
        checkoutDto.setPhone("123-456-7890");
        checkoutDto.setAddress("123 Main St");
        checkoutDto.setCity("New York");
        checkoutDto.setState("NY");
        checkoutDto.setZipCode("10001");
        checkoutDto.setCountry("USA");
        checkoutDto.setCardNumber("4111111111111111");
        checkoutDto.setCardExpirationDate("12/25");
        checkoutDto.setCardCvv("123");

        // Setup Customer
        customer = new Customer();
        customer.setCustomerId("uuid");
        customer.setUsername("testuser");

        // Setup OrderStatus
        cartStatus = new OrderStatus();
        cartStatus.setStatusId(1L);
        cartStatus.setStatusName("CART");

        // Setup Order
        order = new Order();
        order.setOrderId(1L);
        order.setCustomer(customer);
        order.setOrderStatus(cartStatus);

        // Setup Payment
        payment = new Payment();
        payment.setPaymentId(1L);
        payment.setAmount(100.0);
    }

    @Test
    void testCheckout_WithValidCoupon_AppliesDiscount() {
        // Arrange
        checkoutDto.setCoupon("SAVE20");
        OrderProduct op1 = createOrderProduct(1L, 100.0);

        Coupon coupon = new Coupon();
        coupon.setCouponCode("SAVE20");
        coupon.setDiscountAmount(20.0);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.of(order));
            when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(order))
                    .thenReturn(Collections.singletonList(op1));
            when(couponRepository.findByCouponCode("SAVE20"))
                    .thenReturn(Optional.of(coupon));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // Act
            String result = paymentServiceImpl.checkout(checkoutDto);

            // Assert
            assertEquals("1", result);
            verify(paymentRepository, times(1)).save(argThat(p -> p.getAmount() == 80.0));
            verify(couponRepository, times(1)).findByCouponCode("SAVE20");
        }
    }

    @Test
    void testCheckout_CouponExceedsTotalAmount_SetsTotalToZero() {
        // Arrange
        checkoutDto.setCoupon("HUGE_DISCOUNT");
        OrderProduct op1 = createOrderProduct(1L, 50.0);

        Coupon coupon = new Coupon();
        coupon.setCouponCode("HUGE_DISCOUNT");
        coupon.setDiscountAmount(100.0);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.of(order));
            when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(order))
                    .thenReturn(Collections.singletonList(op1));
            when(couponRepository.findByCouponCode("HUGE_DISCOUNT"))
                    .thenReturn(Optional.of(coupon));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // Act
            String result = paymentServiceImpl.checkout(checkoutDto);

            // Assert
            assertEquals("1", result);
            verify(paymentRepository, times(1)).save(argThat(p -> p.getAmount() == 0.0));
        }
    }

    @Test
    void testCheckout_BlankCouponCode_IgnoresCoupon() {
        // Arrange
        checkoutDto.setCoupon("   ");
        OrderProduct op1 = createOrderProduct(1L, 100.0);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.of(order));
            when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(order))
                    .thenReturn(Collections.singletonList(op1));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // Act
            String result = paymentServiceImpl.checkout(checkoutDto);

            // Assert
            assertEquals("1", result);
            verify(couponRepository, never()).findByCouponCode(anyString());
            verify(paymentRepository, times(1)).save(argThat(p -> p.getAmount() == 100.0));
        }
    }

    @Test
    void testCheckout_CustomerNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> paymentServiceImpl.checkout(checkoutDto));
            verify(customerRepository, times(1)).findByUsernameAndDeletedAtIsNull("testuser");
        }
    }

    @Test
    void testCheckout_CartStatusNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> paymentServiceImpl.checkout(checkoutDto));
            verify(orderStatusRepository, times(1)).findByStatusNameIgnoreCase("CART");
        }
    }

    @Test
    void testCheckout_OrderNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> paymentServiceImpl.checkout(checkoutDto));
            verify(orderRepository, times(1)).findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus);
        }
    }

    @Test
    void testCheckout_InvalidCoupon_ThrowsEntityNotFoundException() {
        // Arrange
        checkoutDto.setCoupon("INVALID");
        OrderProduct op1 = createOrderProduct(1L, 100.0);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.of(order));
            when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(order))
                    .thenReturn(Collections.singletonList(op1));
            when(couponRepository.findByCouponCode("INVALID"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> paymentServiceImpl.checkout(checkoutDto));
            verify(couponRepository, times(1)).findByCouponCode("INVALID");
        }
    }

    @Test
    void testCheckout_EmptyCart_CalculatesZeroTotal() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.of(order));
            when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(order))
                    .thenReturn(Collections.emptyList());
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // Act
            String result = paymentServiceImpl.checkout(checkoutDto);

            // Assert
            assertEquals("1", result);
            verify(paymentRepository, times(1)).save(argThat(p -> p.getAmount() == 0.0));
        }
    }

    @Test
    void testCheckout_PaymentCreatedWithCorrectDetails() {
        // Arrange
        OrderProduct op1 = createOrderProduct(1L, 100.0);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(customerRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                    .thenReturn(Optional.of(customer));
            when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                    .thenReturn(Optional.of(cartStatus));
            when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus))
                    .thenReturn(Optional.of(order));
            when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(order))
                    .thenReturn(Collections.singletonList(op1));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // Act
            paymentServiceImpl.checkout(checkoutDto);

            // Assert
            verify(paymentRepository, times(1)).save(argThat(p ->
                    p.getAmount() == 100.0 &&
                            p.getBillName().equals("John Doe") &&
                            p.getDescription().equals("Checkout payment for order 1") &&
                            p.getPaymentDate() != null
            ));
        }
    }

    // Helper method
    private OrderProduct createOrderProduct(Long id, Double totalPrice) {
        OrderProduct op = new OrderProduct();
        op.setOrderProductId(id);
        op.setTotalPrice(totalPrice);
        op.setOrder(order);
        return op;
    }
}