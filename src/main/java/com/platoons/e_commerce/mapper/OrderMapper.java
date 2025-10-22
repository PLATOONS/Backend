package com.platoons.e_commerce.mapper;

import com.platoons.e_commerce.dto.*;
import com.platoons.e_commerce.entity.*;

public class OrderMapper {

    public static Order mapCreateOrderRequestDtoToOrder(CreateOrderRequestDto orderDto, Order order) {
        order.setSubtotalAmount(orderDto.getSubtotalAmount());
        order.setTotalAmount(orderDto.getTotalAmount());

        Customer customer = new Customer();
        customer.setCustomerId(String.valueOf(orderDto.getCustomer()));
        order.setCustomer(customer);

        if (orderDto.getCouponId() != null) {
            Coupon coupon = new Coupon();
            coupon.setCouponId(orderDto.getCouponId());
            order.setCoupon(coupon);
        }

        return order;
    }

    public static OrderDto mapOrderToOrderDto(Order order, OrderDto orderDto) {
        orderDto.setOrderId(order.getOrderId());
        orderDto.setSubtotalAmount(order.getSubtotalAmount());
        orderDto.setTotalAmount(order.getTotalAmount());

        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerId(order.getCustomer().getCustomerId());
        customerDto.setRegistrationDate(order.getCustomer().getRegistrationDate());
        customerDto.setUsername(order.getCustomer().getUsername());
        customerDto.setEmail(order.getCustomer().getEmail());
        customerDto.setPhoneNumber(order.getCustomer().getPhoneNumber());
        customerDto.setFirstName(order.getCustomer().getFirstName());
        customerDto.setLastName(order.getCustomer().getLastName());
        orderDto.setCustomer(customerDto);

        if (order.getCoupon() != null) {
            CouponDto couponDto = new CouponDto();
            couponDto.setCouponId(order.getCoupon().getCouponId());
            couponDto.setCouponCode(order.getCoupon().getCouponCode());
            couponDto.setDiscountAmount(order.getCoupon().getDiscountAmount());
            orderDto.setCoupon(couponDto);
        }

        return orderDto;
    }

    public static Order mapUpdateOrderDtoToOrder(UpdateOrderDto orderDto, Order order) {
        order.setSubtotalAmount(orderDto.getSubtotalAmount());
        order.setTotalAmount(orderDto.getTotalAmount());

        Customer customer = new Customer();
        customer.setCustomerId(String.valueOf(orderDto.getCustomer()));
        order.setCustomer(customer);

        if (orderDto.getCouponId() != null) {
            Coupon coupon = new Coupon();
            coupon.setCouponId(orderDto.getCouponId());
            order.setCoupon(coupon);
        } else {
            order.setCoupon(null);
        }

        return order;
    }

    public static OrderResponseDto mapOrderToOrderResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setSubtotalAmount(order.getSubtotalAmount());
        dto.setTotalAmount(order.getTotalAmount());

        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
            dto.setCustomerUsername(order.getCustomer().getUsername());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }

        if (order.getCoupon() != null) {
            dto.setCouponId(order.getCoupon().getCouponId());
            dto.setCouponCode(order.getCoupon().getCouponCode());
            dto.setDiscountAmount(order.getCoupon().getDiscountAmount());
        }

        return dto;
    }
}
