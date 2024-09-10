package com.example.auctrade.domain.limit.service;

import java.util.List;

import com.example.auctrade.domain.limit.dto.PurchaseDTO;

public interface PurchaseService {
	boolean processPurchase(Long limitsId, int purchaseQuantity, Long userId);
	List<PurchaseDTO.Get> findPurchasesByUserId(Long userId);
}
