package com.example.auctrade.domain.bid.mapper;

import com.example.auctrade.domain.bid.entity.BidLog;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidStatus;
import com.example.auctrade.domain.bid.vo.BidUserInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;

public class BidMapper {
    private BidMapper(){}

    public static BidLog toEntity(BidVo bidVo, BidStatus status) {
        return  BidLog.builder()
                .auctionId(bidVo.getAuctionId())
                .userId(bidVo.getUserId())
                .amount(bidVo.getAmount())
                .status(status)
                .build();
    }

    public static BidInfoVo toBidInfoVo(BidLog bidLog) {
        return BidInfoVo.builder()
                .auctionId(bidLog.getAuctionId())
                .userId(bidLog.getUserId())
                .amount(bidLog.getAmount())
                .status(bidLog.getStatus())
                .build();
    }

    public static BidUserInfoVo toBidUserInfoVo(String email, Integer amount){
        return BidUserInfoVo.builder()
                .email(email == null ? "NONE" : email)
                .amount(amount == null ? -1 : amount)
                .build();
    }
}

