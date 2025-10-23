package com.platoons.e_commerce.repository;

import com.platoons.e_commerce.entity.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends CrudRepository<Review, Long> {
    Optional<List<Review>> findByOrderProductProductProductId(String productId);
}
