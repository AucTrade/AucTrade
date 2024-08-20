package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuctionService {

    Long createAuction(AuctionDTO.Create request, long productId, String saleUsername);

    List<AuctionDTO.GetList> findAll(Pageable pageable);

    AuctionDTO.Get findById(long id);

    List<AuctionDTO.GetList> getMyAuctions(Pageable pageable, String email);

    List<AuctionDTO.GetList> getMyAuctions(List<Long> ids);

    List<AuctionDTO.GetList> getDepositList(Pageable pageable);

    List<Long> findAllActiveAuctionIds();

    void startAuction(Long id);

    void endAuction(Long id);

    int getMaxPersonnel(Long id);

    int getMinimumPrice(Long id);

    int getLastPageNum(String email, int size);

    String getStartAt(Long id);
}
