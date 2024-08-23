package com.example.auctrade.domain.limit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.service.PurchaseHandlerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
	private final PurchaseHandlerService purchaseService;

	@PostMapping("/{limitId}")
	public ResponseEntity<LimitDTO.Get> purchase(@PathVariable Long limitId, @RequestBody LimitDTO.Purchase purchaseDTO){
		LimitDTO.Get limitDTO = purchaseService.purchase(limitId, purchaseDTO);
		return ResponseEntity.ok(limitDTO);
	}

}
