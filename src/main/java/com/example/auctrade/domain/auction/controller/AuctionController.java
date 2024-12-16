package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.auction.service.AuctionService;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
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
public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService){
        this.auctionService = auctionService;
    }

    @GetMapping("/my/auctions")
    public ResponseEntity<AuctionDto.GetPage> getAllMyAuction(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "all") String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.getAllMyAuctions(page, size, userDetails.getUsername(), status));
    }

    @PostMapping(value = "/auctions", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuctionDto.Result> createAuction(@RequestPart(value = "request") AuctionDto.Create requestDto, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.createAuction(requestDto, imgFiles, userDetails.getUsername()));
    }

    @GetMapping(value = "/auctions")
    public ResponseEntity<List<AuctionDto.BeforeStart>> getBeforeAuctionPage(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(auctionService.getAllBeforeStartAuction(page, size));
    }

    @GetMapping("/auctions/{auctionId}/enter")
    public ResponseEntity<AuctionDto.Enter> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuctionById(auctionId));
    }

    @PostMapping("/auctions/{auctionId}/deposits")
    public ResponseEntity<AuctionDto.Result> placeDeposit(@RequestBody AuctionDto.Deposit requestDto, @PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.placeDeposit(requestDto, auctionId, userDetails.getUsername()));
    }

    @GetMapping("/auctions/{auctionId}/deposits")
    public ResponseEntity<List<DepositInfoVo>> getAllAuctionDeposit(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAllDeposit(auctionId));
    }

    @DeleteMapping("/auctions/{auctionId}/deposits")
    public ResponseEntity<AuctionDto.Result> cancelDeposit(@PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.cancelDeposit(auctionId, userDetails.getUsername()));
    }

    @PostMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<AuctionDto.BidResult> bidAuction(@RequestBody AuctionDto.Bid requestDto, @PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.placeBid(requestDto,auctionId,userDetails.getUsername()));
    }

    @PostMapping("/auctions/{auctionId}/complete")
    public ResponseEntity<AuctionDto.Result> completeAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.completeAuction(auctionId));
    }

    @GetMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<List<BidInfoVo>> getAllAuctionBid(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAllBid(page, size, auctionId));
    }
    @DeleteMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<AuctionDto.Result> cancelBid(@PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionService.cancelBid(auctionId, userDetails.getUsername()));
    }
}
