package com.example.auctrade.domain.purchase.controller;

import com.example.auctrade.domain.purchase.dto.PurchaseDTO;
import com.example.auctrade.domain.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
public class PurchaseController {

	private final PurchaseService purchaseService;

	@PostMapping("/{limitId}")
	public ResponseEntity<Boolean> processPurchase(
		@PathVariable Long limitId,
		@RequestBody PurchaseDTO.Purchase purchaseDto,
		@AuthenticationPrincipal UserDetails userDetails) {
		String buyerEmail = userDetails.getUsername(); // 인증된 사용자 ID 추출
		boolean result = purchaseService.processPurchase(purchaseDto, limitId, buyerEmail);
		return ResponseEntity.ok(result);
	}

}
