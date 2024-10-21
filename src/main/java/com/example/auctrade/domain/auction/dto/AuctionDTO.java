package com.example.auctrade.domain.auction.dto;

import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.global.valid.AuctionValidationGroups;
import com.example.auctrade.global.valid.ProductValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionDTO {
    private AuctionDTO(){}

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

        @NotBlank(message = "경매 상품명을 입력해 주세요.", groups = ProductValidationGroups.NameBlankGroup.class)
        private String productName;

        private String productDetail;

        @NotBlank(message = "상품의 카테고리를 입력해 주세요.", groups = ProductValidationGroups.CategoryBlankGroup.class)
        private long productCategoryId;
    }

    @Getter
    @Builder
    public static class Get {
        private Long id;
        private String title;
        private String introduce;
        private LocalDateTime startDate;
        private LocalDateTime finishDate;
        private Integer maxPersonnel;
        private Integer minimumPrice;
        private String saleUserEmail;
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
        private LocalDateTime startDate;
        private LocalDateTime finishDate;
        private Long minimumPrice;
        private String saleUserEmail;
        private String productName;
        private String productDetail;
        private String productCategory;
        private List<String> files;
    }

    @Getter
    @Builder
    public static class GetList {
        private Long id;
        private String title;
        private String introduce;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;
        private String finishDate;
        private Integer maxPersonnel;
        private Integer curPersonnel;
        private Long minimumPrice;
        private Long productId;
        private Long price;
        private String productCategory;
        private String thumbnail;
        private Boolean isStarted;
        private Boolean isEnded;

        public void updateProductInfo(String productCategory, String thumbnail){
            this.productCategory = productCategory;
            this.thumbnail = thumbnail;
        }
        public void updateMinimumPrice(Long minimumPrice){
            this.minimumPrice = minimumPrice;
        }
        public void updateCurPersonnel(Integer curPersonnel){
            this.curPersonnel = curPersonnel;
        }
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
        private Boolean isSuccess;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BeforeStart{
        private Long id;
        private String title;
        private String introduce;
        private LocalDateTime startDate;
        private LocalDateTime finishDate;
        private Long minDeposit;
        private Integer currentPersonnel;
        private String thumbnail;
        private Integer maxPersonnel;
        private Long minimumPrice;
        private Long price;
        private String productCategory;
    }
}
