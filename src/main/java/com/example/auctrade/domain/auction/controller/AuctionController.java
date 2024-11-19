package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping("/my/auctions")
    public ResponseEntity<AuctionDto.GetPage> getAllMyAuction(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "all") String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok( auctionService.getAllMyAuctions(page, size, userDetails.getUsername(), status));
    }

    @PostMapping(value = "/auctions", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuctionDto.Result> createAuction(@RequestPart(value = "request") AuctionDto.Create auctionDTO, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.createAuction(auctionDTO, imgFiles, userDetails.getUsername()));
    }

    @GetMapping("/auctions/enter/{auctionId}")
    public ResponseEntity<AuctionDto.Enter> getAuction(@PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.getAuctionById(auctionId));
    }

    @PostMapping("/auctions/{auctionId}/deposits")
    public ResponseEntity<AuctionDto.Result> depositAuction(@RequestBody AuctionDto.PutDeposit requestDto, @PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.placeDeposit(requestDto, auctionId, userDetails.getUsername()));
    }

    @PutMapping("/auctions/{auctionId}/deposits")
    public ResponseEntity<AuctionDto.Result> cancelDeposit(@RequestBody AuctionDto.PutDeposit requestDto, @PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.placeDeposit(requestDto, auctionId, userDetails.getUsername()));
    }

    @GetMapping("/auctions/deposits")
    public ResponseEntity<List<AuctionDto.BeforeStart>> getDepositAuctions(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(auctionService.getAllBeforeStartAuction(page, size));
    }

    @PostMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<AuctionDto.Result> bidAuction(@RequestBody AuctionDto.PutBid requestDto,  @PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.placeBid(requestDto,auctionId,userDetails.getUsername()));
    }

    @PutMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<AuctionDto.Result> cancelBid(@RequestBody AuctionDto.PutDeposit requestDto, @PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.placeDeposit(requestDto, auctionId, userDetails.getUsername()));
    }

}
