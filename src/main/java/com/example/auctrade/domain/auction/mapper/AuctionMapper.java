package com.example.auctrade.domain.auction.mapper;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class AuctionMapper {

    // Entity -> DTO
    public static AuctionDTO toDto(Auction auction) {
        if (auction == null) {
            return null;
        }

        return AuctionDTO.builder()
                .title(auction.getTitle())
                .introduce(auction.getIntroduce())
                .personnel(auction.getPersonnel())
                .productId(auction.getProduct() != null ? auction.getProduct().getId() : null)
                .saleUserId(auction.getSaleUser() != null ? auction.getSaleUser().getId() : null)
                .startDate(auction.getStartDate())
                .minimumPrice(auction.getMinimumPrice())
                .price(auction.getPrice())
                .finishDate(auction.getFinishDate())
                .build();
    }

    // DTO -> Entity
    public static Auction toEntity(AuctionDTO auctionDTO, Product product, User saleUser) {
        if (auctionDTO == null) {
            return null;
        }

        return Auction.builder()
                .title(auctionDTO.getTitle())
                .introduce(auctionDTO.getIntroduce())
                .personnel(auctionDTO.getPersonnel())
                .product(product)
                .saleUser(saleUser)
                .startDate(auctionDTO.getStartDate())
                .minimumPrice(auctionDTO.getMinimumPrice())
                .price(auctionDTO.getPrice())
                .finishDate(auctionDTO.getFinishDate())
                .build();
    }

    // List of entities -> list of DTOs
    public static List<AuctionDTO> toDtoList(List<Auction> auctions) {
        if (auctions == null) {
            return null;
        }

        return auctions.stream()
                .map(AuctionMapper::toDto)
                .collect(Collectors.toList());
    }

    // List of DTOs -> list of entities (경매상품과 판매 유저가 제공됐을 때)
    public static List<Auction> toEntityList(List<AuctionDTO> auctionDTOs, Product product, User saleUser) {
        if (auctionDTOs == null) {
            return null;
        }

        return auctionDTOs.stream()
                .map(dto -> toEntity(dto, product, saleUser))
                .collect(Collectors.toList());
    }
}

