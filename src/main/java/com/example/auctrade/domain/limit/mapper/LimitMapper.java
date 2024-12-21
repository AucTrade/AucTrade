package com.example.auctrade.domain.limit.mapper;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.product.dto.ProductDto;

public class LimitMapper {

	// DTO -> Entity (Create)
	public static Limits toEntity(LimitDTO.Create limitDTO, Long productId, Long sellerId) {
		return (limitDTO == null || productId == null || sellerId == null) ? null : Limits.builder()
			.title(limitDTO.getTitle())
			.introduce(limitDTO.getIntroduce())
			.price(limitDTO.getPrice())
			.saleDate(limitDTO.getSaleDate())
			.personalLimit(limitDTO.getPersonalLimit())
			.status(0)
			.amount(limitDTO.getAmount())
			.productId(productId)
			.sellerId(sellerId)
			.build();
	}

	// Entity -> DTO (Get)
	public static LimitDTO.Get toDto(Limits limits, ProductDto.Get product, String seller) {
		return (limits == null || product == null || seller == null) ? null : LimitDTO.Get.builder()
			.id(limits.getId())
			.title(limits.getTitle())
			.introduce(limits.getIntroduce())
			.price(limits.getPrice())
			.saleDate(limits.getSaleDate())
			.personalLimit(limits.getPersonalLimit())
			.productName(product.getName())
			.productDetail(product.getDetail())
			.productCategory(product.getCategoryName())
			.sellerId(limits.getSellerId())
			.seller(seller)
			.amount(limits.getAmount())
			.files(product.getFiles())
			.thumbnail(product.getFiles().get(0))
			.created(limits.getCreated())
			.build();
	}
	//
	// // DTO -> Trade DTO (LimitTradeRequest 생성)
	// public static LimitDTO.LimitTradeRequest toTradeDto(LimitDTO.Purchase purchaseDto, Long buyerId, Long limitId, Long sellerId) {
	// 	return (purchaseDto == null || buyerId == null || limitId == null || sellerId == null) ? null : LimitDTO.LimitTradeRequest.builder()
	// 		.quantity(purchaseDto.getQuantity())
	// 		.buyerId(buyerId) // 변경: buyerId 사용
	// 		.sellerId(sellerId) // 변경: sellerId 사용
	// 		.postId(limitId)
	// 		.isAuction(false) // Limit 거래이므로 false로 설정
	// 		.build();
	// }
}
