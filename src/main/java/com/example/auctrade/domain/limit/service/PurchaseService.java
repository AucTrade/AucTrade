package com.example.auctrade.domain.limit.service;

public interface PurchaseService {
	boolean processPurchase(Long limitsId, int purchaseQuantity, String userId);
}
