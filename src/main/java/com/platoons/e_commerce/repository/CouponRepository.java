package com.platoons.e_commerce.repository;

import com.platoons.e_commerce.entity.Coupon;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends CrudRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCode(String couponCode);
}
