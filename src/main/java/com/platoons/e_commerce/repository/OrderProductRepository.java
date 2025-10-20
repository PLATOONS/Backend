package com.platoons.e_commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.entity.OrderProduct;
import com.platoons.e_commerce.entity.Product;

public interface OrderProductRepository extends CrudRepository<OrderProduct, Long> {

    Optional<OrderProduct> findByOrderAndProductAndColor(Order order, Product product, String color);

    List<OrderProduct> findAllByOrder(Order order);

    Optional<OrderProduct> findByOrderAndProductProductIdAndDeletedAtIsNull(Order order, String productId);

    List<OrderProduct> findAllByOrderAndDeletedAtIsNull(Order order);
}
