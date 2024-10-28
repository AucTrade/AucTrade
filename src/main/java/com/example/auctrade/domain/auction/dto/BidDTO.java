package com.example.auctrade.domain.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class BidDTO {
    private BidDTO(){}
    @Getter
    @Builder
    @AllArgsConstructor
    public static class Create{
        private Long auctionId;
        private String username;
        private Integer price;
    }
    @Getter
    @Builder
    public static class Get{
        private final Long auctionId;
        private final String username;
        private final Integer price;
    }

    @Getter
    @Builder
    public static class Result{
        private final Long auctionId;
        private final String username;
        private final Integer price;
        private final Boolean isSuccess;
    }

    @Getter
    @Builder
    public static class List{
        private final String id;
        private final Long auctionId;
        private final String username;
        private final Integer price;
    }
}
