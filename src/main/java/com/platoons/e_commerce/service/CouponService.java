package com.platoons.e_commerce.service;

import com.platoons.e_commerce.entity.Coupon;
import com.platoons.e_commerce.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CouponService {
    private final CouponRepository repo;

    public CouponService(CouponRepository repo) {
        this.repo = repo;
    }

    public Optional<Coupon> findByCode(String code) {
        if (code == null) return Optional.empty();
        return repo.findByCouponCodeIgnoreCase(code.trim());
    }

    /** Crea un cupón; lanza IllegalArgumentException si ya existe */
    public Coupon create(String code, Double amount) {
        String trimmed = code == null ? null : code.trim();
        if (trimmed == null || trimmed.isBlank()) {
            throw new IllegalArgumentException("Invalid coupon code");
        }
        if (repo.findByCouponCodeIgnoreCase(trimmed).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        Coupon c = new Coupon();
        c.setCouponCode(trimmed);
        c.setDiscountAmount(amount);
        return repo.save(c);
    }
}


