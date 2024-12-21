package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductCategoryDto;

public interface ProductCategoryService {
    ProductCategoryDto.Get createCategory(ProductCategoryDto.Create productCategoryDto);

    ProductCategoryDto.GetAll getAllCategory();
}
