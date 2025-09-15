package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreateProductRequestDto;
import com.platoons.e_commerce.dto.ErrorResponseDto;
import com.platoons.e_commerce.dto.FetchProductResponseDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller for managing product operations.
 */
@RestController
@RequestMapping("/api/v1/product")
@Transactional
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Controller", description = "APIs for managing products")
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "Get all products", description = "Retrieves a paginated list of all products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductRepository.ProductSummaryProjection.class))))
    })
    @PageableAsQueryParam
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ProductRepository.ProductSummaryProjection>> fetchProducts(
            @Parameter(hidden = true) Pageable pageable) {
        log.info("Fetching products page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return ResponseEntity.ok(productService.fetchProducts(pageable));
    }

    @Operation(summary = "Get product by ID", description = "Retrieves product details by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product",
                     content = @Content(schema = @Schema(implementation = FetchProductResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FetchProductResponseDto> fetchProduct(
            @Parameter(description = "ID of the product to be retrieved", required = true)
            @PathVariable String productId) {
        log.info("Fetching product {}", productId);
        return ResponseEntity.ok(productService.fetchProduct(productId));
    }

    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details and images")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponseDto> createProduct(
            @Parameter(description = "Product images", required = true,
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "array", implementation = MultipartFile.class)))
            @RequestPart(name = "image") MultipartFile[] images,
            
            @Parameter(description = "Product details", required = true,
                      content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateProductRequestDto.class)))
            @Valid @RequestPart(name = "productDto") CreateProductRequestDto productDto) {
        log.info("Creating product");
        String productId = productService.createProduct(images, productDto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/product/{id}")
                .buildAndExpand(productId)
                .toUri();

        log.info("Product created with id {}", productId);
        return ResponseEntity.created(uri)
                .body(new GenericResponseDto("Successfully created product"));
    }

    @Operation(summary = "Delete a product", description = "Removes a product and its associated images from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteProduct(
            @Parameter(description = "ID of the product to be deleted", required = true)
            @PathVariable String productId) {
        log.info("Deleting product {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update product information", description = "Updates the details and images of an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productId}")
    public ResponseEntity<GenericResponseDto> updateProduct(
            @Parameter(description = "Product images", required = true,
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "array", implementation = MultipartFile.class)))
            @RequestPart(name = "image") MultipartFile[] images,
            
            @Parameter(description = "Updated product details", required = true,
                      content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateProductRequestDto.class)))
            @Valid @RequestPart(name = "productDto") CreateProductRequestDto productDto,
            
            @Parameter(description = "ID of the product to be updated", required = true)
            @PathVariable String productId) {
        log.info("Updating product {}", productId);
        String productIdReturned = productService.updateProduct(images, productDto, productId);
        log.info("Updated product with id {}", productIdReturned);
        return ResponseEntity.ok(new GenericResponseDto("Successfully updated product"));
    }
}
