package com.example.auctrade.domain.product.mapper;

import com.example.auctrade.domain.product.dto.ProductCategoryDto;
import com.example.auctrade.domain.product.entity.ProductCategory;

import java.util.List;

public class ProductCategoryMapper {
    private ProductCategoryMapper(){}

    public static ProductCategoryDto.Get toGetDto(ProductCategory productCategory) {
        return (productCategory == null) ? null : ProductCategoryDto.Get.builder()
                .categoryId(productCategory.getId())
                .categoryName(productCategory.getCategoryName())
                .build();
    }

    public static ProductCategoryDto.GetAll toGetAllDto(List<ProductCategoryDto.Get> productCategoryList) {
        return (productCategoryList == null) ? null : ProductCategoryDto.GetAll.builder()
                .categoryList(productCategoryList)
                .build();
    }

    public static ProductCategory toEntity(ProductCategoryDto.Create productCategoryDto) {
        return (productCategoryDto == null) ? null : ProductCategory.builder()
                .categoryName(productCategoryDto.getCategoryName())
                .build();
    }
}
