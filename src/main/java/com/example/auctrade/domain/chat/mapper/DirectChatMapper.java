package com.example.auctrade.domain.chat.mapper;

import com.example.auctrade.domain.chat.dto.DirectChatMessageDTO;
import com.example.auctrade.domain.chat.entity.DirectChatMessage;

public class DirectChatMapper {
    private DirectChatMapper(){}

    public static DirectChatMessage toEntity(DirectChatMessageDTO.Create requestDto) {
        return (requestDto == null) ? null : new DirectChatMessage(requestDto.getUsername(), requestDto.getMessage(), requestDto.getChatRoomId());
    }
}

