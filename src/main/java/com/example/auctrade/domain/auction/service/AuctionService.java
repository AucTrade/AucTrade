package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuctionService {

    AuctionDTO.Result createAuction(AuctionDTO.Create request, MultipartFile[] files, String saleUsername);

    AuctionDTO.Enter getAuctionById(long id);

    List<AuctionDTO.BeforeStart> getNotStartedAuctions(int page, int size);

    AuctionDTO.GetPage getAllMyAuctions(int page, int size, String email);

    AuctionDTO.GetPage getMyNotStartedAuctions(int page, int size, String email);

    AuctionDTO.GetPage getMyActiveAuctions(int page, int size, String email);

    AuctionDTO.GetPage getMyEndedAuctions(int page, int size, String email);

    int getMaxParticipation(Long id);

    int getMinimumPrice(Long id);

    String getStartAt(Long id);
}
