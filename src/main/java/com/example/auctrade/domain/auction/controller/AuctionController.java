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

    // view auction list
    @GetMapping
    public ResponseEntity<List<AuctionDTO>> getAuctions() {
        List<AuctionDTO> auctionDTOS = auctionService.findAll();
        return ResponseEntity.ok(auctionDTOS);
    }

    // create auction
    @PostMapping
    public ResponseEntity<AuctionDTO> createAuction(@RequestBody AuctionDTO auctionDTO) {
        AuctionDTO savedAuction = auctionService.save(auctionDTO);
        return ResponseEntity.ok(savedAuction);
    }

    // close action
    @PatchMapping("/{auctionId}")
    public ResponseEntity<String> closeAuction(@PathVariable Long auctionId) {
        auctionService.endAuction(auctionId);
        return ResponseEntity.ok("success message");
    }
}
