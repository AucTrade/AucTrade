package com.example.auctrade.domain.product.mapper;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.dto.ProductDto;

import java.util.List;

public class ProductMapper {
    private ProductMapper(){}

    public static ProductDto.Create toDTO(Product product) {
        return (product == null) ? null : ProductDto.Create.builder()
                .name(product.getName())
                .detail(product.getDetail())
                .productCategoryId(product.getId())
                .build();
    }

    public static ProductDto.Get toGetDto(Product product, List<String> files) {
        return (product == null) ? null : ProductDto.Get.builder()
                .productId(product.getId())
                .name(product.getName())
                .detail(product.getDetail())
                .categoryName(product.getCategory().getCategoryName())
                .files(files)
                .build();
    }

    public static Product toEntity(ProductDto.Create productDTO, Long userId, ProductCategory category) {
        return (productDTO == null) ? null : Product.builder()
                .name(productDTO.getName())
                .detail(productDTO.getDetail())
                .category(category)

                .userId(userId)
                .build();
    }
}
