package com.example.auctrade.domain.bid.mapper;

import com.example.auctrade.domain.bid.entity.BidLog;
import com.example.auctrade.domain.bid.vo.BidVo;

public class BidMapper {
    private BidMapper(){}

    public static BidLog toEntity(BidVo bidVo, Boolean isSuccess) {
        return  BidLog.builder()
                .auctionId(bidVo.getAuctionId())
                .userId(bidVo.getUserId())
                .amount(bidVo.getAmount())
                .isSuccess(isSuccess)
                .build();
    }
}

