package com.platoons.e_commerce.repository;

import java.util.List;
import java.util.Optional;

import com.platoons.e_commerce.dto.CartProductsDto;
import com.platoons.e_commerce.dto.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.platoons.e_commerce.entity.Product;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // JPQL with average rating (AVG) and discount price calculation, mapping directly to DTO
    @Query("""
        select new com.platoons.e_commerce.dto.ProductSummaryDto(
          p.productId,
          p.price,
          p.discount * 100,
          (p.price - (p.price * p.discount)),
          coalesce(avg(r.rating), 0),
          false,
          p.name,
          (select MIN(i.imageName) from ProductImage i where i.product = p and i.deletedAt is null),
          p.createdAt,
          p.category.name,
          (select count(*) from Review rv
             left join rv.orderProduct ro
             left join ro.product pr
             where pr.productId = p.productId
             and pr.deletedAt is null
             and ro.deletedAt is null
             and rv.deletedAt is null)
        )
        from Product p
        left join p.orderProducts o
        left join o.reviews r
        where p.deletedAt is null
               and p.price >= :minPrice
               and p.price <= :maxPrice
               and (:category is null or p.category.name = :category)
        group by p.productId, p.price, p.discount, p.name, p.createdAt, p.category.name
       \s""")
    Page<ProductSummaryDto> findAllSummaries(Pageable pageable, String category, double minPrice, double maxPrice);

    @Transactional
    Optional<Product> findByProductIdAndDeletedAtIsNull(String productId);

    @Query(
            """
                select case when count(*) > 0 then true else false end
                from OrderProduct op
                left join op.product p
                left join op.order o
                left join o.customer c
                left join o.orderStatus s
                where p.productId = :productId
                and c.username = :username
                and s.statusName = 'COMPLETED'
            """
    )
    boolean userBoughtProduct(String username, String productId); // Might need to change this for the status name

    @Query(
            """
                select new com.platoons.e_commerce.dto.CartProductsDto(p.productId,
                       p.name as productName,
                       (p.price - p.discountAmount) as price,
                       (select MIN(i.imageName) from ProductImage i where i.product = p and i.deletedAt is null and i.color = op.color) as imageUrl,
                       op.color as color,
                       op.quantity as quantity)
                from Product p
                left join p.orderProducts op
                left join op.order o
                left join o.customer c
                left join o.orderStatus s
                where c.username = :username
                and s.statusName = 'CART'
            """
    )
    List<CartProductsDto> fetchCartProducts(String username);
}
