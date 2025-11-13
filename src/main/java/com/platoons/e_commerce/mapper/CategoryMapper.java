package com.platoons.e_commerce.mapper;

import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;
import com.platoons.e_commerce.entity.Category;

public class CategoryMapper {
    public static Category mapCreateCategoryRequestDtoToCategory(CreateCategoryRequestDto categoryDto, Category category) {
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setTest(categoryDto.getTest());
        return category;
    }

    public static FetchCategoryResponseDto mapCategoryToFetchCategoryResponseDto(Category category, FetchCategoryResponseDto categoryDto) {
        categoryDto.setCategoryId(category.getCategoryId());
        categoryDto.setName(category.getName());
        categoryDto.setDescription(category.getDescription());
        categoryDto.setCreatedAt(category.getCreatedAt());
        categoryDto.setUpdatedAt(category.getUpdatedAt());
        categoryDto.setTest(category.getTest());
        return categoryDto;
    }
}
