package com.example.auctrade;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.entity.Trade;
import com.example.auctrade.domain.trade.mapper.TradeMapper;
import com.example.auctrade.domain.trade.repository.TradeRepository;
import com.example.auctrade.domain.trade.service.TradeServiceImpl;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TradeServiceTest {

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private LimitRepository limitRepository;

	@Mock
	private AuctionRepository auctionRepository;

	@Mock
	private TradeRepository tradeRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private TradeServiceImpl tradeService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void processLimitTrade_Success() {
		// given
		LimitDTO.LimitTradeRequest request = new LimitDTO.LimitTradeRequest(1, "buyer1", "seller1", 100L, false);
		TradeDTO.Create tradeDTO = TradeMapper.toCreateDto(request);
		Limits limits = Limits.builder()
			.id(100L)
			.price(100L)
			.amount(10)
			.build();

		when(limitRepository.findById(anyLong())).thenReturn(Optional.of(limits));
		when(userService.updatePointById(anyLong(), anyString())).thenReturn(true);
		when(tradeRepository.save(any(Trade.class))).thenReturn(Trade.builder().id(1L).build());

		// when
		boolean result = tradeService.processLimitTrade(request);

		// then
		assertTrue(result);
		verify(tradeRepository, times(1)).save(any(Trade.class));
	}

	@Test
	void processLimitTrade_InsufficientStock() {
		// given
		LimitDTO.LimitTradeRequest request = new LimitDTO.LimitTradeRequest(15, "buyer1", "seller1", 100L, false);
		TradeDTO.Create tradeDTO = TradeMapper.toCreateDto(request);
		Limits limits = Limits.builder()
			.id(100L)
			.price(100L)
			.amount(10)
			.build();

		when(limitRepository.findById(anyLong())).thenReturn(Optional.of(limits));

		// when & then
		assertThrows(CustomException.class, () -> tradeService.processLimitTrade(request));
		verify(tradeRepository, never()).save(any(Trade.class));
	}

	@Test
	void processAuctionTrade_Success() {
		// given
		TradeDTO.Create request = TradeDTO.Create.builder()
			.buyer("buyer1")
			.seller("seller1")
			.postId(200L)
			.quantity(1)
			.isAuction(true)
			.build();

		Auction auction = Auction.builder()
			.id(200L)
			.price(100L)
			.isEnded(true)
			.build();

		when(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction));
		when(tradeRepository.save(any(Trade.class))).thenReturn(Trade.builder().id(1L).build());

		// when
		boolean result = tradeService.processAuctionTrade(request);

		// then
		assertTrue(result);
		verify(tradeRepository, times(1)).save(any(Trade.class));
	}

	@Test
	void processAuctionTrade_AuctionNotEnded() {
		// given
		TradeDTO.Create request = TradeDTO.Create.builder()
			.buyer("buyer1")
			.seller("seller1")
			.postId(200L)
			.quantity(1)
			.isAuction(true)
			.build();

		Auction auction = Auction.builder()
			.id(200L)
			.price(100L)
			.isEnded(false)  // 경매가 아직 종료되지 않은 상태
			.build();

		when(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction));

		// when & then
		assertThrows(CustomException.class, () -> tradeService.processAuctionTrade(request));
		verify(tradeRepository, never()).save(any(Trade.class));
	}
}