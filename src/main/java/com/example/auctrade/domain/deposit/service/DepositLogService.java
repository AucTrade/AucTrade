package com.example.auctrade.domain.deposit.service;

import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import com.example.auctrade.domain.deposit.vo.DepositVo;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepositLogService {

    Long createDepositLog(DepositVo depositVo);

    Long updateLogStatus(Long auctionId, Long userId, DepositStatus status);

    DepositInfoVo getMinDepositLog(Long auctionId);

    DepositInfoVo getDepositLog(Long auctionId, Long userId);

    List<DepositInfoVo> getAllDepositLog(Long auctionId, DepositStatus status);

    List<DepositInfoVo> getAllMyDepositLog(Pageable pageable, Long userId);

    Boolean containsUserId(Long auctionId, Long userId);

}
