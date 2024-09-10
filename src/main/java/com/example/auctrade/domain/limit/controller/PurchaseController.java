package com.example.auctrade.domain.limit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.dto.PurchaseDTO;
import com.example.auctrade.domain.limit.service.PurchaseHandlerService;
import com.example.auctrade.domain.limit.service.PurchaseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
	private final PurchaseHandlerService purchaseHandlerServiceService;
	private final PurchaseService purchaseService;
	@PostMapping("/{limitId}")
	public ResponseEntity<LimitDTO.Get> purchase(@PathVariable Long limitId, @RequestBody PurchaseDTO.Create purchaseDTO){
		LimitDTO.Get limitDTO = purchaseHandlerServiceService.purchase(limitId, purchaseDTO);
		return ResponseEntity.ok(limitDTO);
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<PurchaseDTO.Get>> getUserPurchases(@PathVariable Long userId) {
		List<PurchaseDTO.Get> purchaseList = purchaseService.findPurchasesByUserId(userId);
		return ResponseEntity.ok(purchaseList);
	}
}
