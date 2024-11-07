package com.example.auctrade.domain.limit.mapper;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.user.entity.User;

public class LimitMapper {

	// DTO -> Entity (Create)
	public static Limits toEntity(LimitDTO.Create limitDTO, long productId, String seller) {
		return (limitDTO == null) ? null : Limits.builder()
			.title(limitDTO.getTitle())
			.introduce(limitDTO.getIntroduce())
			.price(limitDTO.getPrice())
			.saleDate(limitDTO.getSaleDate())
			.personalLimit(limitDTO.getPersonalLimit())
			.amount(limitDTO.getAmount()) // 한정 수량으로 초기 설정
			.productId(productId)
			.seller(seller)
			.build();
	}
	public static LimitDTO.Get toDto(Limits limits, ProductDTO.Get product, String seller) {
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
			.seller(seller)
			.created(limits.getCreated())
			.build();
	}


	// DTO -> Trade DTO (LimitTradeRequest 생성)
	public static LimitDTO.LimitTradeRequest toTradeDto(LimitDTO.Purchase purchaseDto, String buyer, Long limitId, String seller) {
		return LimitDTO.LimitTradeRequest.builder()
			.quantity(purchaseDto.getQuantity())
			.buyer(buyer)
			.seller(seller)
			.postId(limitId)
			.isAuction(false) // Limit 거래이므로 false로 설정
			.build();
	}
}
