package com.example.auctrade.domain.limit.service;

import com.example.auctrade.domain.limit.dto.LimitDTO;

public interface PurchaseHandlerService {
	LimitDTO.Get purchase(long limitId, LimitDTO.Purchase purchaseDTO);
}
