package com.example.auctrade.domain.trade.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.auctrade.domain.user.dto.UserDTO;
import org.jboss.marshalling.TraceInformation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auctrade.domain.auction.service.AuctionService;
import com.example.auctrade.domain.limit.service.LimitService;
import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.entity.Trade;
import com.example.auctrade.domain.trade.mapper.TradeMapper;
import com.example.auctrade.domain.trade.repository.TradeRepository;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

	private final TradeRepository tradeRepository;
	private final UserService userService;
	private final AuctionService auctionService;
	private final LimitService limitService;

	@Override
	@Transactional(readOnly = true)
	public List<TradeDTO.Get> findTradesByUserId(Long userId) {
		UserDTO.Info userInfo = userService.getUserInfoById(userId);
		List<Trade> trades = tradeRepository.findByBuyer(userInfo.getEmail());
		return trades.stream()
			.map(TradeMapper::toGetDto)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public TradeDTO.Get processLimitTrade(TradeDTO.Create tradeDTO) {
		boolean success = userService.updatePointById(-calculatePrice(tradeDTO), tradeDTO.getBuyerId());
		if (!success) throw new CustomException(ErrorCode.POINT_UPDATE_FAILED);

		success = userService.updatePointById(calculatePrice(tradeDTO), tradeDTO.getSellerId());
		if (!success) throw new CustomException(ErrorCode.POINT_UPDATE_FAILED);

		Trade tradeEntity = TradeMapper.toEntity(tradeDTO, calculatePrice(tradeDTO));
		return TradeMapper.toGetDto(tradeRepository.save(tradeEntity));
	}

//	@Override
//	@Transactional
//	public boolean processAuctionTrade(TradeDTO.Create tradeDTO) {
//		Trade tradeEntity = TradeMapper.toEntity(tradeDTO, calculatePrice(tradeDTO));
//		tradeRepository.save(tradeEntity);
//		return true;
//	}

	//옥션 거래 수정 필요
	private long calculatePrice(TradeDTO.Create tradeDTO) {
		return tradeDTO.getIsAuction() ?
                (long) auctionService.findById(tradeDTO.getPostId()).getMinimumPrice() * tradeDTO.getQuantity() :
			limitService.getByLimitId(tradeDTO.getPostId()).getPrice() * tradeDTO.getQuantity();
	}
}
