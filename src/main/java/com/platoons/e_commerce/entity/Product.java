package com.platoons.e_commerce.entity;

import java.util.Set;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_info_id")
    private ExtraInfo extraInfo;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<ProductImage> productImages;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<OrderProduct> orderProducts;

    @Id
    @UuidGenerator
    private String productId;

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;

    private double discount;

    private double discountAmount;

    private String measurements;

    private Set<String> availableColors;

    @NotNull(message = "Description is required")
    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than 0")
    private double price;

    @NotNull(message = "Stock quantity is required")
    private int stockQuantity;
}
