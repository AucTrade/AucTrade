package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductCategoryDto;
import com.example.auctrade.domain.product.mapper.ProductCategoryMapper;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "Product Category Service")
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryServiceImpl(ProductCategoryRepository productCategoryRepository){
        this.productCategoryRepository = productCategoryRepository;
    }
    
    /**
     * 카테고리 생성
     * @param productCategoryDto 생성할 카테고리 정보
     * @return 생성된 카테고리 정보
     */
    public ProductCategoryDto.Get createCategory(ProductCategoryDto.Create productCategoryDto) {
        return ProductCategoryMapper.toGetDto(productCategoryRepository.save(ProductCategoryMapper.toEntity(productCategoryDto)));
    }

    /**
     * 카테고리 리스트 조회
     * @return 카테고리 리스트
     */
    @Override
    public ProductCategoryDto.GetAll getAllCategory() {
        List<ProductCategoryDto.Get> categoryList = productCategoryRepository.findAll().stream().map(ProductCategoryMapper::toGetDto).toList();
        return ProductCategoryMapper.toGetAllDto(categoryList);
    }
}
