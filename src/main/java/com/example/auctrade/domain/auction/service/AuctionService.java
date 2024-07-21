package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;

import java.util.List;

public interface AuctionService {

    AuctionDTO.Get save(AuctionDTO.Create requestDto);

    List<AuctionDTO.GetList> findAll();

    AuctionDTO.Enter enter(Long id);

    void startAuction(Long id);

    void endAuction(Long id);


}
