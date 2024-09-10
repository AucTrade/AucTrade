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
import com.example.auctrade.domain.limit.service.LimitService;

import lombok.RequiredArgsConstructor;

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

	@PostMapping
	public ResponseEntity<LimitDTO.Get> createLimit(@RequestBody LimitDTO.Create limitDTO){
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
