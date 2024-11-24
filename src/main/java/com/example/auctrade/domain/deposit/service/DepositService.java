package com.example.auctrade.domain.deposit.service;

import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositVo;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepositService {
    Long placeDeposit(DepositVo depositVo);

    Boolean cancelDeposit(Long auctionId, Long userId);

    Integer getDepositAmount(Long auctionId, Long userId);

    Integer getMinDepositAmount(Long auctionId);

    Integer getNowParticipants(Long auctionId);

    List<DepositInfoVo> getAllMyDepositInfo(Integer page,Integer size, String email);

    List<DepositInfoVo> getAllDepositInfo(Long auctionId);

}
