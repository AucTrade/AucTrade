package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuctionService {

    AuctionDTO.Get save(AuctionDTO.Create request, MultipartFile[] files);

    List<AuctionDTO.List> findAll();

    AuctionDTO.Enter enter(Long id);

    AuctionDTO.BidResult bid(AuctionDTO.Bid request);

    AuctionDTO.Result deposit(AuctionDTO.Deposit request);

    List<AuctionDTO.DepositList> getDepositList(int page, int size);

    List<Long> findAllActiveAuctionIds();
    void processBids(long id);

    void startAuction(Long id);

    void endAuction(Long id);


}
