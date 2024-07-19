package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductCategoryDTO;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryDTO create(ProductCategoryDTO productCategoryDTO) {
        ProductCategory category = new ProductCategory(productCategoryDTO.getCategory());
        productCategoryRepository.save(category);

        return productCategoryDTO;
    }
}
