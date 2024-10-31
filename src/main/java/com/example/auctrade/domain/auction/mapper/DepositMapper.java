package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.document.DepositLog;
import com.example.auctrade.domain.auction.dto.DepositDTO;

public class DepositMapper {
    private DepositMapper(){}

    public static DepositDTO.Result toResultDto(boolean isSuccess) {
        return new DepositDTO.Result(isSuccess);
    }
    public static DepositDTO.GetList toListDto(int deposit, int maxParticipants) {
        return new DepositDTO.GetList(deposit, maxParticipants);
    }
    public static DepositLog toEntity(DepositDTO.Create request){
        return (request == null) ? null : DepositLog.builder()
                .auctionId(request.getAuctionId())
                .deposit(request.getDeposit())
                .username(request.getEmail())
                .build();
    }
}
