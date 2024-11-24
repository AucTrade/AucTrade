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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j(topic = "ProductService")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository){
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }
    
    /**
     * 상품 생성
     * @param productDto 생성할 상품 정보
     * @param userId 대상 유저 ID
     * @return 파일 업로드 성공 여부
     */
    public ProductDto.Get createProduct(ProductDto.Create productDto, Long userId){
        return ProductMapper.toGetDto(productRepository.save(ProductMapper.toEntity(productDto, userId, findCategory(productDto.getProductCategoryId()))));
    }

    /**
     * 상품 조회
     * @param productId 상품 ID
     * @return 상품 정보
     */
    public ProductDto.Get getProduct(Long productId){
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
