package com.example.auctrade.domain.limit.service;

import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.repository.LimitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

	private final RedisTemplate<String, Object> redisTemplate;
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
		if (redisTemplate.opsForValue().get(stockKey) == null) {
			int currentStock = limits.getAmount();
			redisTemplate.opsForValue().set(stockKey, currentStock);
		}

		// Redis에 해당 상품의 사용자별 구매 수량이 있는지 확인하고 없으면 초기화
		String purchaseCountKey = "purchase_count:" + limitsId;
		if (redisTemplate.opsForHash().entries(purchaseCountKey).isEmpty()) {
			redisTemplate.opsForHash().put(purchaseCountKey, userId, 0);
		}

		// Lua 스크립트 정의
		String luaScript = "local stock_key = KEYS[1] "
			+ "local purchase_count_key = KEYS[2] "
			+ "local user_id = ARGV[1] "
			+ "local purchase_quantity = tonumber(ARGV[2]) "
			+ "local user_limit = tonumber(ARGV[3]) "
			+ "local stock = tonumber(redis.call('GET', stock_key)) or 0 "
			+ "if stock < purchase_quantity then return -1 end "
			+ "local user_purchase_count = tonumber(redis.call('HGET', purchase_count_key, user_id)) or 0 "
			+ "if user_purchase_count + purchase_quantity > user_limit then return -2 end "
			+ "local new_stock = redis.call('DECRBY', stock_key, purchase_quantity) "
			+ "redis.call('HINCRBY', purchase_count_key, user_id, purchase_quantity) "
			+ "return new_stock";

		try {
			//Lua 실행
			Long result = redisTemplate.execute(
				(RedisCallback<Long>) connection -> connection.eval(
					luaScript.getBytes(),
					ReturnType.INTEGER,
					2,  // KEYS의 개수
					stockKey.getBytes(),
					purchaseCountKey.getBytes(),
					userId.getBytes(),
					String.valueOf(purchaseQuantity).getBytes(),
					String.valueOf(userLimit).getBytes()
				)
			);

			if (result >= 0) {
				// Redis의 남은 재고를 PostgreSQL에 업데이트
				limits.setAmount(result.intValue());
				limitRepository.save(limits);
				System.out.println("레디스 재고: " + result.intValue() + " DB 재고:" + limits.getAmount());
				return true;
			} else if (result == -1) {
				System.out.println("구매 실패: 재고 부족 사용자 ID = " + userId);
			} else if (result == -2) {
				System.out.println("구매 실패: 인당 구매 한도 초과 사용자 ID = " + userId);
			}
		} catch (Exception e) {
			System.out.println("Error during Lua script execution: " + e.getMessage());
			e.printStackTrace();
		}


		return false;
	}
}
