package com.example.auctrade.domain.chat.service;

import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.factory.ChatMessageFactory;
import com.example.auctrade.domain.chat.repository.ChatMessageRepository;
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
    private final ChatMessageFactory<AuctionChatMessage> chatMessageFactory;

    @Transactional
    public MessageDTO saveChatMessage(final MessageDTO dto) {
        AuctionChatMessage auctionChatMessage =
                chatMessageFactory.orderChatMessage(
                        dto.getUsername(), dto.getMessage(), dto.getAuctionId()
                );

        return new MessageDTO(chatMessageRepository.save(auctionChatMessage));
    }

    // 채팅 로그 조회
    @Transactional(readOnly = true)
    public List<MessageDTO> findLog(String auctionId) {
        return chatMessageRepository.
                findAllByAuctionId(auctionId).stream().map(MessageDTO::new).toList();
    }

    // 채팅 로그 중 경매 내역 로그만 조회
    @Transactional(readOnly = true)
    public List<MessageDTO> findAuctionLog(String auctionId) {
        return chatMessageRepository.
                findAllByAuctionIdAndBidTrue(auctionId).stream().map(MessageDTO::new).toList();
    }
}