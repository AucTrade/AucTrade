package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.user.entity.User;

import java.time.format.DateTimeFormatter;

public class AuctionMapper {

    public static AuctionDTO.Get toDto(Auction auction) {
        return (auction == null) ? null : AuctionDTO.Get.builder()
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .maxPersonnel(auction.getPersonnel())
                .productName(auction.getProduct().getName())
                .saleUserEmail(auction.getSaleUser().getEmail())
                .startDate(auction.getStartDate())
                .minimumPrice(auction.getMinimumPrice())
                .minimumPrice(auction.getPrice())
                .finishDate(auction.getFinishDate())
                .build();
    }

    public static AuctionDTO.Enter toEnterDTO(Auction auction) {
        return (auction == null) ? null : AuctionDTO.Enter.builder()
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .productName(auction.getProduct().getName())
                .saleUserEmail(auction.getSaleUser().getEmail())
                .startDate(auction.getStartDate())
                .minimumPrice((long) auction.getMinimumPrice())
                .finishDate(auction.getFinishDate())
                .build();
    }

    public static AuctionDTO.Enter toEnterDTO(Auction auction, Long price) {
        return (auction == null) ? null : AuctionDTO.Enter.builder()
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .productName(auction.getProduct().getName())
                .saleUserEmail(auction.getSaleUser().getEmail())
                .startDate(auction.getStartDate())
                .minimumPrice(price)
                .finishDate(auction.getFinishDate())
                .build();
    }

    // DTO -> Entity
    public static Auction toEntity(AuctionDTO.Create auctionDTO, Product product, User saleUser) {
        return (auctionDTO == null) ? null :Auction.builder()
                .title(auctionDTO.getTitle())
                .introduce(auctionDTO.getIntroduce())
                .personnel(auctionDTO.getMaxPersonnel())
                .product(product)
                .saleUser(saleUser)
                .startDate(auctionDTO.getStartDate())
                .minimumPrice(auctionDTO.getMinimumPrice())
                .price(auctionDTO.getMinimumPrice())
                .finishDate(auctionDTO.getFinishDate())
                .build();
    }

    public static AuctionDTO.GetList toDtoList(Auction auction) {
        return (auction == null) ? null : AuctionDTO.GetList.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .startDate(auction.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .maxPersonnel(auction.getPersonnel())
                .price((long) auction.getMinimumPrice())
                .minimumPrice(auction.getMinimumPrice())
                .productCategory(auction.getProduct().getCategory().getCategoryName())
                .build();
    }

    public static AuctionDTO.GetList toDtoList(Auction auction, Long price) {
        return (auction == null) ? null : AuctionDTO.GetList.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .startDate(auction.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .maxPersonnel(auction.getPersonnel())
                .price(price)
                .minimumPrice(auction.getMinimumPrice())
                .productCategory(auction.getProduct().getCategory().getCategoryName())
                .build();
    }

    public static AuctionDTO.BidResult toBidResultDTO(BidLog bidLog, boolean isSuccess) {
        return (bidLog == null) ? null : AuctionDTO.BidResult.builder()
                .auctionId(bidLog.getAuctionId())
                .username(bidLog.getUsername())
                .price(bidLog.getPrice())
                .isSuccess(isSuccess)
                .build();
    }

    public static BidLog toEntity(AuctionDTO.Bid bidDto) {
        return (bidDto == null) ? null : new BidLog(bidDto);
    }
}

