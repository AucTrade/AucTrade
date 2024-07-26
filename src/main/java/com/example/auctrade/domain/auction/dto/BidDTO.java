package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.domain.auction.document.BidLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class BidDTO {
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
    public static class Result{
        private final Long auctionId;
        private final String username;
        private final Long price;
        private final Boolean isSuccess;
    }

    @Getter
    public static class List{
        private final String id;
        private final Long auctionId;
        private final String username;
        private final Long price;

        public List(BidLog bidLog){
            this.id = bidLog.getId();
            this.auctionId = bidLog.getAuctionId();
            this.username = bidLog.getUsername();
            this.price = bidLog.getPrice();
        }
    }
}
