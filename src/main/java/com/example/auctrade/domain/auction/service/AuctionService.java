package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuctionService {

    Long createAuction(AuctionDTO.Create request, long productId, String saleUsername);

    List<AuctionDTO.GetList> getAuctions(Pageable pageable);

    AuctionDTO.Get getAuctionById(long id);

    List<AuctionDTO.GetList> getNotStartedAuctions(Pageable pageable);

    List<AuctionDTO.GetList> getAllMyAuctions(Pageable pageable, String email);

    AuctionDTO.GetPage getMyNotStartedAuctions(Pageable pageable, String email);

    AuctionDTO.GetPage getMyActiveAuctions(Pageable pageable, String email);

    AuctionDTO.GetPage getMyEndedAuctions(Pageable pageable, String email);

    int getMaxParticipation(Long id);

    int getMinimumPrice(Long id);

    String getStartAt(Long id);
}
