package com.example.auctrade.domain.chat.service;

import com.example.auctrade.domain.chat.entity.AuctionChatMessage;
import com.example.auctrade.domain.chat.dto.AuctionMessageDto;
import com.example.auctrade.domain.chat.mapper.AuctionChatMapper;
import com.example.auctrade.domain.chat.repository.AuctionChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Slf4j(topic = "Auction Chat Message Service")
public class AuctionChatMessageServiceImpl implements AuctionChatMessageService {
    private final AuctionChatMessageRepository auctionChatMessageRepository;

    public AuctionChatMessageServiceImpl(AuctionChatMessageRepository auctionChatMessageRepository) {
        this.auctionChatMessageRepository = auctionChatMessageRepository;
    }


    @Override
    public AuctionMessageDto.Get createEnterChatMessage(Long auctionId, String email) {
        AuctionChatMessage auctionChatMessage = auctionChatMessageRepository
                .save(AuctionChatMapper.toEntity(auctionId, email, email + " 님이 채팅방에 입장하였습니다."));

        return AuctionChatMapper.toGetDto(auctionChatMessage);
    }

    /**
     * 메시지 DB 저장
     * @param requestDto 메시지 정보
     * @return 저장된 메시지 정보 반환
     */
    @Override
    public AuctionMessageDto.Get createChatMessage(AuctionMessageDto.Create requestDto, String email) {
        AuctionChatMessage auctionChatMessage = auctionChatMessageRepository.save(AuctionChatMapper.toEntity(requestDto, email));
        return AuctionChatMapper.toGetDto(auctionChatMessage);
    }


    /**
     * 경매 메시지 리스트 불러오기
     * @param auctionId 불러올 경매 ID
     * @return 저장된 경매 메시지 리스트
     */
    @Override
    public List<AuctionMessageDto.Get> findLog(Long auctionId) {
        return auctionChatMessageRepository.
                findAllByAuctionId(auctionId).stream().map(AuctionChatMapper::toGetDto).toList();
    }

}