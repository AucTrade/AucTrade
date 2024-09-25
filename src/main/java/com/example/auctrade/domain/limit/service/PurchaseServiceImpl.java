package com.example.auctrade.domain.limit.service;

import java.util.List;

import com.example.auctrade.domain.user.repository.UserRepository;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auctrade.domain.limit.dto.PurchaseDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.entity.Purchase;
import com.example.auctrade.domain.limit.mapper.PurchaseMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.limit.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

	private final RedissonClient redissonClient;
	private final LimitRepository limitRepository;
	private final PurchaseRepository purchaseRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public boolean processPurchase(Long limitsId, int purchaseQuantity, Long userId) {
		Limits limits = limitRepository.findById(limitsId).orElseThrow(() -> new RuntimeException("Limits not found"));
		int userLimit = limits.getLimit();

		initializeStockIfNeeded(limitsId, limits.getAmount());
		initializeUserPurchaseCountIfNeeded(limitsId, userId);

		Long result = executeLuaScript(limitsId, purchaseQuantity, userId, userLimit);

		return processResult(result, limits, purchaseQuantity);
	}

	// Redis에 재고를 초기화
	private void initializeStockIfNeeded(Long limitsId, int currentStock) {
		RBucket<String> stockBucket = redissonClient.getBucket("stock:" + limitsId);
		String stockStr = stockBucket.get();

		Integer stock = (stockStr != null) ? Integer.parseInt(stockStr) : null;

		if (stock == null) {
			stockBucket.set(String.valueOf(currentStock));
		}
	}


	// Redis에 사용자별 구매 수량을 초기화
	private void initializeUserPurchaseCountIfNeeded(Long limitsId, Long userId) {
		RMap<String, Integer> purchaseCountMap = redissonClient.getMap("purchase_count:" + limitsId);

		if (!purchaseCountMap.containsKey(userId.toString())) {
			purchaseCountMap.put(userId.toString(), 0);
		}
	}

	// Lua 스크립트로 Redis 내 구매 과정 처리
	private Long executeLuaScript(Long limitsId, int purchaseQuantity, Long userId, int userLimit) {
		String luaScript =
			"local stock_key = KEYS[1] " +
				"local purchase_count_key = KEYS[2] " +
				"local user_id = ARGV[1] " +
				"local purchase_quantity = tonumber(ARGV[2]) " +
				"local user_limit = tonumber(ARGV[3]) " +

				// stock_key에 대한 기본값 설정
				"local stock = redis.call('GET', stock_key) " +
				"if not stock then " +
				"    stock = 0 " + // 기본값 0 설정
				"    redis.call('SET', stock_key, stock) " + // Redis에 기본값 저장
				"else " +
				"    stock = tonumber(stock) " + // 숫자로 변환
				"end " +

				"if stock < purchase_quantity then return -1 end " + // 재고 부족 시 -1 반환

				// 사용자 구매 수량에 대한 기본값 설정
				"local user_purchase_count = redis.call('HGET', purchase_count_key, user_id) " +
				"if not user_purchase_count then " +
				"    user_purchase_count = 0 " + // 기본값 0 설정
				"    redis.call('HSET', purchase_count_key, user_id, user_purchase_count) " + // Redis에 기본값 저장
				"else " +
				"    user_purchase_count = tonumber(user_purchase_count) " + // 숫자로 변환
				"end " +

				"if user_purchase_count + purchase_quantity > user_limit then return -2 end " + // 구매 한도 초과 시 -2 반환

				// 재고 감소 및 구매 수량 증가
				"local new_stock = redis.call('DECRBY', stock_key, purchase_quantity) " +
				"redis.call('HINCRBY', purchase_count_key, user_id, purchase_quantity) " +
				"return new_stock"; // 남은 재고 반환


		return redissonClient.getScript().eval(
			org.redisson.api.RScript.Mode.READ_WRITE,
			luaScript,
			org.redisson.api.RScript.ReturnType.INTEGER,
			java.util.Arrays.asList("stock:" + limitsId, "purchase_count:" + limitsId),
			userId.toString(), String.valueOf(purchaseQuantity), String.valueOf(userLimit)
		);
	}

	// 구매 후 결과를 PostgreSQL에 반영
	private boolean processResult(Long result, Limits limits, int purchaseQuantity) {
		if (result >= 0) {
			limits.setAmount(result.intValue());
			limitRepository.save(limits);
			System.out.println("Redisson 재고: " + result + " DB 재고: " + limits.getAmount());
			return true;
		} else if (result == -1) {
			System.out.println("구매 실패: 재고 부족 사용자 ID = " + limits.getSaleUser().getEmail());
		} else if (result == -2) {
			System.out.println("구매 실패: 인당 구매 한도 초과 사용자 ID = " + limits.getSaleUser().getEmail());
		}
		return false;
	}
	@Override
	@Transactional(readOnly = true)
	public List<PurchaseDTO.Get> findPurchasesByUserId(String email) {
		// 구매 내역을 PurchaseRepository를 통해 조회
		List<Purchase> purchases = purchaseRepository.findByBuyerId(userRepository.findByEmail(email).orElseThrow().getId());

		// Purchase 엔티티 리스트를 PurchaseDTO.Get로 변환하여 반환
		return purchases.stream()
			.map(purchase -> PurchaseMapper.toDto(purchase, purchase.getLimit()))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PurchaseDTO.GetPage findPurchasePages(String email, int page, int size) {

		Page<Purchase> purchases = purchaseRepository.findByBuyerId(userRepository.findByEmail(email).orElseThrow().getId(),
				toPageable(page, size, "date"));

		List<PurchaseDTO.Get> dtos = purchases.get().map(purchase -> PurchaseMapper.toDto(purchase, purchase.getLimit()))
				.toList();

		return PurchaseMapper.toDtoPage(dtos, purchases.getTotalPages());
	}

	private Pageable toPageable(int page, int size, String target){
		return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
	}
}
