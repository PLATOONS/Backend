package com.platoons.e_commerce.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.platoons.e_commerce.dto.CheckoutDto;
import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;
import com.platoons.e_commerce.entity.Coupon;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.entity.OrderProduct;
import com.platoons.e_commerce.entity.OrderStatus;
import com.platoons.e_commerce.entity.Payment;
import com.platoons.e_commerce.entity.PaymentMethod;
import com.platoons.e_commerce.entity.PaymentStatus;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.PaymentMapper;
import com.platoons.e_commerce.repository.CouponRepository;
import com.platoons.e_commerce.repository.CustomerRepository;
import com.platoons.e_commerce.repository.OrderProductRepository;
import com.platoons.e_commerce.repository.OrderRepository;
import com.platoons.e_commerce.repository.OrderStatusRepository;
import com.platoons.e_commerce.repository.PaymentRepository;
import com.platoons.e_commerce.service.IPaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderProductRepository orderProductRepository;
    private final CouponRepository couponRepository;

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

    @Override
    public String checkout(CheckoutDto checkoutDto) {
        // Get logged-in username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch customer
        Customer customer = customerRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new EntityNotFoundException("customer", "username", username));

        // Fetch CART order status
        OrderStatus cartStatus = orderStatusRepository.findByStatusNameIgnoreCase("CART")
                .orElseThrow(() -> new EntityNotFoundException("orderStatus", "statusName", "CART"));

        // Fetch order with CART status for this customer
        Order order = orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus)
                .orElseThrow(() -> new EntityNotFoundException("order", "status", "CART"));

        // Fetch all order products
        var orderProducts = orderProductRepository.findAllByOrderAndDeletedAtIsNull(order);

        // Calculate total amount from order products
        double totalAmount = orderProducts.stream()
                .mapToDouble(OrderProduct::getTotalPrice)
                .sum();

        // Apply coupon discount if provided
        if (checkoutDto.getCoupon() != null && !checkoutDto.getCoupon().isBlank()) {
            Coupon coupon = couponRepository.findByCouponCode(checkoutDto.getCoupon())
                    .orElseThrow(() -> new EntityNotFoundException("coupon", "couponCode", checkoutDto.getCoupon()));
            totalAmount -= coupon.getDiscountAmount();
            // Ensure total is not negative
            if (totalAmount < 0) {
                totalAmount = 0.0;
            }
        }

        // Create and save payment
        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBillName(checkoutDto.getFirstName() + " " + checkoutDto.getLastName());
        payment.setDescription("Checkout payment for order " + order.getOrderId());

        Payment savedPayment = paymentRepository.save(payment);

        // Change the status of the order COMPLETED
        Optional<OrderStatus> optionalCompletedStatus = orderStatusRepository.findByStatusNameIgnoreCase("COMPLETED");

        OrderStatus completedStatus;

        if (optionalCompletedStatus.isEmpty()){
            OrderStatus status = new OrderStatus();
            status.setStatusName("CART");
            status.setDescription("User's cart");
            completedStatus = orderStatusRepository.save(status);
        }else{
            completedStatus = optionalCompletedStatus.get();
        }

        System.out.println("a");
        order.setOrderStatus(completedStatus);
        orderRepository.save(order);

        return String.valueOf(savedPayment.getPaymentId());
    }
}
