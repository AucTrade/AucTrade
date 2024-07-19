package com.example.auctrade.domain.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private String name;
    private String detail;
    private Long productCategoryId;

    public ProductDTO(String name, String detail, Long productCategoryId) {
        this.name = name;
        this.detail = detail;
        this.productCategoryId = productCategoryId;
    }
}
