package com.example.auctrade.domain.limit.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.service.LimitService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class LimitController {
	private final LimitService limitService;


	// Limit 생성
	@PostMapping
	public ResponseEntity<LimitDTO.Get> createLimit(
		@RequestPart("data") LimitDTO.Create limitDTO,
		@RequestPart("files") MultipartFile[] imgFiles,
		@AuthenticationPrincipal UserDetails userDetails) throws IOException {
		String sellerEmail = userDetails.getUsername(); // sellerId를 UserDetails에서 가져옴
		LimitDTO.Get result = limitService.createLimit(limitDTO, imgFiles, sellerEmail);
		return ResponseEntity.ok(result);
	}

	// 모든 Limit 조회
	@GetMapping
	public ResponseEntity<List<LimitDTO.Get>> getAllLimits() {
		List<LimitDTO.Get> result = limitService.getAllLimits();
		return ResponseEntity.ok(result);
	}

	// 특정 Limit ID로 조회
	@GetMapping("/{limitId}")
	public ResponseEntity<LimitDTO.Get> getByLimitId(@PathVariable Long limitId) {
		LimitDTO.Get result = limitService.getByLimitId(limitId);
		return ResponseEntity.ok(result);
	}

	// 특정 사용자 ID로 Limit 조회
	@GetMapping("/seller/{userId}")
	public ResponseEntity<List<LimitDTO.Get>> getLimitBySellerId(@PathVariable Long userId) {
		List<LimitDTO.Get> result = limitService.getLimitBySellerId(userId);
		return ResponseEntity.ok(result);
	}

	// Limit 마감 처리
	@PutMapping("/{limitId}/end")
	public ResponseEntity<Void> markLimitAsEnded(@PathVariable Long limitId) {
		limitService.markLimitAsEnded(limitId);
		return ResponseEntity.noContent().build();
	}

	// 내 Limit 목록 조회 (페이지네이션 지원)
	@GetMapping("/my")
	public ResponseEntity<LimitDTO.GetPage> getMyLimitedPage(
		@RequestParam int page,
		@RequestParam int size,
		@RequestParam(defaultValue = "all") String status,
		@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails.getUsername(); // 인증된 사용자 이메일
		LimitDTO.GetPage result = limitService.getMyLimitedPage(page, size, status, email);
		return ResponseEntity.ok(result);
	}
	// private final LimitQueueService queueService;
	//
	// @GetMapping
	// public ResponseEntity<List<LimitDTO.Get>> getLimits(){
	// 	List<LimitDTO.Get> limitDTOS = limitService.findAll();
	// 	return ResponseEntity.ok(limitDTOS);
	// }

	// @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	// public ResponseEntity<LimitDTO.Get> createLimit(
	// 	@RequestPart(value = "request") LimitDTO.Create limitDTO,
	// 	@RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles,
	// 	@AuthenticationPrincipal UserDetails userDetails) throws IOException {
	//
	// 	// 인증된 사용자 이메일 가져오기
	// 	String email = userDetails.getUsername();
	//
	// 	// 서비스 호출하여 한정 판매 생성
	// 	LimitDTO.Get savedLimit = limitService.createLimitSale(limitDTO, imgFiles, email);
	//
	// 	// 응답 반환
	// 	return ResponseEntity.ok(savedLimit);
	// }

	//
	// @GetMapping("/{limitId}")
	// public ResponseEntity<LimitDTO.Get> getLimit(@PathVariable Long limitId){
	// 	LimitDTO.Get limitDTO = limitService.findById(limitId);
	// 	return ResponseEntity.ok(limitDTO);
	// }
	// @GetMapping("/user/{userId}")
	// public ResponseEntity<List<LimitDTO.Get>> getUserLimits( @AuthenticationPrincipal UserDetails userDetails) {
	// 	List<LimitDTO.Get> userLimits = limitService.findByUserEmail(userDetails.getUsername());
	// 	return ResponseEntity.ok(userLimits);
	// }
	// @GetMapping("/my/limits")
	// public ResponseEntity<LimitDTO.GetPage> getAllMyLimits(
	// 		@RequestParam(defaultValue = "1") int page,
	// 		@RequestParam(defaultValue = "9") int size,
	// 		@RequestParam(defaultValue = "all") String status,
	// 		@AuthenticationPrincipal UserDetails userDetails) {
	//
	// 	return ResponseEntity.ok(limitService.getMyLimitedPage(page, size, status, userDetails.getUsername()));
	// }
	//
	// @PostMapping("/{limitId}/purchase")
	// public ResponseEntity<?> purchaseLimit(@PathVariable Long limitId, @RequestBody LimitDTO.Purchase purchaseDto, @AuthenticationPrincipal UserDetails userDetails) {
	// 	boolean result = queueService.processLimitPurchase(purchaseDto, limitId, userDetails.getUsername());
	//
	// 	return ResponseEntity.ok(result);
	// }

}
