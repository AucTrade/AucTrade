package com.example.auctrade.domain.purchase.service;

import com.example.auctrade.domain.purchase.dto.PurchaseDTO;

public interface PurchaseService {
	boolean processPurchase(PurchaseDTO.Purchase purchaseDto, Long limitId, String buyerEmail);
}
