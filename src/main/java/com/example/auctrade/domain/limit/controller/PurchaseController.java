package com.example.auctrade.domain.limit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<LimitDTO.Get> purchase(@PathVariable Long limitId, @RequestBody PurchaseDTO.Create purchaseDTO,  @AuthenticationPrincipal UserDetails userDetails){
		LimitDTO.Get limitDTO = purchaseHandlerServiceService.purchase(limitId, purchaseDTO, userDetails.getUsername());
		return ResponseEntity.ok(limitDTO);
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<PurchaseDTO.Get>> getUserPurchases(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails ) {
		List<PurchaseDTO.Get> purchaseList = purchaseService.findPurchasesByUserId(userDetails.getUsername());
		return ResponseEntity.ok(purchaseList);
	}
	@GetMapping("/my")
	public ResponseEntity<PurchaseDTO.GetPage> getPurchasePages(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size, @AuthenticationPrincipal UserDetails userDetails) {
		PurchaseDTO.GetPage pages = purchaseService.findPurchasePages(userDetails.getUsername(), page, size);
		return ResponseEntity.ok(pages);
	}
}
