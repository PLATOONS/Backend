package com.platoons.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.platoons.e_commerce.entity.Customer;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, String> {

    Optional<Customer> findByCustomerIdAndDeletedAtIsNull(String productId);

    Optional<Customer> findByUsernameAndDeletedAtIsNull(String username);

    Optional<Customer> findByEmailAndDeletedAtIsNull(String email);

    Optional<Customer> findByCustomerIdAndDeletedAtIsNull(Long customerId);
}
