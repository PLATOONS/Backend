package com.platoons.e_commerce.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;
import com.platoons.e_commerce.entity.Payment;
import com.platoons.e_commerce.entity.PaymentMethod;
import com.platoons.e_commerce.entity.PaymentStatus;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.PaymentMapper;
import com.platoons.e_commerce.repository.PaymentRepository;
import com.platoons.e_commerce.service.IPaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public String createPayment(CreatePaymentRequestDto paymentDto) {
        Payment payment = PaymentMapper.mapCreatePaymentRequestDtoToPayment(paymentDto, new Payment());

        var savedPayment = paymentRepository.save(payment);

        PaymentMethod method = new PaymentMethod();
        PaymentStatus status = new PaymentStatus();
        Payment payment2 = new Payment();
        payment2.setPaymentStatus(status);
        payment2.setPaymentMethod(method);
        paymentRepository.save(payment2);

        return String.valueOf(savedPayment.getPaymentId());
    }

    @Override
    public PaymentDto fetchPayment(Long paymentId) {
        var savedPayment = paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("payment", "paymentId", paymentId.toString()));
        return PaymentMapper.mapPaymentToPaymentDto(savedPayment, new PaymentDto());
    }

    @Override
    public void deletePayment(Long paymentId) {
        var optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isEmpty())
            return;

        var savedPayment = optionalPayment.get();
        savedPayment.setDeletedAt(LocalDateTime.now());
        paymentRepository.save(savedPayment);
    }

    @Override
    public String updatePayment(UpdatePaymentDto paymentDto, Long paymentId) {
        Payment payment = paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("payment", "paymentId", paymentId.toString()));

        PaymentMapper.mapUpdatePaymentDtoToPayment(paymentDto, payment);

        return String.valueOf(payment.getPaymentId());
    }
}
