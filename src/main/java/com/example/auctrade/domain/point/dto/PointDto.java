package com.example.auctrade.domain.point.dto;

import com.example.auctrade.global.valid.PointValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class PointDto {
    private PointDto(){}
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create{
        @NotNull(message = "포인트 량을 입력해주세요.", groups = PointValidationGroups.AmountBlankGroup.class)
        private Integer amount;
        @NotBlank(message = "계좌 번호를 입력해주세요.", groups = PointValidationGroups.AccountBlankGroup.class)
        private String account;
    }

    @Getter
    @AllArgsConstructor
    public static class Result {
        private final Long pointId;
        private final Boolean success;
    }

    @Getter
    @AllArgsConstructor
    public static class Get {
        private final Integer amount;
    }
}
