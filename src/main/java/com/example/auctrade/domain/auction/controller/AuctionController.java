package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.service.AuctionService;
import com.example.auctrade.global.vaild.AuctionValidationGroups;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<List<AuctionDTO.List>> getAuctions() {
        return ResponseEntity.ok(auctionService.findAll());
    }

    @PostMapping(value = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuctionDTO.Get> createAuction(@RequestPart(value = "request") AuctionDTO.Create auctionDTO, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles) {
        return ResponseEntity.ok(auctionService.save(auctionDTO, imgFiles));
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
    public ResponseEntity<List<AuctionDTO.DepositList>> getDeposits(@RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(auctionService.getDepositList(page, size));
    }

    @PostMapping("/bids")
    public ResponseEntity<AuctionDTO.BidResult> bidAuction(@RequestBody AuctionDTO.Bid auctionDTO) {
        return ResponseEntity.ok(auctionService.bid(auctionDTO));
    }

    @Scheduled(fixedRate = 1000)
    public void processAllBids() {
        auctionService.findAllActiveAuctionIds().forEach(auctionService::processBids);
    }
}
