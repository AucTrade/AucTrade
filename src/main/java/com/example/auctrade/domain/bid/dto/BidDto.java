package com.example.auctrade.domain.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class BidDto {
    private BidDto(){}

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserIfo {
        private Long userId;
        private String email;
        private Integer amount;
    }
}
