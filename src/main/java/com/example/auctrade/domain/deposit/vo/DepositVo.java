package com.example.auctrade.domain.deposit.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepositVo {
    private Long auctionId;
    private Long userId;
    private Integer amount;
    private Integer maxParticipants;
}
