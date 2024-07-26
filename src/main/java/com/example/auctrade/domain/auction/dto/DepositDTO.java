package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.domain.auction.document.DepositLog;
import com.example.auctrade.global.vaild.MessageValidationGroups;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class DepositDTO {
    @Getter
    @Builder
    public static class Create{
        private Long auctionId;
        private String username;
        private Long price;
        private Integer maxPersonnel;
    }

    @Getter
    @AllArgsConstructor
    public static class Get{
        private String id;
        public Get(DepositLog depositLog){
            this.id = depositLog.getId();
        }
    }

    @Getter
    public static class List{
        private String id;
        public List(DepositLog depositLog){
            this.id = depositLog.getId();
        }
    }
}
