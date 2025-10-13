package com.platoons.e_commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platoons.e_commerce.controller.CustomerController;
import com.platoons.e_commerce.dto.CreateUserRequestDto;
import com.platoons.e_commerce.dto.CustomerDto;
import com.platoons.e_commerce.dto.UpdateCustomerRequestDto;
import com.platoons.e_commerce.service.ICustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICustomerService customerService;

    @MockitoBean
    private AuditorAware<String> auditorAware;

    @Test
    void fetchCustomer_returnsOkWithBody() throws Exception {
        CustomerDto dto = new CustomerDto();
        when(customerService.fetchCustomer("ABC")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/customer/{customerId}", "ABC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(customerService).fetchCustomer("ABC");
    }

//    @Test
//    void createCustomer_returnsCreatedWithLocationAndMessage() throws Exception {
//        when(customerService.createCustomer(any(CreateUserRequestDto.class)))
//                .thenReturn("CUS-123");
//
//        CreateUserRequestDto dto = new CreateUserRequestDto();
//        dto.setUsername("joemama");
//        dto.setFirstName("Joe");
//        dto.setLastName("Mama");
//        dto.setPassword("StrongP@ssw0rd");
//        dto.setEmail("user@example.com");
//
//        mockMvc.perform(post("/api/v1/customer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isCreated())
//                .andExpect(header().string("Location", "http://localhost/api/v1/customer/CUS-123"))
//                .andExpect(jsonPath("$.message").value("Successfully created customer"));
//
//        verify(customerService).createCustomer(any(CreateUserRequestDto.class));
//    }

    @Test
    void deleteCustomer_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/customer/{customerId}", "CUS-9"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer("CUS-9");
    }

    @Test
    void updateCustomer_returnsCreatedWithLocationAndMessage() throws Exception {
        when(customerService.updateCustomer(any(UpdateCustomerRequestDto.class), eq("CUS-777")))
                .thenReturn("CUS-777");

        UpdateCustomerRequestDto dto = new UpdateCustomerRequestDto();
        dto.setUsername("joemama");
        dto.setFirstName("Joe");
        dto.setLastName("Mama");
        dto.setPassword("StrongP@ssw0rd");
        dto.setEmail("user@example.com");
        dto.setPhoneNumber("+502-98765432");

        mockMvc.perform(put("/api/v1/customer/{customerId}", "CUS-777")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/customer/CUS-777"))
                .andExpect(jsonPath("$.message").value("Successfully updated customer"));

        verify(customerService).updateCustomer(any(UpdateCustomerRequestDto.class), eq("CUS-777"));
    }
}
