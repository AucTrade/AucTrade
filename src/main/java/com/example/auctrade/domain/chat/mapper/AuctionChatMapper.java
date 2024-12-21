package com.example.auctrade.domain.chat.mapper;

import com.example.auctrade.domain.chat.dto.AuctionMessageDto;
import com.example.auctrade.domain.chat.entity.AuctionChatMessage;

public class AuctionChatMapper {
    private AuctionChatMapper(){}

    public static AuctionChatMessage toEntity(Long auctionId, String email, String message) {
        return (auctionId == null) ? null : AuctionChatMessage.builder()
                .auctionId(auctionId)
                .email(email)
                .message(message)
                .build();
    }
    public static AuctionChatMessage toEntity(AuctionMessageDto.Create requestDto, String email) {
        return (requestDto == null) ? null : AuctionChatMessage.builder()
                .auctionId(requestDto.getAuctionId())
                .email(email)
                .message(requestDto.getMessage())
                .build();
    }

    public static AuctionMessageDto.Get toGetDto(AuctionChatMessage auctionChatMessage) {
        return (auctionChatMessage == null) ? null : AuctionMessageDto.Get.builder()
                .id(auctionChatMessage.getId())
                .auctionId(auctionChatMessage.getAuctionId())
                .email(auctionChatMessage.getEmail())
                .message(auctionChatMessage.getMessage())
                .createAt(auctionChatMessage.getCreatedAt())
                .build();
    }
}

