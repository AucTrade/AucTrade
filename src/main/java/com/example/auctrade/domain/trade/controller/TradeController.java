package com.example.auctrade.domain.trade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.service.TradeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
	private final TradeService tradeService;
	// @PostMapping
	// public ResponseEntity<TradeDTO.Get> trade(@RequestBody TradeDTO.Create purchaseDTO){
	// 	TradeDTO.Get tradeDTO = tradeHandlerService.handleTrade(purchaseDTO);
	// 	return ResponseEntity.ok(tradeDTO);
	// }
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<TradeDTO.Get>> getUserTrade(@PathVariable Long userId) {
		List<TradeDTO.Get> purchaseList = tradeService.findTradesByUserId(userId);
		return ResponseEntity.ok(purchaseList);
	}
}
