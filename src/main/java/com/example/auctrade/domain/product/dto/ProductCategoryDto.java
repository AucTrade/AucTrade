package com.example.auctrade.domain.product.dto;

import com.example.auctrade.global.valid.ProductCategoryValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductCategoryDto {

    private ProductCategoryDto(){}

    @Getter
    @AllArgsConstructor
    public static class Create {

        @NotBlank(message = "카테고리 명을 입력해주세요.", groups = ProductCategoryValidationGroups.NameBlankGroup.class)
        private String name;
    }

    @Getter
    @Builder
    public static class Get {
        private Long categoryId;
        private String name;
    }

    @Getter
    @Builder
    public static class GetAll {
        private List<ProductCategoryDto.Get> categoryList;
    }
}
