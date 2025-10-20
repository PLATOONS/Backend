package com.platoons.e_commerce.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.platoons.e_commerce.entity.Product;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // Proyección con los campos que pide la respuesta
    interface ProductSummaryProjection {
        String getProductId();
        Double getPrice();
        Double getDiscountPercentage();
        Double getDiscountedPrice();
        Double getRating();
        Boolean getWishlisted();
        String getProductName();
        String getImageUrl();
        Instant getCreatedAt();
        String getCategory();
    }

    // JPQL con promedio de rating (AVG) y cálculo de precio con descuento
    @Query("""
        select
          p.productId as productId,
          p.price as price,
          p.discount * 100 as discountPercentage,
          (p.price - (p.price * p.discount)) as discountedPrice,
          coalesce(avg(r.rating), 0) as rating,
          false as wishlisted,
          p.name as productName,
          (select MIN(i.imageName) from ProductImage i where i.product = p and i.deletedAt is null) as imageUrl,
          p.createdAt as createdAt,
          p.category.name as category
        from Product p
        left join p.orderProducts o
        left join o.reviews r
        where p.deletedAt is null
               and p.price >= :minPrice
               and p.price <= :maxPrice
               and (:category is null or p.category.name = :category)
        group by p.productId, p.price, p.discount, p.name, p.createdAt
       \s""")
    Page<ProductSummaryProjection> findAllSummaries(Pageable pageable, String category, double minPrice, double maxPrice);

    @Transactional
    Optional<Product> findByProductIdAndDeletedAtIsNull(String productId);
}
