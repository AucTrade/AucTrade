package com.example.auctrade.domain.limit.mapper;
import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.user.entity.User;

import java.time.format.DateTimeFormatter;

public class LimitMapper {

	// Entity -> DTO (Create)
	public static LimitDTO.Get toDto(Limits limits) {
		return (limits == null) ? null : LimitDTO.Get.builder()
			.id(limits.getId())
			.title(limits.getTitle())
			.introduce(limits.getIntroduce())
			.price(limits.getPrice())
			.saleDate(limits.getSaleDate())
			.limit(limits.getLimit())
			.productName(limits.getProduct().getName())
			.productDetail(limits.getProduct().getDetail())
			.productCategory(limits.getProduct().getCategory().getCategoryName())
			.saleUserEmail(limits.getSaleUser().getEmail())
			.created(limits.getCreated())
			.build();
	}


	// DTO -> Entity (Create)
	public static Limits toEntity(LimitDTO.Create limitDTO, Product product, User saleUser) {
		return (limitDTO == null) ? null : Limits.builder()
			.title(limitDTO.getTitle())
			.introduce(limitDTO.getIntroduce())
			.price(limitDTO.getPrice())
			.saleDate(limitDTO.getSaleDate())
			.limit(limitDTO.getLimit())
			.product(product)
			.saleUser(saleUser)
			.build();
	}
	// DTO -> Trade DTO (LimitTradeRequest 생성)
	public static LimitDTO.LimitTradeRequest toTradeDto(LimitDTO.Purchase purchaseDto, Long buyerId, Long limitId) {
		return LimitDTO.LimitTradeRequest.builder()
			.quantity(purchaseDto.getQuantity())
			.buyer(buyerId)
			.postId(limitId)
			.isAuction(false) // Limit 거래이므로 false로 설정
			.build();
	}
}