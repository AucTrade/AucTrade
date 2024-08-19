package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.global.vaild.MessageValidationGroups;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class DepositDTO {
    private DepositDTO(){}
    @Getter
    @Builder
    public static class Create{
        @NotBlank(message = "경매 ID가 없습니다.", groups = MessageValidationGroups.AuctionIdBlankGroup.class)
        private Long auctionId;

        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = MessageValidationGroups.MinPriceRangeGroup.class)
        private Long deposit;
    }

    @Getter
    @AllArgsConstructor
    public static class Get{
        private String id;
    }

    @Getter
    @AllArgsConstructor
    public static class List{
        private Long deposit;
        private Integer currentPersonnel;
    }
    @Getter
    @AllArgsConstructor
    public static class Result{
        private final Boolean success;
    }
}
