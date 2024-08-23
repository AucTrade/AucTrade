package com.example.auctrade.domain.limit.service;

import org.springframework.stereotype.Service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseHandlerServiceImpl implements PurchaseHandlerService {
	private final PurchaseQueueService queueService;
	private final PurchaseService purchaseService;
	private final LimitRepository limitRepository;

	@Override
	public LimitDTO.Get purchase(long limitId, LimitDTO.Purchase purchaseDTO) {
		Limits limit = limitRepository.findById(limitId).orElseThrow();

		String userId = System.currentTimeMillis() + "user";
		queueService.addToQueue(userId);

		for (String userKey : queueService.getQueue()) {
			boolean success = purchaseService.processPurchase(limitId, purchaseDTO.getQuantity(), userId);
			queueService.removeFromQueue(userKey);
			if (success) {
				break;
			}
		}
		return LimitMapper.toDto(limit);
	}
}
