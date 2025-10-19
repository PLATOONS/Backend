package com.platoons.e_commerce;

import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;
import com.platoons.e_commerce.entity.Category;
import com.platoons.e_commerce.mapper.CategoryMapper;
import com.platoons.e_commerce.repository.CategoryRepository;
import com.platoons.e_commerce.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CreateCategoryRequestDto createCategoryRequestDto;
    private Category category;
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        // Setup test data
        createCategoryRequestDto = new CreateCategoryRequestDto();
        createCategoryRequestDto.setName("Electronics");
        createCategoryRequestDto.setDescription("Electronic items and gadgets");

        category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic items and gadgets");

        savedCategory = new Category();
        savedCategory.setCategoryId(1L);
        savedCategory.setName("Electronics");
        savedCategory.setDescription("Electronic items and gadgets");
    }

    @Test
    void testCreateCategory_Success() {
        // Arrange
        try (MockedStatic<CategoryMapper> mockedMapper = mockStatic(CategoryMapper.class)) {
            mockedMapper.when(() -> CategoryMapper.mapCreateCategoryRequestDtoToCategory(
                            any(CreateCategoryRequestDto.class), any(Category.class)))
                    .thenReturn(category);

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            // Act
            Long categoryId = categoryService.createCategory(createCategoryRequestDto);

            // Assert
            assertNotNull(categoryId);
            assertEquals(1L, categoryId);
            verify(categoryRepository, times(1)).save(any(Category.class));
            mockedMapper.verify(() -> CategoryMapper.mapCreateCategoryRequestDtoToCategory(
                    any(CreateCategoryRequestDto.class), any(Category.class)), times(1));
        }
    }

    @Test
    void testCreateCategory_WithNullDto() {
        // Arrange
        try (MockedStatic<CategoryMapper> mockedMapper = mockStatic(CategoryMapper.class)) {
            mockedMapper.when(() -> CategoryMapper.mapCreateCategoryRequestDtoToCategory(
                            isNull(), any(Category.class)))
                    .thenReturn(new Category());

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            // Act
            Long categoryId = categoryService.createCategory(null);

            // Assert
            assertNotNull(categoryId);
            assertEquals(1L, categoryId);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }
    }

    @Test
    void testFetchAllCategories_Success() {
        // Arrange
        Category category1 = new Category();
        category1.setCategoryId(1L);
        category1.setName("Electronics");
        category1.setDescription("Electronic items");

        Category category2 = new Category();
        category2.setCategoryId(2L);
        category2.setName("Clothing");
        category2.setDescription("Apparel and fashion");

        List<Category> categories = Arrays.asList(category1, category2);

        FetchCategoryResponseDto responseDto1 = new FetchCategoryResponseDto();
        responseDto1.setCategoryId(1L);
        responseDto1.setName("Electronics");

        FetchCategoryResponseDto responseDto2 = new FetchCategoryResponseDto();
        responseDto2.setCategoryId(2L);
        responseDto2.setName("Clothing");

        try (MockedStatic<CategoryMapper> mockedMapper = mockStatic(CategoryMapper.class)) {
            when(categoryRepository.findAll()).thenReturn(categories);

            mockedMapper.when(() -> CategoryMapper.mapCategoryToFetchCategoryResponseDto(
                            eq(category1), any(FetchCategoryResponseDto.class)))
                    .thenReturn(responseDto1);

            mockedMapper.when(() -> CategoryMapper.mapCategoryToFetchCategoryResponseDto(
                            eq(category2), any(FetchCategoryResponseDto.class)))
                    .thenReturn(responseDto2);

            // Act
            List<FetchCategoryResponseDto> result = categoryService.fetchAllCategories();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Electronics", result.get(0).getName());
            assertEquals("Clothing", result.get(1).getName());
            verify(categoryRepository, times(1)).findAll();
            mockedMapper.verify(() -> CategoryMapper.mapCategoryToFetchCategoryResponseDto(
                    any(Category.class), any(FetchCategoryResponseDto.class)), times(2));
        }
    }

    @Test
    void testFetchAllCategories_EmptyList() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<CategoryMapper> mockedMapper = mockStatic(CategoryMapper.class)) {
            // Act
            List<FetchCategoryResponseDto> result = categoryService.fetchAllCategories();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository, times(1)).findAll();
            mockedMapper.verify(() -> CategoryMapper.mapCategoryToFetchCategoryResponseDto(
                    any(Category.class), any(FetchCategoryResponseDto.class)), never());
        }
    }

    @Test
    void testFetchAllCategories_SingleCategory() {
        // Arrange
        Category category1 = new Category();
        category1.setCategoryId(1L);
        category1.setName("Books");

        FetchCategoryResponseDto responseDto1 = new FetchCategoryResponseDto();
        responseDto1.setCategoryId(1L);
        responseDto1.setName("Books");

        try (MockedStatic<CategoryMapper> mockedMapper = mockStatic(CategoryMapper.class)) {
            when(categoryRepository.findAll()).thenReturn(Collections.singletonList(category1));

            mockedMapper.when(() -> CategoryMapper.mapCategoryToFetchCategoryResponseDto(
                            any(Category.class), any(FetchCategoryResponseDto.class)))
                    .thenReturn(responseDto1);

            // Act
            List<FetchCategoryResponseDto> result = categoryService.fetchAllCategories();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Books", result.get(0).getName());
            verify(categoryRepository, times(1)).findAll();
        }
    }

    @Test
    void testCreateCategory_RepositoryThrowsException() {
        // Arrange
        try (MockedStatic<CategoryMapper> mockedMapper = mockStatic(CategoryMapper.class)) {
            mockedMapper.when(() -> CategoryMapper.mapCreateCategoryRequestDtoToCategory(
                            any(CreateCategoryRequestDto.class), any(Category.class)))
                    .thenReturn(category);

            when(categoryRepository.save(any(Category.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                categoryService.createCategory(createCategoryRequestDto);
            });

            verify(categoryRepository, times(1)).save(any(Category.class));
        }
    }

    @Test
    void testFetchAllCategories_RepositoryThrowsException() {
        // Arrange
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            categoryService.fetchAllCategories();
        });

        verify(categoryRepository, times(1)).findAll();
    }
}