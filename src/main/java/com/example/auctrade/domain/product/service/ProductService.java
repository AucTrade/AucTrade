package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductDto;

public interface ProductService {
    ProductDto.Get createProduct(ProductDto.Create productDto, Long userId);

    ProductDto.Get getProduct(Long productId);

}
