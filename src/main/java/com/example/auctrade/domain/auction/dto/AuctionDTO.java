package com.example.auctrade.domain.auction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuctionDTO {
    private String title;
    private String introduce;
    private int personnel;
    private Long productId;
    private Long saleUserId;
    private LocalDateTime startDate;
    private int minimumPrice;
    private int price;
    private LocalDateTime finishDate;
}
