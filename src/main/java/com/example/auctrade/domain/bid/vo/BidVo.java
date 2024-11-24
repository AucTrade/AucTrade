package com.example.auctrade.domain.bid.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BidVo {
    private Long auctionId;
    private Long userId;
    private String email;
    private Integer amount;
}