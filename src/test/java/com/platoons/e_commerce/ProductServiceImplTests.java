package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.platoons.e_commerce.dto.CreateProductRequestDto;
import com.platoons.e_commerce.dto.FetchProductResponseDto;
import com.platoons.e_commerce.entity.Category;
import com.platoons.e_commerce.entity.Product;
import com.platoons.e_commerce.entity.ProductImage;
import com.platoons.e_commerce.entity.ExtraInfo;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.ProductMapper;
import com.platoons.e_commerce.repository.CategoryRepository;
import com.platoons.e_commerce.repository.ExtraInfoRepository;
import com.platoons.e_commerce.repository.ProductImageRepository;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.service.impl.ProductServiceImpl;
import com.platoons.e_commerce.utils.ImageUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public class ProductServiceImplTests {
    private ImageUtils imageUtils;
    private ProductRepository productRepository;
    private ProductImageRepository productImageRepository;
    private CategoryRepository categoryRepository;
    private ExtraInfoRepository extraInfoRepository;
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        imageUtils = mock(ImageUtils.class);
        productRepository = mock(ProductRepository.class);
        productImageRepository = mock(ProductImageRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        extraInfoRepository = mock(ExtraInfoRepository.class);

        productService = new ProductServiceImpl(
                imageUtils,
                productRepository,
                productImageRepository,
                categoryRepository,
                extraInfoRepository);
    }

    @Test
    void testFetchProducts_WithAllParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, "Electronics", 100.0, 500.0))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, "Electronics", "100", "500");

        assertNotNull(result);
        assertEquals(mockPage, result);
        verify(productRepository, times(1)).findAllSummaries(pageable, "Electronics", 100.0, 500.0);
    }

    @Test
    void testFetchProducts_WithNullMinPrice_ShouldUseDefaultZero() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, "Books", 0.0, 200.0))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, "Books", null, "200");

        assertNotNull(result);
        verify(productRepository, times(1)).findAllSummaries(pageable, "Books", 0.0, 200.0);
    }

    @Test
    void testFetchProducts_WithNullMaxPrice_ShouldUseDefault999999() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, "Clothing", 50.0, 999999.0))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, "Clothing", "50", null);

        assertNotNull(result);
        verify(productRepository, times(1)).findAllSummaries(pageable, "Clothing", 50.0, 999999.0);
    }

    @Test
    void testFetchProducts_WithBothPricesNull_ShouldUseDefaults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, null, 0.0, 999999.0))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, null, null, null);

        assertNotNull(result);
        verify(productRepository, times(1)).findAllSummaries(pageable, null, 0.0, 999999.0);
    }

    @Test
    void testFetchProducts_WithMinGreaterThanMax_ShouldThrowBadRequestException() {
        Pageable pageable = PageRequest.of(0, 10);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> productService.fetchProducts(pageable, "Electronics", "500", "100"));

        assertEquals("Min price is greater than max price", exception.getMessage());
        verify(productRepository, never()).findAllSummaries(any(), any(), anyDouble(), anyDouble());
    }

    @Test
    void testFetchProducts_WithInvalidMinPrice_ShouldThrowNumberFormatException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(NumberFormatException.class,
                () -> productService.fetchProducts(pageable, "Electronics", "invalid", "100"));

        verify(productRepository, never()).findAllSummaries(any(), any(), anyDouble(), anyDouble());
    }

    @Test
    void testFetchProducts_WithInvalidMaxPrice_ShouldThrowNumberFormatException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(NumberFormatException.class,
                () -> productService.fetchProducts(pageable, "Electronics", "50", "not-a-number"));

        verify(productRepository, never()).findAllSummaries(any(), any(), anyDouble(), anyDouble());
    }

    @Test
    void testFetchProducts_WithNullCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, null, 0.0, 1000.0))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, null, "0", "1000");

        assertNotNull(result);
        verify(productRepository, times(1)).findAllSummaries(pageable, null, 0.0, 1000.0);
    }

    @Test
    void testFetchProducts_WithEqualMinAndMaxPrices() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, "Sports", 100.0, 100.0))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, "Sports", "100", "100");

        assertNotNull(result);
        verify(productRepository, times(1)).findAllSummaries(pageable, "Sports", 100.0, 100.0);
    }

    @Test
    void testFetchProducts_WithDecimalPrices() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductRepository.ProductSummaryProjection> mockPage = new PageImpl<>(List.of());

        when(productRepository.findAllSummaries(pageable, "Home", 19.99, 299.99))
                .thenReturn(mockPage);

        Page<ProductRepository.ProductSummaryProjection> result = productService.fetchProducts(
                pageable, "Home", "19.99", "299.99");

        assertNotNull(result);
        verify(productRepository, times(1)).findAllSummaries(pageable, "Home", 19.99, 299.99);
    }

    @Test
    void testFetchProduct_Found() {
        Product product = new Product();
        product.setProductId("p1");
        FetchProductResponseDto dto = new FetchProductResponseDto();

        when(productRepository.findByProductIdAndDeletedAtIsNull("p1"))
                .thenReturn(Optional.of(product));

        try (MockedStatic<ProductMapper> mockedMapper = mockStatic(ProductMapper.class)) {
            mockedMapper
                    .when(() -> ProductMapper.mapProductToFetchProductResponseDto(eq(product),
                            any(FetchProductResponseDto.class)))
                    .thenReturn(dto);

            FetchProductResponseDto result = productService.fetchProduct("p1");

            assertNotNull(result);
            assertEquals(dto, result);
            verify(productRepository, times(1)).findByProductIdAndDeletedAtIsNull("p1");
        }
    }

    @Test
    void testFetchProduct_NotFound() {
        when(productRepository.findByProductIdAndDeletedAtIsNull("pX"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.fetchProduct("pX"));
        verify(productRepository, times(1)).findByProductIdAndDeletedAtIsNull("pX");
    }

    @Test
    void testCreateProduct_Success() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("img.png");
        when(imageUtils.fileIsImage(file)).thenReturn(true);

        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setColors(List.of("red"));
        dto.setCategoryId(1L);
        dto.setDescription("desc");

        Category category = new Category();
        Product product = new Product();
        product.setProductId("p1");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        String result = productService.createProduct(new MultipartFile[] { file }, dto);

        assertEquals("p1", result);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productImageRepository, times(1)).save(any(ProductImage.class));
        verify(extraInfoRepository, times(1)).save(any(ExtraInfo.class));
    }

    @Test
    void testCreateProduct_ImageCountMismatch_ShouldThrow() {
        MultipartFile file = mock(MultipartFile.class);

        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setColors(List.of("red", "blue")); // 2 colors, 1 image

        assertThrows(BadRequestException.class,
                () -> productService.createProduct(new MultipartFile[] { file }, dto));
    }

    @Test
    void testUpdateProduct_Found() {
        Product product = new Product();
        product.setProductId("p1");

        when(productRepository.findByProductIdAndDeletedAtIsNull("p1"))
                .thenReturn(Optional.of(product));

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("img.png");
        when(imageUtils.fileIsImage(file)).thenReturn(true);

        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setColors(List.of("black"));
        dto.setCategoryId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(product)).thenReturn(product);

        String result = productService.updateProduct(new MultipartFile[] { file }, dto, "p1");

        assertEquals("p1", result);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(productRepository.findByProductIdAndDeletedAtIsNull("pX"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productService.updateProduct(new MultipartFile[] {}, new CreateProductRequestDto(), "pX"));
    }

    @Test
    void testDeleteProduct_Found() {
        Product product = new Product();
        product.setProductId("p1");

        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.deleteProduct("p1");

        assertNotNull(product.getDeletedAt());
        verify(productRepository, times(1)).findById("p1");
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.findById("pX")).thenReturn(Optional.empty());

        productService.deleteProduct("pX");

        verify(productRepository, times(1)).findById("pX");
        verify(productRepository, never()).save(any());
    }
}
