package com.example.auctrade.domain.limit.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.global.valid.LimitValidationGroups;
import com.example.auctrade.global.valid.PurchaseValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
		private Long price;

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
		private Long id;
		private String title;
		private String introduce;
		private Long price;
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
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Purchase {
		private int quantity; // 거래 수량
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class LimitTradeRequest {
		private int quantity; // 거래 수량
		private Long buyer; // 구매 회원 ID
		private Long postId; // 게시글 ID (Auction 또는 Limit의 ID)
		private Boolean isAuction; // 게시글 타입 (Auction이면 true, Limit이면 false)
	}

	@Getter
	@AllArgsConstructor
	public static class GetPage {
		private final List<LimitDTO.Get> limits;
		private final Long maxPage;
	}
}