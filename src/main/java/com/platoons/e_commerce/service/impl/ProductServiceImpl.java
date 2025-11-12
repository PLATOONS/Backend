package com.platoons.e_commerce.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.platoons.e_commerce.service.IS3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.platoons.e_commerce.dto.CreateProductRequestDto;
import com.platoons.e_commerce.dto.FetchProductResponseDto;
import com.platoons.e_commerce.dto.ProductSummaryDto;
import com.platoons.e_commerce.entity.Category;
import com.platoons.e_commerce.entity.ExtraInfo;
import com.platoons.e_commerce.entity.Product;
import com.platoons.e_commerce.entity.ProductImage;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.ProductMapper;
import com.platoons.e_commerce.repository.CategoryRepository;
import com.platoons.e_commerce.repository.ExtraInfoRepository;
import com.platoons.e_commerce.repository.ProductImageRepository;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.service.IProductService;
import com.platoons.e_commerce.utils.ImageUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ImageUtils imageUtils;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ExtraInfoRepository extraInfoRepository;
    private final IS3Service s3Service;

    @Override
    public Page<ProductSummaryDto> fetchProducts(Pageable pageable, String category, String min, String max) {
        if (min == null){
            min = "0";
        }

        if (max == null){
            max = "999999";
        }

        double minPrice = Double.parseDouble(min);
        double maxPrice = Double.parseDouble(max);

        if (minPrice > maxPrice){
            throw new BadRequestException("Min price is greater than max price");
        }

        return productRepository.findAllSummaries(pageable, category, minPrice, maxPrice)
                .map(summary -> {
                    String imageUrl = s3Service.getFileUrl(summary.getImageUrl());
                    summary.setImageUrl(imageUrl);
                    return summary;
                });
    }

    @Override
    public FetchProductResponseDto fetchProduct(String productId) {
        Product savedProduct = productRepository
                .findByProductIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "product", "productId", productId));

        return ProductMapper.mapProductToFetchProductResponseDto(
                savedProduct, new FetchProductResponseDto(), s3Service);
    }

    private String saveProduct(MultipartFile[] images, CreateProductRequestDto productDto, Product product){
        List<String> colors = productDto.getColors();

        // Checks that there is the same amount of colors and images
        if (images.length != colors.size())
            throw new BadRequestException(
                    "Quantity of images sent does not equal amount of colors sent");

        // Maps the dto to the entity
        ProductMapper.mapCreateProductRequestDtoToProduct(productDto, product);

        ExtraInfo extraInfo = new ExtraInfo();
        extraInfo.setInformation(productDto.getDescription());
        product.setExtraInfo(extraInfo);

        // Finds the category
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "category", "categoryId", productDto.getCategoryId().toString()));

        product.setCategory(category);

        // Checks if each of the files sent are images
        List<String> imagesName = new ArrayList<>();
        for (MultipartFile image : images) {
            if (imageUtils.fileIsImage(image)) {
                String fileName = null;
                try {
                    fileName = s3Service.uploadFile(image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                imagesName.add(fileName);
            }
            else
                throw new BadRequestException("At least one of the files is not an image");
        }

        // Saves the product
        log.info("Saving product");
        Product savedProduct = productRepository.save(product);
        extraInfo.setProduct(savedProduct);
        log.info("Saving extra info");
        extraInfoRepository.save(extraInfo);

        // Saves the images
        log.info("Saving product images");
        for (int i = 0; i < imagesName.size(); i++) {
            String imageName = imagesName.get(i);

            ProductImage productImage = new ProductImage();
            productImage.setProduct(savedProduct);
            productImage.setImageName(imageName);

            // Checks if the value of color is blank, if it isn't sets the color
            String color = colors.get(i);
            if (color != null && !color.isEmpty())
                productImage.setColor(color);

            productImageRepository.save(productImage);
        }

        return savedProduct.getProductId();
    }

    @Override
    public String createProduct(MultipartFile[] images, CreateProductRequestDto productDto) {
        return saveProduct(images, productDto, new Product());
    }

    @Override
    public void deleteProduct(String productId) {
        var optionalProduct = productRepository.findById(productId);

        // Early return for products that don't exist
        if (optionalProduct.isEmpty())
            return;

        var savedProduct = optionalProduct.get();
        savedProduct.setDeletedAt(LocalDateTime.now());
        productRepository.save(savedProduct);
    }

    @Override
    public String updateProduct(MultipartFile[] images, CreateProductRequestDto productDto, String productId) {
        // Checks that the product exists
        var savedProduct = productRepository.findByProductIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "product", "productId", productId));

        return saveProduct(images, productDto, savedProduct);
    }
}
