package com.platoons.e_commerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // FK a Customer
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_customer")
    )
    private Customer customer;

    // FK a Coupon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "coupon_id",
            foreignKey = @ForeignKey(name = "fk_order_coupon")
    )
    private Coupon coupon;

    // FK a OrderStatus
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "status_id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_order_status")
    )
    private OrderStatus orderStatus;

    //FK a Payment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payment_id",
            foreignKey = @ForeignKey(name = "fk_order_payment")
    )
    private Payment payment;

    // FK a Shipping
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "shipping_id",
            foreignKey = @ForeignKey(name = "fk_order_shipping")
    )
    private Shipping shipping;

    // Subtotal amount
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Subtotal must be positive")
    private Double subtotalAmount;

    // Total amount
    @NotNull(message = "Total is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total must be positive")
    private Double totalAmount;
}
