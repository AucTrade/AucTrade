package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.product.dto.ProductDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionMapper {
    private AuctionMapper(){}

    public static AuctionDTO.Get toGetDto(Auction auction) {
        return (auction == null) ? null : AuctionDTO.Get.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .maxPersonnel(auction.getPersonnel())
                .saleUserEmail(auction.getSaleUsername())
                .startDate(auction.getStartDate())
                .minimumPrice(auction.getMinimumPrice())
                .minimumPrice(auction.getPrice())
                .finishDate(auction.getFinishDate())
                .productId(auction.getProductId())
                .build();
    }

    public static AuctionDTO.Enter toEnterDto(AuctionDTO.Get auction, ProductDTO.Get product, BidDTO.Get bid, List<String> files, String email) {
        return (auction == null) ? null : AuctionDTO.Enter.builder()
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .productName(product.getName())
                .productDetail(product.getDetail())
                .productCategory(product.getCategoryName())
                .username(bid.getUsername())
                .minimumPrice(bid.getPrice())
                .saleUserEmail(auction.getSaleUserEmail())
                .startDate(auction.getStartDate())
                .finishDate(auction.getFinishDate())
                .files(files)
                .enterUser(email)
                .build();
    }

    public static Auction toEntity(AuctionDTO.Create auctionDTO, long productId, String saleUsername) {
        return (auctionDTO == null) ? null :Auction.builder()
                .title(auctionDTO.getTitle())
                .introduce(auctionDTO.getIntroduce())
                .personnel(auctionDTO.getMaxPersonnel())
                .productId(productId)
                .saleUsername(saleUsername)
                .startDate(auctionDTO.getStartDate())
                .minimumPrice(auctionDTO.getMinimumPrice())
                .price(auctionDTO.getMinimumPrice())
                .finishDate(auctionDTO.getFinishDate())
                .build();
    }

    public static AuctionDTO.AfterStartList toMyAuctionPage(List<AuctionDTO.My> auctions, long lastPage) {
        return new AuctionDTO.AfterStartList(auctions, lastPage);
    }

    public static AuctionDTO.GetList toGetListDto(Auction auction) {
        return (auction == null) ? null : AuctionDTO.GetList.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .startDate(auction.getStartDate())
                .maxPersonnel(auction.getPersonnel())
                .productId(auction.getProductId())
                .price((long) auction.getMinimumPrice())
                .minimumPrice(auction.getMinimumPrice())
                .build();
    }
    public static AuctionDTO.My toMyDto(AuctionDTO.GetList auctions, String categoryName, String fileUrl, int curPersonnel, long minDeposit) {
        return (auctions == null) ? null : AuctionDTO.My.builder()
                .id(auctions.getId())
                .title(auctions.getTitle())
                .introduce(auctions.getIntroduce())
                .startDate(auctions.getStartDate())
                .productCategory(categoryName)
                .thumbnail(fileUrl)
                .curPersonnel(curPersonnel)
                .maxPersonnel(auctions.getMaxPersonnel())
                .productId(auctions.getProductId())
                .price((long) auctions.getMinimumPrice())
                .minimumPrice(minDeposit)
                .build();
    }

    public static AuctionDTO.BeforeStart toBeforeStartDto(AuctionDTO.GetList auction, DepositDTO.List deposit, String categoryName, String thumbnail) {
        return (auction == null) ? null : AuctionDTO.BeforeStart.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .startDate(auction.getStartDate())
                .minDeposit(deposit.getDeposit())
                .currentPersonnel(deposit.getCurrentPersonnel())
                .thumbnail(thumbnail)
                .maxPersonnel(auction.getMaxPersonnel())
                .price((long) auction.getMinimumPrice())
                .minimumPrice(auction.getMinimumPrice())
                .productCategory(categoryName)
                .build();
    }

    public static AuctionDTO.Result toResultDto(Boolean isSuccess) {
        return (isSuccess == null) ? null : new AuctionDTO.Result(isSuccess);
    }
}

