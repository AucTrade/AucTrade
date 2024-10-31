package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.product.dto.ProductDTO;

import java.util.List;

public class AuctionMapper {
    private AuctionMapper(){}

    public static AuctionDTO.Get toGetDto(Auction auction) {
        return (auction == null) ? null : AuctionDTO.Get.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getDescription())
                .maxPersonnel(auction.getMaxParticipants())
                .saleUserEmail(auction.getSellerEmail())
                .startDate(auction.getStartTime())
                .minimumPrice(auction.getMinimumPrice())
                .finishDate(auction.getEndTime())
                .productId(auction.getProductId())
                .build();
    }

    public static AuctionDTO.Enter toEnterDto(Auction auction, ProductDTO.Get product, List<String> files) {
        return (auction == null) ? null : AuctionDTO.Enter.builder()
                .title(auction.getTitle())
                .introduce(auction.getDescription())
                .productName(product.getName())
                .productDetail(product.getDetail())
                .productCategory(product.getCategoryName())
                .saleUserEmail(auction.getSellerEmail())
                .startDate(auction.getStartTime())
                .finishDate(auction.getEndTime())
                .files(files)
                .build();
    }

    public static Auction toEntity(AuctionDTO.Create auctionDTO, long productId, String saleUsername) {
        return (auctionDTO == null) ? null :Auction.builder()
                .title(auctionDTO.getTitle())
                .description(auctionDTO.getIntroduce())
                .maxParticipants(auctionDTO.getMaxPersonnel())
                .productId(productId)
                .sellerEmail(saleUsername)
                .startTime(auctionDTO.getStartDate())
                .minimumPrice(auctionDTO.getMinimumPrice())
                .endTime(auctionDTO.getFinishDate())
                .build();
    }

    public static AuctionDTO.GetPage toMyAuctionPage(List<AuctionDTO.GetList> auctions, int lastPage) {
        return new AuctionDTO.GetPage(auctions, lastPage);
    }

    public static AuctionDTO.GetList toGetListDto(Auction auction, int getNowParticipation, String categoryName, String thumbnail) {
        return (auction == null) ? null : AuctionDTO.GetList.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getDescription())
                .startDate(auction.getStartTime())
                .maxPersonnel(auction.getMaxParticipants())
                .productId(auction.getProductId())
                .price(auction.getMinimumPrice())
                .minimumPrice(auction.getMinimumPrice())
                .curPersonnel(getNowParticipation)
                .productCategory(categoryName)
                .thumbnail(thumbnail)
                .build();
    }

    public static AuctionDTO.BeforeStart toBeforeStartDto(Auction auction, DepositDTO.GetList deposit, String categoryName, String thumbnail) {
        return (auction == null) ? null : AuctionDTO.BeforeStart.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getDescription())
                .startDate(auction.getStartTime())
                .minDeposit(deposit.getDeposit())
                .currentPersonnel(deposit.getNowParticipation())
                .thumbnail(thumbnail)
                .maxPersonnel(auction.getMaxParticipants())
                .price(auction.getMinimumPrice())
                .minimumPrice(auction.getMinimumPrice())
                .productCategory(categoryName)
                .build();
    }

    public static AuctionDTO.GetPage toGetPageDto(List<AuctionDTO.GetList> auctions, int lastPage) {
        return new AuctionDTO.GetPage(auctions, lastPage);
    }


    public static DepositDTO.Create toDepositDto(AuctionDTO.PutDeposit requestDto, String email){
        return (requestDto == null) ? null : DepositDTO.Create.builder()
                .auctionId(requestDto.getAuctionId())
                .email(email)
                .deposit(requestDto.getDeposit())
                .build();
    }

    public static AuctionDTO.Result toResultDto(Boolean isSuccess) {
        return (isSuccess == null) ? null : new AuctionDTO.Result(isSuccess);
    }
}

