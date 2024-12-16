package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidStatus;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BidLogService {

    Long createBidLog(BidVo bidVo, BidStatus status);

    Long updateLogStatus(Long auctionId, Long userId, BidStatus status);

    BidInfoVo getBidLog(Long auctionId, Long userId);

    List<BidInfoVo> getAllBidLog(Pageable pageable, Long auctionId);

    List<BidInfoVo> getAllMyBidLog(Pageable pageable, Long userId);

    Boolean containsUserId(Long auctionId, Long userId);

}
