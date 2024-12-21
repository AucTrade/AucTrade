package com.example.auctrade.domain.trade.service;// package com.example.auctrade.domain.trade.service;
//
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.stream.Collectors;
//
// import org.redisson.api.RBucket;
// import org.redisson.api.RLock;
// import org.redisson.api.RedissonClient;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import com.example.auctrade.domain.auction.entity.Auction;
// import com.example.auctrade.domain.auction.repository.AuctionRepository;
// import com.example.auctrade.domain.limit.dto.LimitDTO;
// import com.example.auctrade.domain.limit.entity.Limits;
// import com.example.auctrade.domain.limit.repository.LimitRepository;
// import com.example.auctrade.domain.trade.dto.TradeDTO;
// import com.example.auctrade.domain.trade.entity.Trade;
// import com.example.auctrade.domain.trade.mapper.TradeMapper;
// import com.example.auctrade.domain.trade.repository.TradeRepository;
// import com.example.auctrade.global.exception.CustomException;
// import com.example.auctrade.global.exception.ErrorCode;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class TradeRedissonServiceImpl implements TradeService {
//
// 	private final RedissonClient redissonClient;
// 	private final LimitRepository limitRepository;
// 	private final AuctionRepository auctionRepository;
// 	private final TradeRepository tradeRepository;
// 	@Override
// 	@Transactional(readOnly = true)
// 	public List<TradeDTO.Get> findTradesByUserId(Long userId) {
// 		List<Trade> trades = tradeRepository.findByBuyer(userId);
//
// 		return trades.stream()
// 			.map(trade -> TradeMapper.toGetDto(trade))
// 			.collect(Collectors.toList());
// 	}
// 	// 구매 처리 로직 구현
// 	@Override
// 	@Transactional
// 	public boolean processLimitTrade(LimitDTO.LimitTradeRequest limitTradeRequest) {
// 		TradeDTO.Create tradeDTO = TradeMapper.toCreateDto(limitTradeRequest);
// 		boolean success = validateLimitTrade(tradeDTO);
//
// 		if (success) {
// 			Trade tradeEntity = TradeMapper.toEntity(tradeDTO, calculatePrice(tradeDTO));
// 			tradeRepository.save(tradeEntity);
// 			return true;
// 		} else {
// 			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
// 		}
// 	}
// 	@Override
// 	@Transactional
// 	public boolean processAuctionTrade(TradeDTO.Create tradeDTO){
// 		boolean success = validateAuctionTrade(tradeDTO);
// 		// 검증 통과 시 거래 내역 저장
// 		if (success) {
// 			Trade tradeEntity = TradeMapper.toEntity(tradeDTO, calculatePrice(tradeDTO));
// 			tradeRepository.save(tradeEntity);
// 			return success;
// 		} else {
// 			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
// 		}
// 	}
// 	// 경매 검증 및 처리
// 	private boolean validateAuctionTrade(TradeDTO.Create tradeDTO) {
// 		// 경매 종료 여부 확인
// 		Auction auction = getAuction(tradeDTO.getPostId());
//
// 		if (!auction.isEnded()) {
// 			throw new CustomException(ErrorCode.AUCTION_NOT_ENDED);
// 		}
//
// 		// 낙찰자 확인 & 낙찰가 확인
// 		// Auction entity에 추가 안할건지 확인
// 		return true; // 검증 성공
// 	}
// 	private boolean validateLimitTrade(TradeDTO.Create tradeDTO) {
// 		Long result = executeRedissonForLimit(tradeDTO.getPostId(), tradeDTO.getQuantity(), tradeDTO.getBuyer());
//
// 		if (result == null) {
// 			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
// 		}
//
// 		if (result >= 0) {
// 			return true;
// 		} else if (Long.valueOf(-1L).equals(result)) {
// 			throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
// 		} else if (Long.valueOf(-2L).equals(result)) {
// 			throw new CustomException(ErrorCode.USER_LIMIT_EXCEEDED);
// 		}
//
// 		throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
// 	}
//
// 	// Redisson을 통한 한정 판매 검증 (재고 및 구매 한도)
// 	private Long executeRedissonForLimit(Long postId, int quantity, Long buyerId) {
// 		String stockKey = "limit:" + postId + ":stock";
// 		String purchaseCountKey = "limit:" + postId + ":purchase_count";
//
// 		// Redis 락 설정
// 		RLock lock = redissonClient.getLock("limitLock:" + postId);
// 		try {
// 			// 락 획득
// 			lock.lock();
//
// 			// 재고 확인
// 			int stock = checkLimitStockFromRedis(postId);
// 			if (stock < quantity) {
// 				return -1L;
// 			}
//
// 			// 사용자 구매 한도 확인
// 			int userLimit = checkUserLimitFromRedis(postId, buyerId);
//
// 			int purchaseCount = getUserPurchaseCount(purchaseCountKey, buyerId);
//
// 			if (purchaseCount + quantity > userLimit) {
// 				return -2L;
// 			}
//
// 			// 재고 차감 및 구매 내역 업데이트
// 			RBucket<Integer> stockBucket = redissonClient.getBucket(stockKey);
// 			stockBucket.set(stock - quantity);
// 			updateUserPurchaseCount(purchaseCountKey, buyerId, quantity);
//
// 			return (long) (stock - quantity);
//
// 		} finally {
// 			lock.unlock();
// 		}
// 	}
//
// 	private int getUserPurchaseCount(String purchaseCountKey, Long buyerId) {
// 		RBucket<String> purchaseCountBucket = redissonClient.getBucket(purchaseCountKey + ":" + buyerId);
// 		String purchaseCountStr = purchaseCountBucket.get();
// 		return purchaseCountStr != null ? Integer.parseInt(purchaseCountStr) : 0;
// 	}
//
// 	private void updateUserPurchaseCount(String purchaseCountKey, Long buyerId, int quantity) {
// 		RBucket<String> purchaseCountBucket = redissonClient.getBucket(purchaseCountKey + ":" + buyerId);
// 		int updatedCount = getUserPurchaseCount(purchaseCountKey, buyerId) + quantity;
// 		purchaseCountBucket.set(String.valueOf(updatedCount));
// 	}
//
// 	private int checkLimitStockFromRedis(Long postId) {
// 		String redisKey = "limit:" + postId + ":stock";
// 		RBucket<String> stockBucket = redissonClient.getBucket(redisKey);
//
// 		String stockStr = stockBucket.get();
// 		if (stockStr != null) {
// 			return Integer.parseInt(stockStr);
// 		}
//
// 		Limits limit = getLimit(postId);
// 		int totalStock = limit.getAmount();
// 		int purchasedQuantity = tradeRepository.findTotalPurchasedByPostId(postId);
//
// 		int remainingStock = totalStock - purchasedQuantity;
// 		stockBucket.set(String.valueOf(remainingStock));
//
// 		return remainingStock;
// 	}
//
// 	private int checkUserLimitFromRedis(Long postId, Long buyerId) {
// 		// Redis에서 해당 LimitId의 구매 한도 값을 가져옴
// 		String redisKey = "limit:" + postId + ":user_limit";
// 		RBucket<String> userLimitBucket = redissonClient.getBucket(redisKey);
//
// 		// Redis에 값이 존재하면 해당 값을 반환
// 		String userLimitStr = userLimitBucket.get();
// 		if (userLimitStr != null) {
// 			return Integer.parseInt(userLimitStr);
// 		}
//
// 		// Redis에 값이 없으면 DB에서 사용자의 구매 내역을 조회하고 한도를 계산
// 		// Limit 엔티티에서 기본 한도 가져옴
// 		Limits limit = getLimit(postId);
// 		int baseUserLimit = limit.getLimit();  // Limit 엔티티에서 기본 구매 한도
//
// 		// Trade 테이블에서 사용자가 이미 구매한 수량을 조회
// 		Integer  totalPurchased = tradeRepository.findTotalPurchasedByBuyerAndPostId(buyerId, postId);
// 		if (totalPurchased == null) {
// 			totalPurchased = 0;
// 		}
// 		// 남은 구매 가능 수량 = 기본 구매 한도 - 이미 구매한 수량
// 		int remainingUserLimit = baseUserLimit - totalPurchased;
//
// 		// 남은 구매 가능 수량을 Redis에 캐싱
// 		userLimitBucket.set(String.valueOf(remainingUserLimit));
//
// 		return remainingUserLimit; // 남은 구매 가능 수량을 반환
// 	}
//
// 	private Limits getLimit(Long limitId) {
// 		return limitRepository.findById(limitId)
// 			.orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
// 	}
//
// 	private long calculatePrice(TradeDTO.Create tradeDTO) {
// 		if (tradeDTO.getIsAuction()) {
// 			Auction auction = getAuction(tradeDTO.getPostId());
// 			return auction.getPrice();
// 		} else {
// 			Limits limit = getLimit(tradeDTO.getPostId());
// 			return limit.getPrice() * tradeDTO.getQuantity();
// 		}
// 	}
//
// 	private Auction getAuction(Long auctionId) {
// 		return auctionRepository.findById(auctionId)
// 			.orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
// 	}
// }
