package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.domain.auction.document.BidLog;
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
        private Long price;
    }
    @Getter
    @Builder
    public static class Get{
        private Long auctionId;
        private String username;
        private Long price;

        public void updatePrice(long price){
            this.price = price;
        }
    }

    @Getter
    @Builder
    public static class Result{
        private final Long auctionId;
        private final String username;
        private final Long price;
        private final Boolean isSuccess;
    }

    @Getter
    @Builder
    public static class List{
        private final String id;
        private final Long auctionId;
        private final String username;
        private final Long price;
    }
}
