package com.example.auctrade.domain.product.mapper;

import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.user.entity.User;

public class ProductMapper {

    public static ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDTO(
                product.getName(),
                product.getDetail(),
                product.getCategory() != null ? product.getCategory().getId() : null
        );
    }

    public static Product toEntity(ProductDTO productDTO, ProductCategory category, User user) {
        return (productDTO == null) ? null : new Product(productDTO.getName(), productDTO.getDetail(), category, user);
    }
}
