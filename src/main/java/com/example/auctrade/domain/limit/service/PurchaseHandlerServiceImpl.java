package com.example.auctrade.domain.limit.service;

import org.springframework.stereotype.Service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.dto.PurchaseDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.entity.Purchase;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.limit.mapper.PurchaseMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.limit.repository.PurchaseRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseHandlerServiceImpl implements PurchaseHandlerService {

	private final PurchaseQueueService queueService;
	private final PurchaseService purchaseService;
	private final LimitRepository limitRepository;
	private final UserRepository userRepository;
	private final PurchaseRepository purchaseRepository;

	@Override
	public LimitDTO.Get purchase(long limitId, PurchaseDTO.Create purchaseDTO, String email) {
		// Limits와 User를 데이터베이스에서 조회
		Limits limit = limitRepository.findById(limitId).orElseThrow();
		User user = userRepository.findByEmail(email).orElseThrow();

		// 사용자 구매 대기열에 추가
		queueService.addToQueue(user.getId());

		// 대기열에 있는 사용자 처리
		for (Long userKey : queueService.getQueue()) {
			boolean success = purchaseService.processPurchase(limitId, purchaseDTO.getQuantity(), user.getId());

			// 대기열에서 사용자 제거
			queueService.removeFromQueue(userKey);

			if (success) {
				// 구매 기록을 limit_payment 테이블에 저장
				Purchase purchaseEntity = PurchaseMapper.toEntity(purchaseDTO, limit, user);
				purchaseEntity.completePayment(); // 결제 완료 날짜와 상태를 설정
				purchaseRepository.save(purchaseEntity);

				break;
			}
		}

		return LimitMapper.toDto(limit); // 업데이트된 한정 판매 정보를 반환
	}
}
