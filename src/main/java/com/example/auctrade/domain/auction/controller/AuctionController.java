package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.service.AuctionService;
import com.example.auctrade.domain.auction.service.BidLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final BidLogService bidLogService;
    // view auction list
    @GetMapping
    public ResponseEntity<List<AuctionDTO.GetList>> getAuctions() {
        return ResponseEntity.ok(auctionService.findAll());
    }

    // create auction
    @PostMapping
    public ResponseEntity<AuctionDTO.Get> createAuction(@RequestBody AuctionDTO.Create auctionDTO) {
        return ResponseEntity.ok(auctionService.save(auctionDTO));
    }

    // close action
    @GetMapping("/enter/{auctionId}")
    public ResponseEntity<AuctionDTO.Enter> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.enter(auctionId));
    }

    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<AuctionDTO.BidResult> bidAuction(@RequestBody AuctionDTO.Bid auctionDTO, @PathVariable  Long auctionId) {
        return ResponseEntity.ok(bidLogService.updateBidPrice(auctionDTO));
    }
}
