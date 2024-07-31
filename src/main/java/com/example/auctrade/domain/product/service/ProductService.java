package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final FileService fileService;

    // 상품 생성
    public ProductDTO.Create create(ProductDTO.Create productDTO, MultipartFile[] uploadFiles) throws IOException {
        User user = userRepository.findById(1L).orElseThrow();
        ProductCategory category =
                productCategoryRepository.findById(productDTO.getProductCategoryId()).orElseThrow();

        Product product = productRepository.save(ProductMapper.toEntity(productDTO, category, user));
        if(!fileService.uploadFile(uploadFiles, product.getId()))
            throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);
        return ProductMapper.toDTO(product);
    }

    // 상품 생성
    public ProductDTO.Get get(Long productId) throws IOException {
        User user = userRepository.findById(1L).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        return new ProductDTO.Get(product, fileService.getFiles(product.getId()));
    }
}
