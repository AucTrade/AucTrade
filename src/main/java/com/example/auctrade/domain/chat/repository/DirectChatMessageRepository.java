package com.example.auctrade.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auctrade.domain.chat.entity.DirectChatMessage;

@Repository
public interface DirectChatMessageRepository extends JpaRepository<DirectChatMessage, Long> {
	// 채팅룸 ID를 기반으로 모든 채팅 메시지를 조회
	List<DirectChatMessage> findAllByDirectChatId(String directChatId);

}
