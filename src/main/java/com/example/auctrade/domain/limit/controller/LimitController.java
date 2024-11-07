package com.example.auctrade.domain.limit.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.service.LimitQueueService;
import com.example.auctrade.domain.limit.service.LimitService;
import com.example.auctrade.domain.trade.dto.TradeDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class LimitController {
	private final LimitService limitService;
	private final LimitQueueService queueService;

	@GetMapping
	public ResponseEntity<List<LimitDTO.Get>> getLimits(){
		List<LimitDTO.Get> limitDTOS = limitService.findAll();
		return ResponseEntity.ok(limitDTOS);
	}

	@PostMapping(value = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<LimitDTO.Get> createLimit(@RequestPart(value = "request") LimitDTO.Create limitDTO, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
		LimitDTO.Get savedLimit = limitService.save(limitDTO, imgFiles, userDetails.getUsername());
		return ResponseEntity.ok(savedLimit);
	}

	@GetMapping("/{limitId}")
	public ResponseEntity<LimitDTO.Get> getLimit(@PathVariable Long limitId){
		LimitDTO.Get limitDTO = limitService.findById(limitId);
		return ResponseEntity.ok(limitDTO);
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<LimitDTO.Get>> getUserLimits( @AuthenticationPrincipal UserDetails userDetails) {
		List<LimitDTO.Get> userLimits = limitService.findByUserEmail(userDetails.getUsername());
		return ResponseEntity.ok(userLimits);
	}
	@GetMapping("/my/limits")
	public ResponseEntity<LimitDTO.GetPage> getAllMyLimits(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "9") int size,
			@RequestParam(defaultValue = "all") String status,
			@AuthenticationPrincipal UserDetails userDetails) {

		return ResponseEntity.ok(limitService.getMyLimitedPage(page, size, status, userDetails.getUsername()));
	}

	@PostMapping("/{limitId}/purchase")
	public ResponseEntity<?> purchaseLimit(@PathVariable Long limitId, @RequestBody LimitDTO.Purchase purchaseDto, @AuthenticationPrincipal UserDetails userDetails) {
		boolean result = queueService.processLimitPurchase(purchaseDto, limitId, userDetails.getUsername());

		return ResponseEntity.ok(result);
	}

}
