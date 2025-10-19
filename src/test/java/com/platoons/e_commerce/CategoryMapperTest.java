package com.platoons.e_commerce;

import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;
import com.platoons.e_commerce.entity.Category;
import com.platoons.e_commerce.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

    @Test
    void mapCreateCategoryRequestDtoToCategory_ShouldMapAllFields() {
        // Given
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto();
        requestDto.setName("Electronics");
        requestDto.setDescription("Electronic devices and accessories");

        Category category = new Category();

        // When
        Category result = CategoryMapper.mapCreateCategoryRequestDtoToCategory(requestDto, category);

        // Then
        assertNotNull(result);
        assertEquals(requestDto.getName(), result.getName());
        assertEquals(requestDto.getDescription(), result.getDescription());
    }

    @Test
    void mapCategoryToFetchCategoryResponseDto_ShouldMapAllFields() {
        // Given
        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic devices and accessories");
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        // When
        FetchCategoryResponseDto result = CategoryMapper.mapCategoryToFetchCategoryResponseDto(category, new FetchCategoryResponseDto());

        // Then
        assertNotNull(result);
        assertEquals(category.getCategoryId(), result.getCategoryId());
        assertEquals(category.getName(), result.getName());
        assertEquals(category.getDescription(), result.getDescription());
        assertEquals(category.getCreatedAt(), result.getCreatedAt());
        assertEquals(category.getUpdatedAt(), result.getUpdatedAt());
    }
}
