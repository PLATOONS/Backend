package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.CheckoutDto;
import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;

public interface IPaymentService {
    String createPayment(CreatePaymentRequestDto paymentDto);

    PaymentDto fetchPayment(Long paymentId);

    void deletePayment(Long paymentId);

    String updatePayment(UpdatePaymentDto paymentDto, Long paymentId);

    String checkout(CheckoutDto checkoutDto);
}