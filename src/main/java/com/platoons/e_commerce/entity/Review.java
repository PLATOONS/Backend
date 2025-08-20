package com.platoons.e_commerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Review
@Entity
@Table(
        name = "review",
        indexes = {
            @Index(name = "idx_review_customer_id", columnList = "customer_id"),
            @Index(name = "idx_review_order_product_id", columnList = "order_product_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "customer is required")
    private Customer customer;

     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     @JoinColumn(name = "order_product_id", nullable = false)
     @NotNull(message = "order product is required")
     private OrderProduct orderProduct;


    @NotBlank(message = "comment is required")
    @Size(max = 4000, message = "comment is too long")
    @Column(nullable = false, length = 4000)
    private String comment;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

}
