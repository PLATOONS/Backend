package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreateCouponRequest;
import com.platoons.e_commerce.dto.CouponDto;
import com.platoons.e_commerce.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/coupon")
public class CouponController {

    private final CouponService service;

    public CouponController(CouponService service) {
        this.service = service;
    }

    /** GET /api/v1/coupon/{disscount_code} -> 200 o 404 */
    @GetMapping("/{disscount_code}")
    public ResponseEntity<CouponDto> getByCode(@PathVariable("disscount_code") String disscountCode) {
        return service.findByCode(disscountCode)
                .map(c -> ResponseEntity.ok(new CouponDto(
                        c.getCouponId(),
                        c.getCouponCode(),
                        c.getDiscountAmount()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** POST /api/v1/coupon -> 201 (sin body) o 400 */
    @PostMapping
    public ResponseEntity<Void> createCoupon(@Valid @RequestBody CreateCouponRequest req) {
        try {
            var created = service.create(req.getCouponCode(), req.getDiscountAmount());
            String encoded = URLEncoder.encode(created.getCouponCode(), StandardCharsets.UTF_8);
            return ResponseEntity.created(URI.create("/api/v1/coupon/" + encoded)).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build(); // contrato: 400 en errores
        }
    }
}
