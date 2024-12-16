package com.example.auctrade.domain.purchase.mapper;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.purchase.dto.PurchaseDTO;
import com.example.auctrade.domain.purchase.entity.Purchase;

import java.time.LocalDateTime;

public class PurchaseMapper {
    // DTO -> Entity (Create)
    public static Purchase toEntity(PurchaseDTO.Purchase purchaseDto, LimitDTO.Get limitDTO, Long buyerId, Boolean isAuction) {
        return (purchaseDto == null || limitDTO == null || buyerId == null) ? null : Purchase.builder()
                .limitId(limitDTO.getId())
                .buyerId(buyerId)
                .sellerId(limitDTO.getSellerId())
                .quantity(purchaseDto.getQuantity())
                .totalPrice(limitDTO.getPrice() * purchaseDto.getQuantity())
                .isAuction(isAuction)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
