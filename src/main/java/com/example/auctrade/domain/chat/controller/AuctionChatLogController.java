package com.example.auctrade.domain.chat.controller;

import com.example.auctrade.domain.chat.dto.AuctionMessageDto;
import com.example.auctrade.domain.chat.service.AuctionChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class AuctionChatLogController {
    private final AuctionChatMessageService auctionChatMessageService;
    public AuctionChatLogController(AuctionChatMessageService auctionChatMessageService){
        this.auctionChatMessageService = auctionChatMessageService;
    }

    @GetMapping("/auctions/{auctionId}")
    private ResponseEntity<List<AuctionMessageDto.Get>> getChatLogs(@PathVariable Long auctionId) {
        List<AuctionMessageDto.Get> log = auctionChatMessageService.findLog(auctionId);
        return ResponseEntity.ok(log);
    }
}
