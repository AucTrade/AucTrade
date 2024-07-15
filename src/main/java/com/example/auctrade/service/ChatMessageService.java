package com.example.auctrade.service;

import com.example.auctrade.document.ChatMessage;
import com.example.auctrade.dto.MessageDTO;
import com.example.auctrade.repository.ChatMessageRepository;
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
                .roomId(request.getRoomId())
                .username(request.getUsername())
                .message(request.getMessage())
                .build();
        return new MessageDTO(chatMessageRepository.save(chatMessage));
    }
    public List<MessageDTO> findMsgByRoomId(String roomId) {
        return this.chatMessageRepository.findAllByRoomId(roomId).stream().map(MessageDTO::new).toList();
    }
}