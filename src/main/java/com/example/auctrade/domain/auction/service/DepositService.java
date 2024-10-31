package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.DepositDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepositService {

    DepositDTO.GetList getDepositInfo(Long auctionId);

    Integer getMinDeposit(Long auctionId);

    List<Long> getMyAuctions(Pageable pageable, String email);

    DepositDTO.Result registerDeposit(DepositDTO.Create request);

    Integer getNowParticipation(Long auctionId);

    Long getMyDepositSize(String email);

    void removeMyDepositLog(String email);

    DepositDTO.Result cancelDeposit(Long auctionId, String email);

}
