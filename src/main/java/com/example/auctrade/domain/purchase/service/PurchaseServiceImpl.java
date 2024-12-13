package com.example.auctrade.domain.purchase.service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.service.LimitService;
import com.example.auctrade.domain.purchase.dto.PurchaseDTO;
import com.example.auctrade.domain.purchase.entity.Purchase;
import com.example.auctrade.domain.purchase.mapper.PurchaseMapper;
import com.example.auctrade.domain.purchase.repository.PurchaseRepository;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RQueue;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

	private final RedissonClient redissonClient;
	private final PurchaseRepository purchaseRepository;
	private final UserService userService;
	private final LimitService limitService;

	private static final String PURCHASE_QUEUE_KEY = "purchase_queue";

	@Override
	public boolean processPurchase(PurchaseDTO.Purchase purchase, Long limitId, String buyerEmail) {
		Long buyerId = userService.getUserIdByEmail(buyerEmail);
		// Limit 정보를 가져와 PurchaseEntity 생성
		LimitDTO.Get limitDTO = limitService.getByLimitId(limitId);
		Boolean isAuction = false;  // Limit 거래이므로 false로 설정
		Purchase purchaseEntity = PurchaseMapper.toEntity(purchase, limitDTO, buyerId, isAuction);

		// 구매 요청을 큐에 추가
		addToQueue(buyerId);

		// 큐에서 구매 요청 처리
		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		Set<Long> userIds = queue.readAll().stream()
			.map(Long::parseLong)
			.collect(Collectors.toSet());

		for (Long userKey : userIds) {
			boolean success = validatePurchase(purchaseEntity);

			// 성공하면 큐에서 제거 및 결과 반환
			if (success) {
				queue.remove(userKey.toString());
				return true;
			}
		}

		throw new CustomException(ErrorCode.PURCHASE_FAILED);
	}

	private void addToQueue(Long buyerId) {
		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		queue.add(buyerId.toString());
	}

	private boolean validatePurchase(Purchase purchase) {
		Long result = executeLuaScriptForLimit(purchase);

		if (result == null) {
			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
		}

		if (result >= 0) {
			if (purchase.getTotalPrice() <= getUserPoints(purchase.getBuyerId())) {
				return true;
			} else {
				throw new CustomException(ErrorCode.INSUFFICIENT_POINTS);
			}
		} else if (Long.valueOf(-1L).equals(result)) {
			throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
		} else if (Long.valueOf(-2L).equals(result)) {
			throw new CustomException(ErrorCode.USER_LIMIT_EXCEEDED);
		}

		throw new CustomException(ErrorCode.PURCHASE_FAILED);
	}

	private Long executeLuaScriptForLimit(Purchase purchase) {
		String luaScript = """
            local stock_key = KEYS[1]
            local purchase_count_key = KEYS[2]
            local user_id = ARGV[1]
            local purchase_quantity = tonumber(ARGV[2])
            local user_limit = tonumber(ARGV[3])

            local stock = redis.call('GET', stock_key)
            if not stock or tonumber(stock) < purchase_quantity then
                return -1
            end

            local purchase_count = redis.call('HGET', purchase_count_key, user_id)
            if not purchase_count then
                purchase_count = 0
            end

            if tonumber(purchase_count) + purchase_quantity > user_limit then
                return -2
            end

            redis.call('DECRBY', stock_key, purchase_quantity)
            redis.call('HINCRBY', purchase_count_key, user_id, purchase_quantity)

            local remaining_stock = tonumber(stock) - purchase_quantity
            return remaining_stock
        """;

		Long result = redissonClient.getScript().eval(
			RScript.Mode.READ_WRITE,
			luaScript,
			RScript.ReturnType.MULTI,
			java.util.Arrays.asList("limit:" + purchase.getLimitId() + ":stock", "limit:" + purchase.getLimitId() + ":purchase_count"),
			purchase.getBuyerId().toString(),
			String.valueOf(purchase.getQuantity()),
			String.valueOf(fetchPersonalLimitForUser(purchase))
		);

		return result;
	}

//	public int checkLimitStockFromRedis(Long limitId) {
//		String redisKey = "limit:" + limitId + ":stock";
//		RBucket<String> stockBucket = redissonClient.getBucket(redisKey);
//		String stockStr = stockBucket.get();
//		return stockStr != null ? Integer.parseInt(stockStr) : calculateRemainingStock(limitId);
//	}

//	private int calculateRemainingStock(Long limitId) {
//		LimitDTO.Get limitDTO = limitService.getByLimitId(limitId);
//		int totalStock = limitDTO.getAmount();
//		int purchasedQuantity = purchaseRepository.findTotalPurchasedByBuyerIdAndLimitId(limitId);
//		int remainingStock = totalStock - purchasedQuantity;
//
//		redissonClient.getBucket("limit:" + limitId + ":stock").set(String.valueOf(remainingStock));
//		return remainingStock;
//	}

	private int fetchPersonalLimitForUser(Purchase purchase) {
		String redisKey = "limit:" + purchase.getLimitId() + ":user_limit";
		RBucket<String> userLimitBucket = redissonClient.getBucket(redisKey);
		String userLimitStr = (String) userLimitBucket.get();

		if (userLimitStr != null) {
			return Integer.parseInt(userLimitStr);
		}

		LimitDTO.Get limitDTO = limitService.getByLimitId(purchase.getLimitId());
		int baseUserLimit = limitDTO.getPersonalLimit();
		int totalPurchased = purchaseRepository.countByBuyerIdAndLimitId(purchase.getBuyerId(), purchase.getLimitId());
		int remainingUserLimit = baseUserLimit - totalPurchased;

		userLimitBucket.set(String.valueOf(remainingUserLimit));
		return remainingUserLimit;
	}

	private long getUserPoints(Long buyerId) {
		return userService.getUserPoint(buyerId);
	}
}
