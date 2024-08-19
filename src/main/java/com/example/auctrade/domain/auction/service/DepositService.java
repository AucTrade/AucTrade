package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.DepositDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepositService {

    DepositDTO.List getDeposit(Long auctionId, int maxPersonnel);

    Long getMinDeposit(Long auctionId, int maxPersonnel);

    List<Long> getMyAuctions(Pageable pageable, String email);

    DepositDTO.Result depositPrice(DepositDTO.Create requestDto, String email, int maxPersonnel, String startDate);

    Integer getCurrentPersonnel(Long auctionId);

    Long getMyDepositSize(String email);

}
