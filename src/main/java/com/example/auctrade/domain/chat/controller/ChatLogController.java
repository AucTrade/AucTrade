package com.example.auctrade.domain.chat.controller;

import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatLogController {

    private final ChatMessageService chatMessageService;

    // 채팅 로그 조회
    @GetMapping("/{auctionId}")
    private ResponseEntity<List<MessageDTO>> getChatLogs(@PathVariable Long auctionId) {
        List<MessageDTO> log = chatMessageService.findLog(auctionId.toString());
        return ResponseEntity.ok(log);
    }

    // 채팅 로그 중 경매 내역 로그만 조회
    @GetMapping("/{auctionId}/auction")
    private ResponseEntity<List<MessageDTO>> getAuctionLogs(@PathVariable Long auctionId) {
        List<MessageDTO> auctionLog = chatMessageService.findAuctionLog(auctionId.toString());
        return ResponseEntity.ok(auctionLog);
    }
}
