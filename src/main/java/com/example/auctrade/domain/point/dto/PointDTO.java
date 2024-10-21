package com.example.auctrade.domain.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PointDTO {
    private PointDTO(){}

    @Getter
    @AllArgsConstructor
    public static class Recharge {
        @Min(value = 0, message = "포인트는 최소 0 이상입니다.")
        private Long recharge;
    }

    @Getter
    @AllArgsConstructor
    public static class Exchange {
        @Min(value = 0, message = "포인트는 최소 0 이상입니다.")
        private Long exchange;
    }

    @Getter
    @AllArgsConstructor
    public static class Remittance {
        @Min(value = 0, message = "포인트는 최소 0 이상입니다.")
        private Long point;
        @NotBlank
        private String account;
    }

    @Getter
    @AllArgsConstructor
    public static class Result {
        private Long points;
        private Boolean isSuccess;
    }
}
