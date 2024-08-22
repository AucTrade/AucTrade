package com.example.auctrade.domain.auction.controller;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.service.AuctionTotalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Slf4j
public class AuctionController {

    private final AuctionTotalService auctionTotalService;

//    @GetMapping("")
//    public ResponseEntity<List<AuctionDTO.GetList>> getAuctions(
//            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size) {
//        return ResponseEntity.ok(auctionTotalService.getBeforeStartAuctions(page, size));
//    }


    // 여긴가
    @GetMapping("/my-auctions")
    public ResponseEntity<AuctionDTO.AfterStartList> getMyAuctions(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionTotalService.getMyAuctionPage(page, size, userDetails.getUsername()));
    }

    @PostMapping(value = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuctionDTO.Result> createAuction(@RequestPart(value = "request") AuctionDTO.Create auctionDTO, @RequestPart(value = "imgFiles", required = false) MultipartFile[] imgFiles, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(auctionTotalService.createAuction(auctionDTO, imgFiles, userDetails.getUsername()));
    }

    @GetMapping("/enter/{auctionId}")
    public ResponseEntity<AuctionDTO.Enter> getAuction(@PathVariable Long auctionId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionTotalService.enterAuction(auctionId, userDetails.getUsername()));
    }

    @PostMapping("/deposits")
    public ResponseEntity<DepositDTO.Result> depositAuction(@RequestBody DepositDTO.Create requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auctionTotalService.depositPrice(requestDto, userDetails.getUsername()));
    }
    @GetMapping("/deposits")
    public ResponseEntity<List<AuctionDTO.BeforeStart>> getDepositAuctions(@RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(auctionTotalService.getBeforeStartPage(page, size));
    }

    @GetMapping("/bids/{auctionId}")
    public ResponseEntity<BidDTO.Get> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionTotalService.getBidInfo(auctionId));
    }

    @PostMapping("/bids")
    public ResponseEntity<BidDTO.Result> bidAuction(@RequestBody BidDTO.Create auctionDTO) {
        return ResponseEntity.ok(auctionTotalService.bidPrice(auctionDTO));
    }

    @Scheduled(fixedRate = 1000)
    public void processAllBids() {
        auctionTotalService.findAllActiveAuctionIds().forEach(auctionTotalService::processBids);
    }
}
