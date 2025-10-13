package com.platoons.e_commerce.service.impl;

import com.platoons.e_commerce.dto.CreateUserRequestDto;
import com.platoons.e_commerce.dto.CustomerDto;
import com.platoons.e_commerce.dto.UpdateCustomerRequestDto;
import com.platoons.e_commerce.entity.Authority;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.CustomerMapper;
import com.platoons.e_commerce.repository.AuthorityRepository;
import com.platoons.e_commerce.repository.CustomerRepository;
import com.platoons.e_commerce.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;

    @Override
    public String createCustomer(CreateUserRequestDto customerDto) {
        Customer customer =
                CustomerMapper.mapCreateUserRequestDtoToCustomer(customerDto, new Customer());

        customer.setPasswordHash(passwordEncoder.encode(customerDto.getPassword()));

        var savedCustomer = customerRepository.save(customer);

        // Grant the customer the role of CUSTOMER
        Authority authority = new Authority();
        authority.setAuthority("ROLE_CUSTOMER");
        authority.setCustomer(savedCustomer);
        authorityRepository.save(authority);

        return savedCustomer.getCustomerId();
    }

    @Override
    public CustomerDto fetchCustomer(String customerId) {
        var savedCustomer = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer", "customerId", customerId));
        
        return CustomerMapper.mapCustomerToCustomerDto(savedCustomer, new CustomerDto());
    }

    @Override
    public void deleteCustomer(String customerId) {
        var optionalCustomer = customerRepository.findById(customerId);

        // Early return for customers that don't exist
        if(optionalCustomer.isEmpty())
            return;

        var savedCustomer = optionalCustomer.get();
        savedCustomer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(savedCustomer);
    }

    @Override
    public String updateCustomer(UpdateCustomerRequestDto customerDto, String customerId) {
        Customer customer =
                customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                        .orElseThrow(() -> new EntityNotFoundException("customer", "customerId", customerId));

        CustomerMapper.mapUpdateCustomerRequestDtoToCustomer(customerDto, customer);

        customer.setPasswordHash(passwordEncoder.encode(customerDto.getPassword()));
        customerRepository.save(customer);

        return customer.getCustomerId();
    }
}
