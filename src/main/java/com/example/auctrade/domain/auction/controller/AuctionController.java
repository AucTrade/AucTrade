package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<List<AuctionDTO.List>> getAuctions() {
        return ResponseEntity.ok(auctionService.findAll());
    }

    @PostMapping
    public ResponseEntity<AuctionDTO.Get> createAuction(@RequestBody AuctionDTO.Create auctionDTO) {
        return ResponseEntity.ok(auctionService.save(auctionDTO));
    }

    @GetMapping("/enter/{auctionId}")
    public ResponseEntity<AuctionDTO.Enter> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.enter(auctionId));
    }

    @PostMapping("/deposits")
    public ResponseEntity<AuctionDTO.Result> depositAuction(@RequestBody AuctionDTO.Deposit auctionDTO) {
        return ResponseEntity.ok(auctionService.deposit(auctionDTO));
    }
    @GetMapping("/deposits")
    public ResponseEntity<List<AuctionDTO.DepositList>> getDeposits() {
        return ResponseEntity.ok(auctionService.getDepositList());
    }

    @PostMapping("/bids")
    public ResponseEntity<AuctionDTO.Result> bidAuction(@RequestBody AuctionDTO.Bid auctionDTO) {
        return ResponseEntity.ok(auctionService.bid(auctionDTO));
    }
}
