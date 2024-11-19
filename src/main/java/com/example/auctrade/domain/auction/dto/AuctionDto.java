package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.global.valid.AuctionValidationGroups;
import com.example.auctrade.global.valid.MessageValidationGroups;
import com.example.auctrade.global.valid.ProductValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionDto {
    private AuctionDto(){}

    @Getter
    @AllArgsConstructor
    public static class Create {

        @NotBlank(message = "제목을 입력해주세요.", groups = AuctionValidationGroups.TitleBlankGroup.class)
        private String title;

        @NotBlank(message = "내용을 입력해주세요,", groups = AuctionValidationGroups.IntroduceBlankGroup.class)
        private String introduce;

        @Min(value = 2, message = "최대 인원은 최소 2명 이상입니다.", groups = AuctionValidationGroups.PersonnelRangeGroup.class)
        private Integer maxParticipants;

        @NotBlank(message = "경매 상품명을 입력해 주세요.", groups = ProductValidationGroups.NameBlankGroup.class)
        private String productName;

        private String productDetail;

        @NotBlank(message = "상품의 카테고리를 입력해 주세요.", groups = ProductValidationGroups.CategoryBlankGroup.class)
        private long productCategoryId;

        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = AuctionValidationGroups.MinPriceRangeGroup.class)
        private Integer minimumPrice;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime startAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime endAt;
    }

    @Getter
    @AllArgsConstructor
    public static class PutDeposit {
        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = MessageValidationGroups.MinPriceRangeGroup.class)
        private Integer amount;
    }

    @Getter
    @AllArgsConstructor
    public static class PutBid {
        @Min(value = 0, message = "비용은 최소 0원 이상입니다.", groups = MessageValidationGroups.MinPriceRangeGroup.class)
        private Integer amount;
    }

    @Getter
    @Builder
    public static class Get {
        private Long id;
        private String title;
        private String introduce;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer maxParticipants;
        private Integer minimumPrice;
        private String email;
        private Long productId;
        private String productName;
        private String productDetail;
        private String productCategory;
    }

    @Getter
    @Builder
    public static class Enter {
        private String title;
        private String introduce;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer minimumPrice;
        private String email;
        private String productName;
        private String productDetail;
        private String productCategory;
        private List<String> files;
        private String bidUser;
        private Integer bidAmount;
    }

    @Getter
    @Builder
    public static class GetList {
        private Long id;
        private String title;
        private String introduce;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startAt;
        private String endAt;
        private Integer maxParticipants;
        private Integer nowParticipants;
        private Integer minimumPrice;
        private Long productId;
        private String productCategory;
        private String thumbnail;
        private Boolean isStarted;
        private Boolean isEnded;
    }

    @Getter
    @AllArgsConstructor
    public static class GetPage {
        private final List<GetList> auctions;
        private final int maxPage;
    }

    @Getter
    @AllArgsConstructor
    public static class Result{
        private Long auctionId;
        private Boolean isSuccess;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BeforeStart{
        private Long id;
        private String title;
        private String introduce;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer minDeposit;
        private Integer nowParticipants;
        private String thumbnail;
        private Integer maxParticipants;
        private Integer minimumPrice;
        private String productCategory;
    }
}
