package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;

import java.util.List;

public interface ICategoryService {
    Long createCategory(CreateCategoryRequestDto categoryDto);
    List<FetchCategoryResponseDto> fetchAllCategories();
}
