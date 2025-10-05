package com.platoons.e_commerce;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.platoons.e_commerce.repository.AuthorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.platoons.e_commerce.dto.CreateUserRequestDto;
import com.platoons.e_commerce.dto.CustomerDto;
import com.platoons.e_commerce.dto.UpdateCustomerRequestDto;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.CustomerMapper;
import com.platoons.e_commerce.repository.CustomerRepository;
import com.platoons.e_commerce.service.impl.CustomerServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository, passwordEncoder, authorityRepository);
    }

    @Test
    void createCustomer_mapsAndSaves_andReturnsId() {
        CreateUserRequestDto dto = mock(CreateUserRequestDto.class);
        Customer mapped = new Customer();
        Customer saved = new Customer();
        saved.setCustomerId("CUS-001");

        try (MockedStatic<CustomerMapper> mocked = mockStatic(CustomerMapper.class)) {
            mocked.when(() -> CustomerMapper.mapCreateUserRequestDtoToCustomer(eq(dto), any(Customer.class)))
                  .thenReturn(mapped);
            when(customerRepository.save(same(mapped))).thenReturn(saved);

            String id = service.createCustomer(dto);

            assertEquals("CUS-001", id);
            verify(customerRepository).save(mapped);
        }
    }

    @Test
    void fetchCustomer_found_returnsDto() {
        Customer entity = new Customer();
        CustomerDto dto = new CustomerDto();

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("ABC"))
                .thenReturn(Optional.of(entity));

        try (MockedStatic<CustomerMapper> mocked = mockStatic(CustomerMapper.class)) {
            mocked.when(() -> CustomerMapper.mapCustomerToCustomerDto(same(entity), any(CustomerDto.class)))
                  .thenReturn(dto);

            CustomerDto out = service.fetchCustomer("ABC");

            assertSame(dto, out);
        }
    }

    @Test
    void fetchCustomer_notFound_throwsEntityNotFound() {
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("NOPE"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.fetchCustomer("NOPE"));
    }

    @Test
    void deleteCustomer_nonExisting_isNoOp_usesFindById() {
        when(customerRepository.findById("X")).thenReturn(Optional.empty());

        service.deleteCustomer("X");

        verify(customerRepository).findById("X");
        verify(customerRepository, never()).save(any());
        verify(customerRepository, never()).findByCustomerIdAndDeletedAtIsNull(anyString());
    }

    @Test
    void deleteCustomer_existing_setsDeletedAt_andSaves() {
        Customer c = new Customer();
        when(customerRepository.findById("OK")).thenReturn(Optional.of(c));

        service.deleteCustomer("OK");

        assertNotNull(c.getDeletedAt());
        verify(customerRepository).save(c);
    }

    @Test
    void updateCustomer_mapsAndReturnsId_withoutSaving() {
        UpdateCustomerRequestDto dto = mock(UpdateCustomerRequestDto.class);
        Customer c = new Customer();
        c.setCustomerId("CUS-777");

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("CUS-777"))
                .thenReturn(Optional.of(c));

        try (MockedStatic<CustomerMapper> mocked = mockStatic(CustomerMapper.class)) {
            mocked.when(() -> CustomerMapper.mapUpdateCustomerRequestDtoToCustomer(eq(dto), same(c)))
                  .then(inv -> null);

            String id = service.updateCustomer(dto, "CUS-777");

            assertEquals("CUS-777", id);
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }
}
