package com.platoons.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.platoons.e_commerce.entity.OrderStatus;

public interface OrderStatusRepository extends CrudRepository<OrderStatus, Long> {

    Optional<OrderStatus> findByStatusNameIgnoreCase(String statusName);
}
