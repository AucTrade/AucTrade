package com.example.auctrade.domain.product.controller;

import com.example.auctrade.domain.product.dto.ProductCategoryDTO;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.service.ProductCategoryService;
import com.example.auctrade.domain.product.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;

    // 상품 생성
    @PostMapping
    public ResponseEntity<ProductDTO.Create> addProduct(@RequestBody ProductDTO.Create dto, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles) throws IOException {
        //ProductDTO.Create productDTO = productService.create(dto, imgFiles);
        return ResponseEntity.ok(null);
    }

    // 상품 카테고리 생성
    @PostMapping("/category")
    public ResponseEntity<ProductCategoryDTO> addProductCategory(@RequestBody ProductCategoryDTO dto) {
        ProductCategoryDTO productCategoryDTO = productCategoryService.create(dto);
        return ResponseEntity.ok(productCategoryDTO);
    }
}
