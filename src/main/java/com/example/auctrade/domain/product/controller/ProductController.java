package com.example.auctrade.domain.product.controller;

import com.example.auctrade.domain.product.dto.ProductCategoryDto;
import com.example.auctrade.domain.product.dto.ProductDto;
import com.example.auctrade.domain.product.service.ProductCategoryService;
import com.example.auctrade.domain.product.service.ProductServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductServiceImpl productService;
    private final ProductCategoryService productCategoryService;

    // 상품 생성
    @PostMapping
    public ResponseEntity<ProductDto.Create> addProduct(@RequestBody ProductDto.Create dto, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles) throws IOException {
        //ProductDTO.Create productDTO = productService.create(dto, imgFiles);
        return ResponseEntity.ok(null);
    }

    // 상품 카테고리 생성
    @PostMapping("/category")
    public ResponseEntity<ProductCategoryDto.Get> addProductCategory(@RequestBody ProductCategoryDto.Create requestDto) {
        return ResponseEntity.ok(productCategoryService.create(requestDto));
    }

    @GetMapping("/category")
    public ResponseEntity<ProductCategoryDto.GetAll> getAllProductCategory() {
        return ResponseEntity.ok(productCategoryService.getAllCategory());
    }
}
