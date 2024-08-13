package com.example.auctrade.domain.product.mapper;

import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.user.entity.User;

import java.util.List;

public class ProductMapper {
    private ProductMapper(){}

    public static ProductDTO.Create toDTO(Product product) {
        return (product == null) ? null : ProductDTO.Create.builder()
                .name(product.getName())
                .detail(product.getDetail())
                .productCategoryId(product.getId())
                .build();
    }
    public static ProductDTO.Get toGetDto(Product product) {
        return (product == null) ? null : ProductDTO.Get.builder()
                .name(product.getName())
                .detail(product.getDetail())
                .categoryName(product.getCategory().getCategoryName())
                .build();
    }

    public static Product toEntity(ProductDTO.Create productDTO, ProductCategory category) {
        return (productDTO == null) ? null : new Product(productDTO, category);
    }
}
