package com.platoons.e_commerce.mapper;

import com.platoons.e_commerce.dto.ProductImageDto;
import com.platoons.e_commerce.service.IS3Service;

public class ProductImageMapper {

    public static ProductImageDto mapProductImageToProductImageDto(
            com.platoons.e_commerce.entity.ProductImage productImage,
            IS3Service s3Service) {
        ProductImageDto productImageDto = new ProductImageDto();
        productImageDto.setColor(productImage.getColor());
        productImageDto.setImageUrl(s3Service.getFileUrl(productImage.getImageName()));
        return productImageDto;
    }
}
