package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import com.platoons.e_commerce.controller.PaymentController;
import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;
import com.platoons.e_commerce.service.IPaymentService;

public class PaymentControllerTest {
    private IPaymentService ipaymentService;
    private PaymentController paymentController;
    private Long paymentId;

    @BeforeEach
    void setUp(){
        ipaymentService = mock(IPaymentService.class);
        paymentController = new PaymentController(ipaymentService);
        paymentId = 1L;
    }

    @Test
    void fetchPayment_returnsPaymentDetails(){
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setPaymentId(paymentId);
        paymentDto.setAmount(456.00);

        when(ipaymentService.fetchPayment(paymentId)).thenReturn(paymentDto);

        ResponseEntity<PaymentDto> response = paymentController.fetchPayment(paymentId);

        assertEquals(200 , response.getStatusCodeValue());
        assertEquals(456.00, response.getBody().getAmount());
        verify(ipaymentService, times(1)).fetchPayment(paymentId);
    }

    @Test
    void createPayment_returnsCreatedResponse(){
        CreatePaymentRequestDto paymentRequestDto = new CreatePaymentRequestDto();
        paymentRequestDto.setAmount(218.00);

        when(ipaymentService.createPayment(paymentRequestDto)).thenReturn(String.valueOf(paymentId));

        try(MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)){
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uricomponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyString())).thenReturn(uricomponents);
            when(uricomponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/payment/"+ paymentId));

            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = paymentController.createPayment(paymentRequestDto);

            assertEquals(201, response.getStatusCodeValue());
            assertEquals("Successful Payment", response.getBody().getMessage());
            verify(ipaymentService, times(1)).createPayment(paymentRequestDto);
        }
    }

    @Test
    void updatePayment_returnsCreatedResponse(){
        UpdatePaymentDto updatePaymentDto = new UpdatePaymentDto();
        updatePaymentDto.setAmount(67.00);

        when(ipaymentService.updatePayment(updatePaymentDto, paymentId)).thenReturn(String.valueOf(paymentId));

        try(MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)){
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uricomponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyString())).thenReturn(uricomponents);
            when(uricomponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/payment/"+ paymentId));

            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = paymentController.updatePayment(updatePaymentDto, paymentId);

            assertEquals(201, response.getStatusCodeValue());
            assertEquals(" Payment updated", response.getBody().getMessage());
            verify(ipaymentService, times(1)).updatePayment(updatePaymentDto, paymentId);
        }
    }

    @Test
    void deletePayment_returnsNotContent(){
        doNothing().when(ipaymentService).deletePayment(paymentId);

        ResponseEntity<Object> response = paymentController.deletePayment(paymentId);

        assertEquals(204, response.getStatusCodeValue());
        verify(ipaymentService,times(1)).deletePayment(paymentId);
    }
}
