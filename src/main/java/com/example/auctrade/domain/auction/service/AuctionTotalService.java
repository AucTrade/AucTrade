package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuctionTotalService {
    AuctionDTO.Result createAuction(AuctionDTO.Create request, MultipartFile[] files, String email) throws IOException;

    AuctionDTO.GetPage getMyAuctionsByStatus(int page, int size, String status, String email);

    AuctionDTO.Enter getAuctionInfo(Long id, String email);

    BidDTO.Result placeBid(BidDTO.Create request);

    DepositDTO.Result registerDeposit(AuctionDTO.PutDeposit requestDto, String email);

    List<AuctionDTO.BeforeStart> getNotStartedAuctions(int page, int size);

    BidDTO.Get getBidInfo(Long auctionId);
}
