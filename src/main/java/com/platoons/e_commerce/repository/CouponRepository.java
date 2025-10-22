package com.platoons.e_commerce.repository;

import com.platoons.e_commerce.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCodeIgnoreCase(String couponCode);
}
