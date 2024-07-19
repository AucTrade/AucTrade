package com.example.auctrade.domain.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCategoryDTO {
    private String category;

    public ProductCategoryDTO(String category) {
        this.category = category;
    }
}
