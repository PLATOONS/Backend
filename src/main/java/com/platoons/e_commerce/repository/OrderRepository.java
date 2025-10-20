package com.platoons.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.entity.OrderStatus;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

    Optional<Order> findByOrderIdAndDeletedAtIsNull(Long orderId);

    Optional<Order> findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(Customer customer, OrderStatus orderStatus);
}
