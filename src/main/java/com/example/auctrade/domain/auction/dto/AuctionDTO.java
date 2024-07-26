package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.global.vaild.AuctionValidationGroups;
import com.example.auctrade.global.vaild.MessageValidationGroups;
import com.example.auctrade.global.vaild.ProductValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class AuctionDTO {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class Create {

        @NotBlank(message = "제목을 입력해주세요.", groups = AuctionValidationGroups.TitleBlankGroup.class)
        private String title;

        @NotBlank(message = "내용을 입력해주세요,", groups = AuctionValidationGroups.IntroduceBlankGroup.class)
        private String introduce;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime finishDate;

        @Min(value = 2, message = "최대 인원은 최소 2명 이상입니다.", groups = AuctionValidationGroups.PersonnelRangeGroup.class)
        private Integer maxPersonnel;

        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = AuctionValidationGroups.MinPriceRangeGroup.class)
        private Integer minimumPrice;

        @NotBlank(message = "경매 등록 인원의 email을 입력해 주세요.", groups = AuctionValidationGroups.SaleUserBlankGroup.class)
        private String saleUserEmail;

        @NotBlank(message = "경매 상품명을 입력해 주세요.", groups = ProductValidationGroups.NameBlankGroup.class)
        private String productName;

        private String productDetail;

        @NotBlank(message = "상품의 카테고리를 입력해 주세요.", groups = ProductValidationGroups.CategoryBlankGroup.class)
        private String productCategory;
    }

    @Getter
    @Builder
    public static class Get {
        private String title;
        private String introduce;
        private LocalDateTime startDate;
        private LocalDateTime finishDate;
        private Integer maxPersonnel;
        private Integer minimumPrice;
        private String saleUserEmail;
        private String productName;
        private String productDetail;
        private String productCategory;
    }

    @Getter
    @Builder
    public static class Enter {
        private String title;
        private String introduce;
        private LocalDateTime startDate;
        private LocalDateTime finishDate;
        private Long minimumPrice;
        private String saleUserEmail;
        private String productName;
        private String productDetail;
        private String productCategory;
    }

    @Getter
    @Builder
    public static class List {
        private Long id;
        private String title;
        private String introduce;
        private String startDate;
        private String finishDate;
        private Integer maxPersonnel;
        private Integer minimumPrice;
        private Long price;
        private String productCategory;
    }

    @Getter
    @AllArgsConstructor
    public static class Result{
        private Boolean isSuccess;
    }

    @Getter
    @AllArgsConstructor
    public static class Bid{
        @NotBlank(message = "경매 ID가 없습니다.", groups = MessageValidationGroups.AuctionIdBlankGroup.class)
        private Long auctionId;

        @NotBlank(message = "user ID 가 없습니다.", groups = MessageValidationGroups.UsernameBlankGroup.class)
        private String username;

        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = MessageValidationGroups.MinPriceRangeGroup.class)
        private Long price;
    }

    @Getter
    @AllArgsConstructor
    public static class Deposit{
        @NotBlank(message = "경매 ID가 없습니다.", groups = MessageValidationGroups.AuctionIdBlankGroup.class)
        private Long id;

        @NotBlank(message = "user ID 가 없습니다.", groups = MessageValidationGroups.UsernameBlankGroup.class)
        private String username;

        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = MessageValidationGroups.MinPriceRangeGroup.class)
        private Long deposit;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DepositList{
        private Long id;
        private String title;
        private String introduce;
        private String startDate;
        private String finishDate;
        private Long minDeposit;
        private Integer currentPersonnel;
        private Integer maxPersonnel;
        private Integer minimumPrice;
        private Long price;
        private String productCategory;
    }
}
