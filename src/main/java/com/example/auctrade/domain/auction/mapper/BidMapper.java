package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;

public class BidMapper {
    public static BidDTO.Result toBidResultDto(BidDTO.Create bidDto, boolean isSuccess) {
        return (bidDto == null) ? null : BidDTO.Result.builder()
                .auctionId(bidDto.getAuctionId())
                .username(bidDto.getUsername())
                .price(bidDto.getPrice())
                .isSuccess(isSuccess)
                .build();
    }

    public static BidDTO.Get toBidGetDto(BidLog bidEntity) {
        return (bidEntity == null) ? null : BidDTO.Get.builder()
                .auctionId(bidEntity.getAuctionId())
                .username(bidEntity.getUsername())
                .price(bidEntity.getPrice())
                .build();
    }
    public static BidDTO.List toListDto(BidLog bidEntity) {
        return (bidEntity == null) ? null : BidDTO.List.builder()
                .auctionId(bidEntity.getAuctionId())
                .username(bidEntity.getUsername())
                .price(bidEntity.getPrice())
                .build();
    }
}
