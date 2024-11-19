package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuctionService {

    AuctionDto.Result createAuction(AuctionDto.Create request, MultipartFile[] files, String email);

    AuctionDto.Enter getAuctionById(long id);

    List<AuctionDto.BeforeStart> getAllBeforeStartAuction(int page, int size);

    AuctionDto.GetPage getAllMyAuctions(int page, int size, String email, String status);

    AuctionDto.Result placeDeposit(AuctionDto.PutDeposit request, Long auctionId, String email);

    AuctionDto.Result cancelDeposit(Long auctionId, String email);

    AuctionDto.Result placeBid(AuctionDto.PutBid request, Long auctionId, String email);

    AuctionDto.Result cancelBid(Long auctionId, String email);

    int getMaxParticipation(Long id);

    int getMinimumPrice(Long id);

    String getStartAt(Long id);
}
