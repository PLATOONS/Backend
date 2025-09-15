package com.platoons.e_commerce;

import com.platoons.e_commerce.controller.ProductController;
import com.platoons.e_commerce.dto.CreateProductRequestDto;
import com.platoons.e_commerce.dto.FetchProductResponseDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ProductControllerTest {

    private IProductService productService;
    private ProductController productController;
    private String productId;

    @BeforeEach
    void setUp() {
        productService = mock(IProductService.class);
        productController = new ProductController(productService);
        productId = UUID.randomUUID().toString();
    }

    @Test
    void fetchProducts_returnsPagedProducts() {
        var pageable = PageRequest.of(0, 10);
        ProductRepository.ProductSummaryProjection projection = mock(ProductRepository.ProductSummaryProjection.class);
        Page<ProductRepository.ProductSummaryProjection> page = new PageImpl<>(Collections.singletonList(projection));

        when(productService.fetchProducts(pageable)).thenReturn(page);

        ResponseEntity<Page<ProductRepository.ProductSummaryProjection>> response = productController
                .fetchProducts(pageable);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
        verify(productService, times(1)).fetchProducts(pageable);
    }

    @Test
    void fetchProduct_returnsProductDetails() {
        FetchProductResponseDto dto = new FetchProductResponseDto();
        dto.setProductId(productId);
        dto.setName("Laptop");

        when(productService.fetchProduct(productId)).thenReturn(dto);

        ResponseEntity<FetchProductResponseDto> response = productController.fetchProduct(productId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Laptop", response.getBody().getName());
        verify(productService, times(1)).fetchProduct(productId);
    }

    @Test
    void createProduct_returnsCreatedResponse() {
        MultipartFile file = mock(MultipartFile.class);
        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setName("Phone");

        when(productService.createProduct(any(MultipartFile[].class), eq(dto))).thenReturn(productId);

        // Mockear ServletUriComponentsBuilder
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {

            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyString())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/product/" + productId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = productController.createProduct(new MultipartFile[] { file },
                    dto);

            assertEquals(201, response.getStatusCodeValue());
            assertEquals("Successfully created product", response.getBody().getMessage());
            verify(productService, times(1)).createProduct(any(MultipartFile[].class), eq(dto));
        }
    }

    @Test
    void updateProduct_returnsOkResponse() {
        MultipartFile file = mock(MultipartFile.class);
        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setName("Updated Phone");

        when(productService.updateProduct(any(MultipartFile[].class), eq(dto), eq(productId)))
                .thenReturn(productId);

        ResponseEntity<GenericResponseDto> response = productController.updateProduct(new MultipartFile[] { file }, dto,
                productId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully updated product", response.getBody().getMessage());
        verify(productService, times(1)).updateProduct(any(MultipartFile[].class), eq(dto), eq(productId));
    }

    @Test
    void deleteProduct_returnsNoContent() {
        doNothing().when(productService).deleteProduct(productId);

        ResponseEntity<Object> response = productController.deleteProduct(productId);

        assertEquals(204, response.getStatusCodeValue());
        verify(productService, times(1)).deleteProduct(productId);
    }
}
