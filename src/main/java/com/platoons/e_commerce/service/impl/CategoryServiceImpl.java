package com.platoons.e_commerce.service.impl;

import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;
import com.platoons.e_commerce.entity.Category;
import com.platoons.e_commerce.mapper.CategoryMapper;
import com.platoons.e_commerce.repository.CategoryRepository;
import com.platoons.e_commerce.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Long createCategory(CreateCategoryRequestDto categoryDto) {

        Category category = CategoryMapper.mapCreateCategoryRequestDtoToCategory(categoryDto, new Category());

        var savedCategory = categoryRepository.save(category);

        return savedCategory.getCategoryId();
    }

    @Override
    public List<FetchCategoryResponseDto> fetchAllCategories() {

        var categories = categoryRepository.findAll();

        List<FetchCategoryResponseDto> categoriesDto = new ArrayList<>();

        categories.forEach(cat -> categoriesDto
                .add(CategoryMapper.mapCategoryToFetchCategoryResponseDto(
                        cat, new FetchCategoryResponseDto())));

        return categoriesDto;
    }
}
