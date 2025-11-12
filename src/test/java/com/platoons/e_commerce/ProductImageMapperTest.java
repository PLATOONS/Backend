package com.platoons.e_commerce;

import org.junit.jupiter.api.Test;

import com.platoons.e_commerce.entity.ProductImage;
import com.platoons.e_commerce.dto.ProductImageDto;
import com.platoons.e_commerce.mapper.ProductImageMapper;
import org.springframework.test.context.ActiveProfiles;
import com.platoons.e_commerce.service.IS3Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ProductImageMapperTest {

    @Test
    void testmapProductImageToProductImageDto() {
        ProductImage productImage = new ProductImage();
        productImage.setColor("Red");
        productImage.setImageName("img_1");

        IS3Service s3Service = mock(IS3Service.class);
        when(s3Service.getFileUrl("img_1")).thenReturn("https://bucket/img_1");

        ProductImageDto result = ProductImageMapper.mapProductImageToProductImageDto(productImage, s3Service);

        assertEquals("Red", result.getColor());
        assertEquals("https://bucket/img_1", result.getImageUrl());
    }

    @Test
    void testConstructorProductImage() {
        @SuppressWarnings("unused")
        ProductImageMapper PIM = new ProductImageMapper();
    }
}
