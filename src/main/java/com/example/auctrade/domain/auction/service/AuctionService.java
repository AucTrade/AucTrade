package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;

import java.util.List;

public interface AuctionService {

    AuctionDTO.Get save(AuctionDTO.Create request);

    List<AuctionDTO.List> findAll();

    AuctionDTO.Enter enter(Long id);

    AuctionDTO.Result bid(AuctionDTO.Bid request);

    AuctionDTO.Result deposit(AuctionDTO.Deposit request);

    List<AuctionDTO.DepositList> getDepositList();

    void startAuction(Long id);

    void endAuction(Long id);


}
