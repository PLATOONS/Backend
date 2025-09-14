package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.platoons.e_commerce.dto.CreateProductRequestDto;
import com.platoons.e_commerce.dto.FetchProductResponseDto;
import com.platoons.e_commerce.entity.Product;
import com.platoons.e_commerce.entity.ProductImage;
import com.platoons.e_commerce.entity.Category;
import com.platoons.e_commerce.entity.ExtraInfo;
import com.platoons.e_commerce.mapper.ProductMapper;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class ProductMapperTests {
    @Test
    void testmapCreateProductRequestDtoToProduct() {
        CreateProductRequestDto dto = new CreateProductRequestDto();
        dto.setName("Product_1");
        dto.setDiscount(0.05);
        dto.setMeasurements("Lorem Ipsum");
        dto.setDescription("Lorem Ipsum");
        dto.setPrice(100.00);
        dto.setStockQuantity(100);

        Product product = new Product();

        Product result = ProductMapper.mapCreateProductRequestDtoToProduct(dto, product);

        assertEquals("Product_1", result.getName());
        assertEquals(0.05, result.getDiscount());
        assertEquals(5.00, result.getDiscountAmount());
        assertEquals("Lorem Ipsum", result.getMeasurements());
        assertEquals("Lorem Ipsum", result.getDescription());
        assertEquals(100.00, result.getPrice());
        assertEquals(100, result.getStockQuantity());
    }

    @Test
    void testMapProductToFetchProductResponseDto() {
        Category category = new Category();
        category.setName("Electronics");

        ProductImage image1 = new ProductImage();
        image1.setColor("Red");
        ProductImage image2 = new ProductImage();
        image2.setColor("Blue");

        Set<ProductImage> images = new HashSet<>();
        images.add(image1);
        images.add(image2);

        ExtraInfo extraInfo = new ExtraInfo();
        extraInfo.setPackaging("Box");
        extraInfo.setWeight("1.5");
        extraInfo.setHeight("10.0");
        extraInfo.setWidth("5.0");
        extraInfo.setInformation("Extra info test");

        Product product = new Product();
        product.setProductId("123");
        product.setName("Smartphone");
        product.setCategory(category);
        product.setPrice(500.0);
        product.setStockQuantity(50);
        product.setDiscount(0.1);
        product.setDiscountAmount(50.0);
        product.setMeasurements("10x5x2");
        product.setDescription("Latest smartphone");
        product.setExtraInfo(extraInfo);
        product.setProductImages(images);

        FetchProductResponseDto dto = new FetchProductResponseDto();

        FetchProductResponseDto result = ProductMapper.mapProductToFetchProductResponseDto(product, dto);

        assertEquals("123", result.getProductId());
        assertEquals("Electronics", result.getCategory());
        assertEquals(500.0, result.getPrice());
        assertEquals(50, result.getStockQuantity());
        assertEquals(0.1, result.getDiscount());
        assertEquals(50.0, result.getDiscountAmount());
        assertEquals("10x5x2", result.getMeasurements());
        assertEquals("Latest smartphone", result.getDescription());
        assertEquals("Box", result.getPackaging());
        assertEquals("1.5", result.getWeight());
        assertEquals("10.0", result.getHeight());
        assertEquals("5.0", result.getWidth());
        assertEquals("Extra info test", result.getInformation());
        assertEquals(2, result.getProductImages().size());
    }
}
