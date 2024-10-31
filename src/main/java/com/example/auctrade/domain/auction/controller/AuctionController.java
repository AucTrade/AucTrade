package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.service.AuctionTotalService;
import com.example.auctrade.domain.auction.service.DepositService;
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

    private final AuctionTotalService auctionTotalService;
    private final DepositService depositService;

    @GetMapping("/my/auctions")
    public ResponseEntity<AuctionDTO.GetPage> getAllMyAuction(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "all") String status,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(auctionTotalService.getMyAuctionsByStatus(page, size, status, userDetails.getUsername()));
    }

    @PostMapping(value = "/auctions", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuctionDTO.Result> createAuction(@RequestPart(value = "request") AuctionDTO.Create auctionDTO, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(auctionTotalService.createAuction(auctionDTO, imgFiles, userDetails.getUsername()));
    }

    @GetMapping("/auctions/enter/{auctionId}")
    public ResponseEntity<AuctionDTO.Enter> getAuction(@PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionTotalService.getAuctionInfo(auctionId, userDetails.getUsername()));
    }

    @PostMapping("/auctions/deposits")
    public ResponseEntity<DepositDTO.Result> depositAuction(@RequestBody AuctionDTO.PutDeposit requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(depositService.registerDeposit(AuctionMapper.toDepositDto(requestDto, userDetails.getUsername())));
    }

    @GetMapping("/auctions/deposits")
    public ResponseEntity<List<AuctionDTO.BeforeStart>> getDepositAuctions(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(auctionTotalService.getNotStartedAuctions(page, size));
    }

    @GetMapping("/auctions/bids/{auctionId}")
    public ResponseEntity<BidDTO.Get> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionTotalService.getBidInfo(auctionId));
    }

    @PostMapping("/auctions/bids")
    public ResponseEntity<BidDTO.Result> bidAuction(@RequestBody BidDTO.Create auctionDTO) {
        return ResponseEntity.ok(auctionTotalService.placeBid(auctionDTO));
    }
}
