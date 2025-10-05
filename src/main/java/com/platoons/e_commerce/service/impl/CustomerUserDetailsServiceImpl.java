package com.platoons.e_commerce.service.impl;

import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.repository.CustomerRepository;
import com.platoons.e_commerce.service.ICustomerUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsServiceImpl implements ICustomerUserDetailsService {
    private final CustomerRepository customerRepository;

    // It's actually username or email
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Customer> customerByUsername = customerRepository.findByUsername(username);
        Optional<Customer> customerByEmail = customerRepository.findByEmail(username);

        Customer customer = null;

        if(customerByUsername.isPresent()){
            customer = customerByUsername.get();
        }else if(customerByEmail.isPresent()){
            customer = customerByEmail.get();
        }else{
            throw new BadCredentialsException("Incorrect credentials");
        }

        List<GrantedAuthority> authorities = customer.getAuthorities().stream().map(authority -> new SimpleGrantedAuthority(authority.getAuthority())).collect(Collectors.toList());

        return new User(customer.getUsername(), customer.getPasswordHash(), authorities);
    }

}
