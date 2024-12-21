package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidUserInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.domain.deposit.vo.DepositVo;
import com.example.auctrade.domain.product.dto.ProductDto;

import java.util.List;

public class AuctionMapper {
    private AuctionMapper(){}

    public static AuctionDto.Get toGetDto(Auction auction, String email) {
        return (auction == null) ? null : AuctionDto.Get.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .maxParticipants(auction.getMaxParticipants())
                .email(email)
                .startAt(auction.getStartAt())
                .minimumPrice(auction.getMinimumPrice())
                .endAt(auction.getEndAt())
                .productId(auction.getProductId())
                .build();
    }

    public static AuctionDto.Enter toEnterDto(Auction auction, String email, ProductDto.Get product, List<String> files, BidUserInfoVo bidUserInfoVo) {
        return (auction == null) ? null : AuctionDto.Enter.builder()
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .productName(product.getName())
                .productDetail(product.getDetail())
                .productCategory(product.getCategoryName())
                .email(email)
                .startAt(auction.getStartAt())
                .endAt(auction.getEndAt())
                .minimumPrice(auction.getMinimumPrice())
                .files(files)
                .bidUser(bidUserInfoVo.getEmail())
                .bidAmount(bidUserInfoVo.getAmount())
                .build();
    }

    public static Auction toEntity(AuctionDto.Create auctionDto, long productId, Long userId) {
        return (auctionDto == null) ? null :Auction.builder()
                .title(auctionDto.getTitle())
                .introduce(auctionDto.getIntroduce())
                .maxParticipants(auctionDto.getMaxParticipants())
                .productId(productId)
                .userId(userId)
                .startAt(auctionDto.getStartAt())
                .minimumPrice(auctionDto.getMinimumPrice())
                .endAt(auctionDto.getEndAt())
                .isEnded(false)
                .build();
    }

    public static AuctionDto.GetPage toMyAuctionPage(List<AuctionDto.GetList> auctions, int lastPage) {
        return new AuctionDto.GetPage(auctions, lastPage);
    }

    public static AuctionDto.GetList toGetListDto(Auction auction, int nowParticipants, String categoryName, String thumbnail) {
        return (auction == null) ? null : AuctionDto.GetList.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .startAt(auction.getStartAt())
                .endAt(auction.getEndAt())
                .maxParticipants(auction.getMaxParticipants())
                .productId(auction.getProductId())
                .minimumPrice(auction.getMinimumPrice())
                .nowParticipants(nowParticipants)
                .productCategory(categoryName)
                .thumbnail(thumbnail)
                .build();
    }

    public static AuctionDto.BeforeStart toBeforeStartDto(Auction auction, Integer minDeposit, Integer nowParticipants, String categoryName, String thumbnail) {
        return (auction == null) ? null : AuctionDto.BeforeStart.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .startAt(auction.getStartAt())
                .endAt(auction.getEndAt())
                .minDeposit(minDeposit)
                .nowParticipants(nowParticipants)
                .thumbnail(thumbnail)
                .maxParticipants(auction.getMaxParticipants())
                .minimumPrice(auction.getMinimumPrice())
                .productCategory(categoryName)
                .build();
    }

    public static AuctionDto.GetPage toGetPageDto(List<AuctionDto.GetList> auctions, int lastPage) {
        return new AuctionDto.GetPage(auctions, lastPage);
    }


    public static DepositVo toDepositVo(AuctionDto.Deposit requestDto, Integer maxParticipants, Long auctionId, Long userId){
        return (requestDto == null) ? null : DepositVo.builder()
                .auctionId(auctionId)
                .userId(userId)
                .amount(requestDto.getAmount())
                .maxParticipants(maxParticipants)
                .build();
    }

    public static BidVo toBidVo(AuctionDto.Bid requestDto, Long auctionId, Long userId, String email){
        return (requestDto == null) ? null : BidVo.builder()
                .auctionId(auctionId)
                .userId(userId)
                .email(email)
                .amount(requestDto.getAmount())
                .build();
    }

    public static ProductDto.Create toProductDto(AuctionDto.Create auctionDto) {
        return (auctionDto == null) ? null : ProductDto.Create.builder()
                .name(auctionDto.getProductName())
                .detail(auctionDto.getProductDetail())
                .productCategoryId(auctionDto.getProductCategoryId())
                .build();
    }

    public static AuctionDto.Result toResultDto(Long auctionId, Boolean isSuccess) {
        return (isSuccess == null) ? null : new AuctionDto.Result(auctionId, isSuccess);
    }
    public static AuctionDto.BidResult toBidResultDto(Long auctionId, Integer amount, Boolean isSuccess) {
        return (isSuccess == null) ? null : new AuctionDto.BidResult(auctionId, amount, isSuccess);
    }
}

