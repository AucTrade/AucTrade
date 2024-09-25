package com.example.auctrade.domain.limit.service;

import java.util.List;

import com.example.auctrade.domain.limit.dto.PurchaseDTO;
import org.springframework.data.domain.Page;

public interface PurchaseService {
	boolean processPurchase(Long limitsId, int purchaseQuantity, Long userId);
	List<PurchaseDTO.Get> findPurchasesByUserId(String email);

	PurchaseDTO.GetPage findPurchasePages(String email, int page, int size);

}
