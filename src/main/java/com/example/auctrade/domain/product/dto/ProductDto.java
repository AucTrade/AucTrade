package com.example.auctrade.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDto {
    private ProductDto(){}
    @Builder
    @Getter
    @AllArgsConstructor
    public static class Create {
        private String name;
        private String detail;
        private Long productCategoryId;
    }
    @Builder
    @Getter
    public static class Get {
        private Long productId;
        private String name;
        private String detail;
        private String categoryName;
        private String saleUsername;
        private List<String> files;

        public void updateFiles(List<String> files) {
            this.files = files;
        }
    }
}
