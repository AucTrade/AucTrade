package com.example.auctrade.domain.chat.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.auctrade.domain.chat.document.DirectChatMessage;

@Repository
public interface DirectChatMessageRepository extends MongoRepository<DirectChatMessage, String> {
	// 채팅룸 ID를 기반으로 모든 채팅 메시지를 조회
	List<DirectChatMessage> findAllByDirectChatId(String directChatId);

}
