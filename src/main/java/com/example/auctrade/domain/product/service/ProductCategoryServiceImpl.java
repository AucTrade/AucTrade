package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductCategoryDto;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.mapper.ProductCategoryMapper;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryDto.Get create(ProductCategoryDto.Create productCategoryDto) {
        return ProductCategoryMapper.toGetDto(productCategoryRepository.save(ProductCategoryMapper.toEntity(productCategoryDto)));
    }

    @Override
    public ProductCategoryDto.GetAll getAllCategory() {
        List<ProductCategoryDto.Get> categoryList = productCategoryRepository.findAll().stream().map(ProductCategoryMapper::toGetDto).toList();
        return ProductCategoryMapper.toGetAllDto(categoryList);
    }
}
