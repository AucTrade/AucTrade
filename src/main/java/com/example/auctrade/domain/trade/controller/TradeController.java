package com.example.auctrade.domain.trade.controller;

import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

	@PostMapping
	public ResponseEntity<TradeDTO.Get> processTradePoints(@RequestBody TradeDTO.Create tradeCreateDTO) {
		return ResponseEntity.ok(tradeService.processLimitTrade(tradeCreateDTO));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<TradeDTO.Get>> getUserTrade(@PathVariable Long userId) {
		List<TradeDTO.Get> purchaseList = tradeService.findTradesByUserId(userId);
		return ResponseEntity.ok(purchaseList);
	}
}
