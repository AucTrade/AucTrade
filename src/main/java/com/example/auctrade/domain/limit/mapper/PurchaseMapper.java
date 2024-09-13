package com.example.auctrade.domain.limit.mapper;

import com.example.auctrade.domain.limit.dto.PurchaseDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.entity.Purchase;
import com.example.auctrade.domain.user.entity.User;

public class PurchaseMapper {

	// DTO -> Entity (Create)
	public static Purchase toEntity(PurchaseDTO.Create purchaseDTO, Limits limits, User user) {
		if (purchaseDTO == null || limits == null || user == null) {
			return null;
		}

		return Purchase.builder()
			.price(limits.getPrice())
			.isFinished(false) // 초기값: 결제가 완료되지 않은 상태
			.quantity(purchaseDTO.getQuantity())
			.limit(limits)
			.buyer(user)
			.build();
	}

	// Entity -> DTO (Get)
	public static PurchaseDTO.Get toDto(Purchase purchase, Limits limits) {
		if (purchase == null) {
			return null;
		}

		return PurchaseDTO.Get.builder()
			.id(purchase.getId())
			.price(limits.getPrice())
			.date(purchase.getDate()) // 결제가 완료되면 서버에서 자동으로 설정된 date 반환
			.quantity(purchase.getQuantity())
			.isFinished(purchase.getIsFinished())
			.buyerEmail(purchase.getBuyer().getEmail())
			.limitTitle(purchase.getLimit().getTitle())
			.build();
	}
}
