package com.example.auctrade.domain.bid.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BidInfoVo {
    private Long auctionId;
    private String email;
    private Integer amount;
}
