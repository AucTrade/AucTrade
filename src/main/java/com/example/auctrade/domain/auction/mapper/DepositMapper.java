package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.dto.DepositDTO;

public class DepositMapper {
    private DepositMapper(){}

    public static DepositDTO.Result toResultDto(boolean isSuccess) {
        return new DepositDTO.Result(isSuccess);
    }
    public static DepositDTO.List toListDto(long deposit, int psersonnel) {
        return new DepositDTO.List(deposit, psersonnel);
    }
}
