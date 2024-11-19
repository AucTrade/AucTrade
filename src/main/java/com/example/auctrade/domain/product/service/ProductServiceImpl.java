package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductDto;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "ProductService")
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductDto.Get create(ProductDto.Create productDto, Long userId){

        return ProductMapper.toGetDto(productRepository.save(ProductMapper.toEntity(productDto, userId, findCategory(productDto.getProductCategoryId()))));
    }
    public ProductDto.Get get(Long productId){
        return ProductMapper.toGetDto(findProduct(productId));
    }
    private Product findProduct(long id){
        return productRepository.findById(id).orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductCategory findCategory(long id){
        return productCategoryRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }
}
