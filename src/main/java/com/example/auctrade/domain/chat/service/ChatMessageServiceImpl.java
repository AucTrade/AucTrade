package com.example.auctrade.domain.chat.service;

import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.factory.ChatMessageFactory;
import com.example.auctrade.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageFactory<AuctionChatMessage> chatMessageFactory;

    /**
     * 메시지 DB 저장
     * @param requestDto 메시지 정보
     * @return 저장된 메시지 정보 반환
     */
    @Override
    public MessageDTO.Get saveChatMessage(MessageDTO.Create requestDto) {
        AuctionChatMessage auctionChatMessage =
                chatMessageFactory.orderChatMessage(requestDto.getUsername(), requestDto.getMessage(), requestDto.getAuctionId());
        return new MessageDTO.Get(chatMessageRepository.save(auctionChatMessage));
    }

    /**
     * 경매 메시지 리스트 불러오기
     * @param auctionId 불러올 경매 ID
     * @return 저장된 경매 메시지 리스트
     */
    @Override
    public List<MessageDTO.Get> findLog(String auctionId) {
        return chatMessageRepository.
                findAllByAuctionId(auctionId).stream().map(MessageDTO.Get::new).toList();
    }

    /**
     * 경매 입찰 메시지 리스트 불러오기
     * @param auctionId 불러올 경매 ID
     * @return 저장된 경매 입찰 메시지 리스트
     */
    @Override
    public List<MessageDTO.Get> findAuctionLog(String auctionId) {
        return chatMessageRepository.
                findAllByAuctionIdAndBidTrue(auctionId).stream().map(MessageDTO.Get::new).toList();
    }
}