package com.example.auctrade.domain.limit.service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.dto.PurchaseDTO;

public interface PurchaseHandlerService {
	LimitDTO.Get purchase(long limitId, PurchaseDTO.Create purchaseDTO);
}
