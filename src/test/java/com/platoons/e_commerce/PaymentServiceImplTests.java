package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.platoons.e_commerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;
import com.platoons.e_commerce.entity.Payment;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.PaymentMapper;
import com.platoons.e_commerce.service.impl.PaymentServiceImpl;

public class PaymentServiceImplTests {
    private PaymentRepository  paymentRepository;
    private PaymentServiceImpl paymentServiceImpl;

    @BeforeEach
    void setUp(){
        paymentRepository = mock(PaymentRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderStatusRepository orderStatusRepository = mock(OrderStatusRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        OrderProductRepository orderProductRepository = mock(OrderProductRepository.class);
        CouponRepository couponRepository = mock(CouponRepository.class);

        paymentServiceImpl = new PaymentServiceImpl(paymentRepository, customerRepository,
                orderRepository, orderStatusRepository, orderProductRepository, couponRepository);
    }

    @Test
    void testCreatePayment(){
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        Payment payment = new Payment();
        payment.setPaymentId(1L);

        try(MockedStatic<PaymentMapper> mockedMapper = mockStatic(PaymentMapper.class)){
            mockedMapper.when(()->PaymentMapper.mapCreatePaymentRequestDtoToPayment(eq(requestDto), any(Payment.class))).thenReturn(payment);

            when(paymentRepository.save(payment)).thenReturn(payment);

            String result = paymentServiceImpl.createPayment(requestDto);

            assertEquals("1", result);
            verify(paymentRepository, times(1)).save(payment);
        }
    }

    @Test
    void testfetchPayment_Found(){
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        PaymentDto paymentDto = new PaymentDto();

        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(payment));

        try(MockedStatic<PaymentMapper> mockedMapper = mockStatic(PaymentMapper.class)){
            mockedMapper.when(()->PaymentMapper.mapPaymentToPaymentDto(eq(payment), any(PaymentDto.class))).thenReturn(paymentDto);
            
            PaymentDto result = paymentServiceImpl.fetchPayment(1L);
            assertNotNull(result);
            assertEquals(paymentDto, result);
            verify(paymentRepository, times(1)).findByPaymentIdAndDeletedAtIsNull(1L);
        }
    }

    @Test
    void testfetchPayment_NotFound(){
        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, ()->paymentServiceImpl.fetchPayment(1L));
        verify(paymentRepository, times(1)).findByPaymentIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testupdatePayment_Found(){
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        UpdatePaymentDto updatePaymentDto = new UpdatePaymentDto();

        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(payment));

        try (MockedStatic<PaymentMapper> mockedMapper = mockStatic(PaymentMapper.class)){
            mockedMapper.when(() -> PaymentMapper.mapUpdatePaymentDtoToPayment(eq(updatePaymentDto), any(Payment.class))).then(invocation -> null);
        }

        when(paymentRepository.save(payment)).thenReturn(payment);

        String result = paymentServiceImpl.updatePayment(updatePaymentDto, 1L);

        assertEquals("1", result);
        verify(paymentRepository, times(1)).findByPaymentIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testupdatePayment_NotFound(){
        UpdatePaymentDto updatePaymentDto = new UpdatePaymentDto();
    
        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> paymentServiceImpl.updatePayment(updatePaymentDto, 1L));
        verify(paymentRepository, times(1)).findByPaymentIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testdeletePayment_Found(){
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentServiceImpl.deletePayment(1L);

        assertNotNull(payment.getDeletedAt());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void testdeletePayment_NotFound(){
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        paymentServiceImpl.deletePayment(1L);

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any());
    }
}
