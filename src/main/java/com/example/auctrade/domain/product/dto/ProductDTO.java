package com.example.auctrade.domain.product.dto;

import com.example.auctrade.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDTO {
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
    @AllArgsConstructor
        public static class Get {
        private String name;
        private String detail;
        private String categoryName;
        private List<String> files;

        public Get(Product product, List<String> files) {
            this.name = product.getName();
            this.detail = product.getDetail();
            this.categoryName = product.getCategory().getCategoryName();
            this.files = files;
        }
    }
}
