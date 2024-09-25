package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuctionTotalService {
    AuctionDTO.Result createAuction(AuctionDTO.Create request, MultipartFile[] files, String email) throws IOException;

    AuctionDTO.GetPage getMyAuctionPage(int page, int size, String status, String email);

    AuctionDTO.Enter enterAuction(Long id, String email);

    BidDTO.Result bidPrice(BidDTO.Create request);

    DepositDTO.Result depositPrice(DepositDTO.Create requestDto, String email);

    List<AuctionDTO.BeforeStart> getBeforeStartPage(int page, int size);

    List<Long> findAllActiveAuctionIds();

    BidDTO.Get getBidInfo(Long auctionId);

    void processBids(long id);

    AuctionDTO.Result startAuction(Long id, String email);

    AuctionDTO.Result endAuction(Long id, String email);
}
