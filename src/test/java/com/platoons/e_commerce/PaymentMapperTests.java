package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;
import com.platoons.e_commerce.entity.Payment;
import com.platoons.e_commerce.entity.PaymentMethod;
import com.platoons.e_commerce.entity.PaymentStatus;
import com.platoons.e_commerce.mapper.PaymentMapper;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class PaymentMapperTests {
    @Test
    void testmapCreatePaymentRequestDtoToPayment() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto();
        dto.setAmount(100.00);
        dto.setPaymentDate(LocalDateTime.of(2025, 9, 3, 20, 17, 0));
        dto.setBillName("John Doe");
        dto.setDescription("Lorem Ipsum...");

        Payment payment = new Payment();

        Payment result = PaymentMapper.mapCreatePaymentRequestDtoToPayment(dto, payment);

        assertEquals(100.00, result.getAmount());
        assertEquals(LocalDateTime.of(2025, 9, 3, 20, 17, 0), result.getPaymentDate());
        assertEquals("John Doe", result.getBillName());
        assertEquals("Lorem Ipsum...", result.getDescription());
    }

    @Test
    void testPaymentToPaymentDto() {
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setAmount(100.00);
        payment.setPaymentDate(LocalDateTime.of(2025, 9, 3, 20, 27, 0));
        payment.setBillName("John Doe");
        payment.setDescription("Lorem Ipsum...");

        PaymentMethod method = new PaymentMethod();
        method.setMethodId(1L);
        method.setName("Method_2");
        method.setDescription("Lorem Ipsum...");
        payment.setPaymentMethod(method);

        PaymentStatus status = new PaymentStatus();
        status.setStatusId(1L);
        status.setName("Status_1");
        status.setDescription("Lorem Ipsum...");
        payment.setPaymentStatus(status);

        PaymentDto dto = new PaymentDto();

        PaymentDto result = PaymentMapper.mapPaymentToPaymentDto(payment, dto);

        assertEquals(1l, result.getPaymentId());
        assertEquals(100.00, result.getAmount());
        assertEquals(LocalDateTime.of(2025, 9, 3, 20, 27, 0), result.getPaymentDate());
        assertEquals("John Doe", result.getBillName());
        assertEquals("Lorem Ipsum...", result.getDescription());
        assertEquals(1L, result.getPaymentMethod().getMethodId());
        assertEquals("Method_2", result.getPaymentMethod().getName());
        assertEquals("Lorem Ipsum...", result.getPaymentMethod().getDescription());
        assertEquals(1L, result.getPaymentStatus().getStatusId());
        assertEquals("Status_1", result.getPaymentStatus().getName());
        assertEquals("Lorem Ipsum...", result.getPaymentStatus().getDescription());
    }

    @Test
    void testMapUpdatePaymentDtoToPayment() {
        UpdatePaymentDto dto = new UpdatePaymentDto();
        dto.setAmount(100.50);
        dto.setPaymentDate(LocalDateTime.of(2023, 5, 12, 14, 0, 0));
        dto.setBillName("Sam Smith");
        dto.setDescription("Rusticatio Mexicana");
        dto.setMethodId(2L);
        dto.setStatusId(2L);

        Payment payment = new Payment();
        payment.setPaymentId(2L);
        payment.setPaymentDate(LocalDateTime.of(2023, 5, 12, 14, 0, 0));

        Payment result = PaymentMapper.mapUpdatePaymentDtoToPayment(dto, payment);

        assertEquals(100.50, result.getAmount());
        assertEquals("Sam Smith", result.getBillName());
        assertEquals("Rusticatio Mexicana", result.getDescription());
        assertEquals(2L, result.getPaymentMethod().getMethodId());
        assertEquals(2L, result.getPaymentStatus().getStatusId());

        assertEquals(2L, result.getPaymentId());
        assertEquals(LocalDateTime.of(2023, 5, 12, 14, 0, 0), result.getPaymentDate());

    }
}
