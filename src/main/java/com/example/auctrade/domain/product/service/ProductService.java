package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;

    // 상품 생성
    public ProductDTO create(ProductDTO productDTO) {
        User user = userRepository.findById(1L).orElseThrow();
        ProductCategory category =
                productCategoryRepository.findById(productDTO.getProductCategoryId()).orElseThrow();

        Product product = ProductMapper.toEntity(productDTO, category, user);
        productRepository.save(product);

        return ProductMapper.toDTO(product);
    }
}
