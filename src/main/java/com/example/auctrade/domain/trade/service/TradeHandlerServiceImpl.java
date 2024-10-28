package com.example.auctrade.domain.trade.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.service.TradeHandlerService;
import com.example.auctrade.domain.trade.service.TradeQueueService;
import com.example.auctrade.domain.trade.service.TradeService;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeHandlerServiceImpl implements TradeHandlerService {

	private final TradeQueueService queueService;
	private final TradeService tradeService;
	private final AuctionRepository auctionRepository;

	@Override
	public TradeDTO.Get handleTrade(TradeDTO.Create tradeDTO) {
		if (tradeDTO.getIsAuction()) {
			// Auction인 경우
			return handleAuctionTrade(tradeDTO);
		} else {
			// Limit인 경우
			return handleLimitTrade(tradeDTO);
		}
	}

	@Override
	public TradeDTO.Get handleLimitTrade(TradeDTO.Create tradeDTO) {
		// 대기열에 사용자 추가
		queueService.addToQueue(tradeDTO.getBuyer());

		// 대기열에 있는 사용자 처리
		for (Long userKey : queueService.getQueue()) {
			TradeDTO.Get trade = tradeService.processTrade(tradeDTO);

			// 대기열에서 사용자 제거
			queueService.removeFromQueue(userKey);

			if (trade != null) {
				return trade;
			}
		}

		throw new IllegalStateException("Trade processing failed.");
	}

	@Override
	public TradeDTO.Get handleAuctionTrade(TradeDTO.Create tradeDTO) {
		// 경매 종료 여부 확인 (Auction 엔티티를 가져와 확인 필요)
		verifyAuctionEnd(tradeDTO.getPostId());

		// 결제 처리 및 TradeDTO.Get 반환
		TradeDTO.Get trade = tradeService.processTrade(tradeDTO);

		if (trade != null) {
			return trade;
		} else {
			throw new IllegalStateException("Auction purchase failed.");
		}
	}

	// 경매 종료 여부 확인 메서드 (필요한 경우에만 Auction 엔티티를 사용)
	private void verifyAuctionEnd(Long auctionId) {
		Auction auction = auctionRepository.findById(auctionId)
			.orElseThrow(() -> new IllegalStateException("Auction not found."));
//		if (!auction.isEnded()) {
//			throw new IllegalStateException("Auction is not yet ended. Payment can only be made after auction ends.");
//		}
	}


}
