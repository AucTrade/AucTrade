package com.example.auctrade.domain.limit.dto;

import java.time.LocalDateTime;

import com.example.auctrade.global.vaild.LimitValidationGroups;
import com.example.auctrade.global.vaild.PurchaseValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class LimitDTO {

	@Builder
	@Getter
	@AllArgsConstructor
	public static class Create {

		@NotBlank(message = "제목을 입력해주세요.", groups = LimitValidationGroups.TitleBlankGroup.class)
		private String title;

		@NotBlank(message = "내용을 입력해주세요,", groups = LimitValidationGroups.IntroduceBlankGroup.class)
		private String introduce;

		@Min(value = 0, message = "가격은 최소 0원 이상입니다.", groups = LimitValidationGroups.PriceMinGroup.class)
		private int price;

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		private LocalDateTime saleDate;

		@Min(value = 1, message = "제한 수량은 최소 1개 이상이어야 합니다.", groups = LimitValidationGroups.LimitMinGroup.class)
		private int limit;

		@NotBlank(message = "경매 상품명을 입력해 주세요.", groups = LimitValidationGroups.ProductNameBlankGroup.class)
		private String productName;

		private String productDetail;

		@NotBlank(message = "상품의 카테고리를 입력해 주세요.", groups = LimitValidationGroups.ProductCategoryBlankGroup.class)
		private String productCategory;

		@NotBlank(message = "판매자의 이메일을 입력해 주세요.", groups = LimitValidationGroups.SaleUserEmailBlankGroup.class)
		private String saleUserEmail;
	}

	@Getter
	@Builder
	public static class Get {
		private String title;
		private String introduce;
		private int price;
		private LocalDateTime saleDate;
		private int limit;
		private String productName;
		private String productDetail;
		private String productCategory;
		private String saleUserEmail;
		private LocalDateTime created;
	}

	@Getter
	@Builder
	public static class Purchase {
		@NotBlank(message = "판매자의 이메일을 입력해 주세요.", groups = PurchaseValidationGroups.SaleUserEmailBlankGroup.class)
		private String saleUserEmail;

		@Min(value = 1, message = "구매 수량은 최소 1개 이상이어야 합니다.", groups = PurchaseValidationGroups.QuantityMinGroup.class)
		private int quantity;
	}
}