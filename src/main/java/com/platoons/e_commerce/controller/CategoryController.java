package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreateCategoryRequestDto;
import com.platoons.e_commerce.dto.FetchCategoryResponseDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    public ResponseEntity<GenericResponseDto> createCategory(@Valid @RequestBody CreateCategoryRequestDto category) {
        log.info("Creating category");

        Long categoryId = categoryService.createCategory(category);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/category/{id}")
                .buildAndExpand(categoryId)
                .toUri();

        log.info("Category created with id {}", categoryId);
        return ResponseEntity.created(uri)
                .body(new GenericResponseDto("Successfully created category"));
    }

    @GetMapping
    public ResponseEntity<List<FetchCategoryResponseDto>> fetchAllCategories() {
        log.info("Fetching all categories");
        return ResponseEntity.ok(categoryService.fetchAllCategories());
    }
}
