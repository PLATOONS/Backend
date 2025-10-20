package com.platoons.e_commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platoons.e_commerce.controller.CategoryController;
import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;
import com.platoons.e_commerce.service.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICategoryService categoryService;

    private CreateCategoryRequestDto createCategoryRequestDto;
    private FetchCategoryResponseDto fetchCategoryResponseDto1;
    private FetchCategoryResponseDto fetchCategoryResponseDto2;

    @BeforeEach
    void setUp() {
        createCategoryRequestDto = new CreateCategoryRequestDto();
        createCategoryRequestDto.setName("Electronics");
        createCategoryRequestDto.setDescription("Electronic items and gadgets");

        fetchCategoryResponseDto1 = new FetchCategoryResponseDto();
        fetchCategoryResponseDto1.setCategoryId(1L);
        fetchCategoryResponseDto1.setName("Electronics");
        fetchCategoryResponseDto1.setDescription("Electronic items");

        fetchCategoryResponseDto2 = new FetchCategoryResponseDto();
        fetchCategoryResponseDto2.setCategoryId(2L);
        fetchCategoryResponseDto2.setName("Clothing");
        fetchCategoryResponseDto2.setDescription("Apparel and fashion");
    }

    @Test
    void testCreateCategory_Success() throws Exception {
        // Arrange
        Long categoryId = 1L;
        when(categoryService.createCategory(any(CreateCategoryRequestDto.class)))
                .thenReturn(categoryId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/v1/category/1")))
                .andExpect(jsonPath("$.message").value("Successfully created category"));

        verify(categoryService, times(1)).createCategory(any(CreateCategoryRequestDto.class));
    }

    @Test
    void testCreateCategory_WithValidationError() throws Exception {
        // Arrange - Create DTO with invalid data
        CreateCategoryRequestDto invalidDto = new CreateCategoryRequestDto();

        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategory_ServiceThrowsException() throws Exception {
        // Arrange
        when(categoryService.createCategory(any(CreateCategoryRequestDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryRequestDto)))
                .andExpect(status().isInternalServerError());

        verify(categoryService, times(1)).createCategory(any(CreateCategoryRequestDto.class));
    }

    @Test
    void testCreateCategory_WithMalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Electronics\", \"invalid\": \"json\"}"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CreateCategoryRequestDto.class));
    }

    @Test
    void testCreateCategory_WithNullBody() throws Exception {
        // Arrange
        when(categoryService.createCategory(any())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFetchAllCategories_Success() throws Exception {
        // Arrange
        List<FetchCategoryResponseDto> categories = Arrays.asList(
                fetchCategoryResponseDto1,
                fetchCategoryResponseDto2
        );
        when(categoryService.fetchAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].description").value("Electronic items"))
                .andExpect(jsonPath("$[1].categoryId").value(2))
                .andExpect(jsonPath("$[1].name").value("Clothing"))
                .andExpect(jsonPath("$[1].description").value("Apparel and fashion"));

        verify(categoryService, times(1)).fetchAllCategories();
    }

    @Test
    void testFetchAllCategories_EmptyList() throws Exception {
        // Arrange
        when(categoryService.fetchAllCategories()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));

        verify(categoryService, times(1)).fetchAllCategories();
    }

    @Test
    void testFetchAllCategories_SingleCategory() throws Exception {
        // Arrange
        List<FetchCategoryResponseDto> categories = Collections.singletonList(fetchCategoryResponseDto1);
        when(categoryService.fetchAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        verify(categoryService, times(1)).fetchAllCategories();
    }

    @Test
    void testFetchAllCategories_ServiceThrowsException() throws Exception {
        // Arrange
        when(categoryService.fetchAllCategories())
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(categoryService, times(1)).fetchAllCategories();
    }

    @Test
    void testFetchAllCategories_WithWrongHttpMethod() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/category/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(categoryService, never()).fetchAllCategories();
    }

    @Test
    void testCreateCategory_VerifyLocationHeader() throws Exception {
        // Arrange
        Long categoryId = 42L;
        when(categoryService.createCategory(any(CreateCategoryRequestDto.class)))
                .thenReturn(categoryId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/v1/category/42")));

        verify(categoryService, times(1)).createCategory(any(CreateCategoryRequestDto.class));
    }

    @Test
    void testCreateCategory_VerifyResponseBody() throws Exception {
        // Arrange
        when(categoryService.createCategory(any(CreateCategoryRequestDto.class)))
                .thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.message").value("Successfully created category"));

        verify(categoryService, times(1)).createCategory(any(CreateCategoryRequestDto.class));
    }

    @Test
    void testFetchAllCategories_VerifyContentType() throws Exception {
        // Arrange
        when(categoryService.fetchAllCategories()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/category"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(categoryService, times(1)).fetchAllCategories();
    }
}