package com.example.auctrade.domain.limit.service;

import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

	private final RedissonClient redissonClient;
	private final LimitRepository limitRepository;

	@Override
	@Transactional
	public boolean processPurchase(Long limitsId, int purchaseQuantity, String userId) {
		// 한정판매 정보 가져오기
		Limits limits = limitRepository.findById(limitsId)
				.orElseThrow(() -> new RuntimeException("Limits not found"));
		int userLimit = limits.getLimit();

		// Redis에 해당 상품의 재고가 있는지 확인하고 없으면 설정
		String stockKey = "stock:" + limitsId;
		RAtomicLong stock = redissonClient.getAtomicLong(stockKey);
		long currentStock = stock.get();
		if (currentStock == 0) {
			stock.set(limits.getAmount());
		}

		// Redis에 해당 상품의 사용자별 구매 수량이 있는지 확인하고 없으면 초기화
		String purchaseCountKey = "purchase_count:" + limitsId;
		RMap<String, Integer> purchaseCountMap = redissonClient.getMap(purchaseCountKey);
		Integer userPurchaseCount = purchaseCountMap.get(userId);
		if (userPurchaseCount == null) {
			purchaseCountMap.put(userId, 0);
			userPurchaseCount = 0;
		}

		// 비즈니스 로직 수행
		synchronized (this) {
			long stockValue = stock.get();
			if (stockValue < purchaseQuantity) {
				log.info("구매 실패: 재고 부족 사용자 ID = {}", userId);
				return false;
			}

			if (userPurchaseCount + purchaseQuantity > userLimit) {
				log.info("구매 실패: 인당 구매 한도 초과 사용자 ID = {}", userId);
				return false;
			}

			stock.addAndGet(-purchaseQuantity);
			purchaseCountMap.put(userId, userPurchaseCount + purchaseQuantity);

			// Redis의 남은 재고를 PostgreSQL에 업데이트
			limits.setAmount((int) stock.get());
			limitRepository.save(limits);
			log.info("레디스 재고: {} DB 재고: {}", stock.get(), limits.getAmount());
			return true;
		}
	}
}
