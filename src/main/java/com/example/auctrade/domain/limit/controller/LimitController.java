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

	@GetMapping
	public ResponseEntity<List<LimitDTO.Get>> getLimits(){
		List<LimitDTO.Get> limitDTOS = limitService.findAll();
		return ResponseEntity.ok(limitDTOS);
	}

	@PostMapping(value = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<LimitDTO.Get> createLimit(@RequestPart(value = "request") LimitDTO.Create limitDTO, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles, @AuthenticationPrincipal UserDetails userDetails){
		LimitDTO.Get saveLimit = limitService.save(limitDTO);
		return ResponseEntity.ok(saveLimit);
	}

	@GetMapping("/{limitId}")
	public ResponseEntity<LimitDTO.Get> getLimit(@PathVariable Long limitId){
		LimitDTO.Get limitDTO = limitService.findById(limitId);
		return ResponseEntity.ok(limitDTO);
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<LimitDTO.Get>> getUserLimits(@PathVariable Long userId) {
		List<LimitDTO.Get> userLimits = limitService.findByUserId(userId);
		return ResponseEntity.ok(userLimits);
	}
}
