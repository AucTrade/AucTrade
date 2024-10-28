package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.global.valid.MessageValidationGroups;
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
        private Long auctionId;
        private Integer deposit;
        private int minPrice;
        private int maxParticipation;
        private String startTime;
        private String email;
    }

    @Getter
    @AllArgsConstructor
    public static class Get{
        private final String id;
    }

    @Getter
    @AllArgsConstructor
    public static class GetList{
        private final Integer deposit;
        private final Integer nowParticipation;
    }

    @Getter
    @AllArgsConstructor
    public static class Result{
        private final Boolean success;
    }
}
