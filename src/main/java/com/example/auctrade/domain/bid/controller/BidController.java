package com.example.auctrade.domain.bid.controller;

import com.example.auctrade.domain.bid.service.BidService;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class BidController {
    private final BidService bidService;

    public BidController(BidService bidService){
        this.bidService = bidService;
    }

    @GetMapping("/my/bids")
    public ResponseEntity<List<BidInfoVo>> getAllMyBid(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bidService.getAllMyBid(page, size, userDetails.getUsername()));
    }
}
