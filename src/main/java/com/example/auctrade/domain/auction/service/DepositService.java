package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.DepositDTO;

public interface DepositService {

    Boolean save(DepositDTO.Create request);

    Long getMinDeposit(Long auctionId, int maxPersonnel);

    Integer getCurrentPersonnel(Long auctionId);
}
