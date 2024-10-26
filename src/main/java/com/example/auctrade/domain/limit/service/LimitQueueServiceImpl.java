package com.example.auctrade.domain.limit.service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.service.TradeService;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LimitQueueServiceImpl implements LimitQueueService {

	private final RedissonClient redissonClient;
	private final TradeService tradeService;
	private final UserRepository userRepository;

	private static final String PURCHASE_QUEUE_KEY = "purchase_queue";

	@Override
	public void addToQueue(Long userId) {
		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		queue.add(userId.toString());
		System.out.println("Added to queue: User ID = " + userId);
	}

	@Override
	public boolean processLimitPurchase(LimitDTO.Purchase purchaseDto, Long limitId, String buyer) {
		Long buyerId = userRepository.findByEmail(buyer)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)) // USER_NOT_FOUND는 적절한 예외 코드로 변경
			.getId();

		addToQueue(buyerId);

		LimitDTO.LimitTradeRequest limitTradeRequest = LimitMapper.toTradeDto(purchaseDto, buyerId, limitId);

		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		Set<Long> userIds = queue.readAll().stream()
			.map(Long::parseLong)
			.collect(Collectors.toSet());

		for (Long userKey : userIds) {
			boolean success = tradeService.processLimitTrade(limitTradeRequest);

			// 거래 성공 여부에 따라 대기열에서 사용자 제거 및 결과 반환
			queue.remove(userKey.toString());

			if (success) {
				return true;
			}
		}

		throw new CustomException(ErrorCode.PURCHASE_FAILED);
	}

}
