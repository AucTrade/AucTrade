package com.example.auctrade.domain.limit.service;

import java.util.List;

import com.example.auctrade.domain.limit.dto.LimitDTO;

public interface LimitService {
	LimitDTO.Get save(LimitDTO.Create dto);
	List<LimitDTO.Get> findAll();
	LimitDTO.Get findById(Long id);
	void endLimit(Long id);
}
