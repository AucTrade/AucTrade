package com.example.auctrade.domain.deposit.controller;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.deposit.service.DepositService;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class DepositController {
    private final DepositService depositService;

    public DepositController(DepositService depositService){
        this.depositService = depositService;
    }

    @GetMapping("/my/deposits")
    public ResponseEntity<List<DepositInfoVo>> getAllMyAuction(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(depositService.getAllMyDepositInfo(page, size, userDetails.getUsername()));
    }
}
