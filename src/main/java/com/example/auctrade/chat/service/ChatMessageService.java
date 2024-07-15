package com.example.auctrade.chat.service;

import com.example.auctrade.chat.document.ChatMessage;
import com.example.auctrade.chat.dto.MessageDTO;
import com.example.auctrade.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public MessageDTO saveMsg(final MessageDTO request) {
        ChatMessage chatMessage = ChatMessage.builder()
                .auctionId(request.getAuctionId())
                .username(request.getUsername())
                .message(request.getMessage())
                .build();
        return new MessageDTO(chatMessageRepository.save(chatMessage));
    }

    public List<MessageDTO> findMsgByRoomId(String roomId) {
        return this.chatMessageRepository.findAllByRoomId(roomId).stream().map(MessageDTO::new).toList();
    }
}