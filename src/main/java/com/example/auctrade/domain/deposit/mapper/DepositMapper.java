package com.example.auctrade.domain.deposit.mapper;

import com.example.auctrade.domain.deposit.entity.DepositLog;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import com.example.auctrade.domain.deposit.vo.DepositVo;

public class DepositMapper {
    private DepositMapper(){}

    public static DepositLog toEntity(DepositVo depositVo, DepositStatus status) {
        return  DepositLog.builder()
                .auctionId(depositVo.getAuctionId())
                .userId(depositVo.getUserId())
                .amount(depositVo.getAmount())
                .status(status)
                .build();
    }

    public static DepositInfoVo toDepositInfoVo(DepositLog depositLog) {
        return DepositInfoVo.builder()
                .auctionId(depositLog.getAuctionId())
                .userId(depositLog.getUserId())
                .amount(depositLog.getAmount())
                .build();
    }
}

