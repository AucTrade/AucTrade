package com.example.auctrade.domain.trade.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.entity.Trade;
import com.example.auctrade.domain.trade.mapper.TradeMapper;
import com.example.auctrade.domain.trade.repository.TradeRepository;
import com.example.auctrade.domain.trade.service.TradeService;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

	private final RedissonClient redissonClient;
	private final LimitRepository limitRepository;
	private final AuctionRepository auctionRepository;
	private final TradeRepository tradeRepository;

	@Override
	@Transactional(readOnly = true)
	public List<TradeDTO.Get> findTradesByUserId(Long userId) {
		List<Trade> trades = tradeRepository.findByBuyer(userId);

		return trades.stream()
			.map(trade -> TradeMapper.toGetDto(trade))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public boolean processLimitTrade(LimitDTO.LimitTradeRequest limitTradeRequest){
		TradeDTO.Create tradeDTO = TradeMapper.toCreateDto(limitTradeRequest);
		boolean success = validateLimitTrade(tradeDTO);

		// 검증 통과 시 거래 내역 저장
		if (success) {
			Trade tradeEntity = TradeMapper.toEntity(tradeDTO, calculatePrice(tradeDTO));
			tradeRepository.save(tradeEntity);
			return success;
		} else {
			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
		}
	}
	@Override
	@Transactional
	public boolean processAuctionTrade(TradeDTO.Create tradeDTO){
		boolean success = validateAuctionTrade(tradeDTO);
		// 검증 통과 시 거래 내역 저장
		if (success) {
			Trade tradeEntity = TradeMapper.toEntity(tradeDTO, calculatePrice(tradeDTO));
			tradeRepository.save(tradeEntity);
			return success;
		} else {
			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
		}
	}

	// 한정 판매 검증 및 처리
	private boolean validateLimitTrade(TradeDTO.Create tradeDTO) {

		// Lua 스크립트를 사용하여 재고 및 구매 한도 검증
		Long result = executeLuaScriptForLimit(tradeDTO.getPostId(), tradeDTO.getQuantity(), tradeDTO.getBuyer());

		if (result == null) {
			throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED); // 예기치 않은 오류 처리
		}

		if (result >= 0) {
			return true; // 검증 통과
		} else if (Long.valueOf(-1L).equals(result)) {  // 명시적으로 Long 타입으로 비교
			throw new CustomException(ErrorCode.INSUFFICIENT_STOCK); // 재고 부족
		} else if (Long.valueOf(-2L).equals(result)) {  // 명시적으로 Long 타입으로 비교
			throw new CustomException(ErrorCode.USER_LIMIT_EXCEEDED); // 인당 구매 한도 초과
		}

		throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED); // 예기치 않은 오류 처리
	}


	// 경매 검증 및 처리
	private boolean validateAuctionTrade(TradeDTO.Create tradeDTO) {
		// 경매 종료 여부 확인
		Auction auction = getAuction(tradeDTO.getPostId());

		if (!auction.isEnded()) {
			throw new CustomException(ErrorCode.AUCTION_NOT_ENDED);
		}

		// 낙찰자 확인 & 낙찰가 확인
		// Auction entity에 추가 안할건지 확인
		return true; // 검증 성공
	}

	// Lua 스크립트를 사용한 한정 판매 검증 (재고 및 구매 한도)
	private Long executeLuaScriptForLimit(Long postId, int quantity, Long buyerId) {
		String luaScript =
			"local stock_key = KEYS[1] " +  // 게시판 재고 수량
				"local purchase_count_key = KEYS[2] " +  // 사용자별 구매한 수량 (해시 구조)
				"local user_id = ARGV[1] " +  // 구매자 아이디
				"local purchase_quantity = tonumber(ARGV[2]) " +  // 사용자가 원하는 구매 수량
				"local user_limit = tonumber(ARGV[3]) " +  // 게시판의 인당 구매 제한 수량

				// 재고 확인
				"local stock = redis.call('GET', stock_key) " +
				"if not stock or tonumber(stock) < purchase_quantity then " +
				"    return {-1} " +  // 재고 부족 시 -1 반환
				"end " +

				// 사용자 구매 한도 확인
				"local purchase_count = redis.call('HGET', purchase_count_key, user_id) " +
				"if not purchase_count then " +
				"    purchase_count = 0 " +
				"end " +

				"if tonumber(purchase_count) + purchase_quantity > user_limit then " +
				"    return {-2} " +  // 구매 한도 초과 시 -2 반환
				"end " +

				// 재고 차감 및 구매 내역 업데이트
				"redis.call('DECRBY', stock_key, purchase_quantity) " +
				"redis.call('HINCRBY', purchase_count_key, user_id, purchase_quantity) " +

				// 남은 재고 반환
				"local remaining_stock = tonumber(stock) - purchase_quantity " +
				"return {remaining_stock} ";  // 남은 재고 반환

		// 전체 재고와 사용자 한도를 체크하는 메서드 호출
		int currentStock = checkLimitStockFromRedis(postId);
		int userLimit = checkUserLimitFromRedis(postId, buyerId);  // buyerId 없이 원래대로

		// Lua 스크립트를 실행하여 Redis 내 재고 및 사용자 구매 한도 업데이트
		List<Object> result = redissonClient.getScript().eval(
			RScript.Mode.READ_WRITE,
			luaScript,
			RScript.ReturnType.MULTI,
			java.util.Arrays.asList("limit:" + postId + ":stock", "limit:" + postId + ":purchase_count"),  // Hash로 사용자별 구매 기록 관리
			buyerId.toString(), String.valueOf(quantity), String.valueOf(userLimit)
		);

		// 결과 처리 (남은 재고 반환)
		return (Long) result.get(0);  // 첫 번째 값은 남은 재고
	}




	// Redis에서 사용자 구매 한도를 가져오는 메서드
	private int checkUserLimitFromRedis(Long postId, Long buyerId) {
		// Redis에서 해당 LimitId의 구매 한도 값을 가져옴
		String redisKey = "limit:" + postId + ":user_limit";
		RBucket<String> userLimitBucket = redissonClient.getBucket(redisKey);

		// Redis에 값이 존재하면 해당 값을 반환
		String userLimitStr = userLimitBucket.get();
		if (userLimitStr != null) {
			return Integer.parseInt(userLimitStr);
		}

		// Redis에 값이 없으면 DB에서 사용자의 구매 내역을 조회하고 한도를 계산
		// Limit 엔티티에서 기본 한도 가져옴
		Limits limit = getLimit(postId);
		int baseUserLimit = limit.getLimit();  // Limit 엔티티에서 기본 구매 한도

		// Trade 테이블에서 사용자가 이미 구매한 수량을 조회
		int totalPurchased = tradeRepository.findTotalPurchasedByBuyerAndPostId(buyerId, postId);

		// 남은 구매 가능 수량 = 기본 구매 한도 - 이미 구매한 수량
		int remainingUserLimit = baseUserLimit - totalPurchased;

		// 남은 구매 가능 수량을 Redis에 캐싱
		userLimitBucket.set(String.valueOf(remainingUserLimit));

		return remainingUserLimit; // 남은 구매 가능 수량을 반환
	}


	private int checkLimitStockFromRedis(Long postId) {
		// Redis에서 해당 LimitId의 전체 재고 값을 가져옴
		String redisKey = "limit:" + postId + ":stock";
		RBucket<String> stockBucket = redissonClient.getBucket(redisKey);

		// Redis에 값이 존재하면 해당 값을 반환
		String stockStr = stockBucket.get();
		if (stockStr != null) {
			return Integer.parseInt(stockStr);
		}

		// Redis에 값이 없으면 Limit 엔티티에서 재고 값을 가져옴
		Limits limit = getLimit(postId);
		int totalStock = limit.getAmount();  // Limit 엔티티에서 전체 재고 값 가져옴

		// Trade 테이블에서 구매된 총 수량을 계산
		int purchasedQuantity = tradeRepository.findTotalPurchasedByPostId(postId);

		// 남은 재고 계산 (전체 재고 - 구매된 수량)
		int remainingStock = totalStock - purchasedQuantity;

		// 가져온 남은 재고 값을 Redis에 저장 (캐싱)
		stockBucket.set(String.valueOf(remainingStock));

		return remainingStock; // 남은 재고 값을 반환
	}


	private long calculatePrice(TradeDTO.Create tradeDTO) {
		// Auction일 경우 경매 가격 처리
		if (tradeDTO.getIsAuction()) {
			// 경매의 경우, 경매 ID에 따른 가격을 조회하거나 경매 로직에 따라 처리
			Auction auction = getAuction(tradeDTO.getPostId());
			return (long)auction.getPrice(); // 경매에서 최종 낙찰된 가격 반환
		} else {
			// 한정 판매일 경우 수량에 따른 가격 계산
			Limits limit = getLimit(tradeDTO.getPostId());
			return limit.getPrice() * tradeDTO.getQuantity(); // 한정 판매의 단가 * 수량
		}
	}

	private Limits getLimit(Long limitId) {
		return limitRepository.findById(limitId)
			.orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
	}

	private Auction getAuction(Long auctionId) {
		return auctionRepository.findById(auctionId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
	}

}
