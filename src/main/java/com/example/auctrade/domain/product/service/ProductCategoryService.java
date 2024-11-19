package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductCategoryDto;

public interface ProductCategoryService {
    ProductCategoryDto.Get create(ProductCategoryDto.Create productCategoryDto);

    ProductCategoryDto.GetAll getAllCategory();
}
