package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuctionService {

    AuctionDto.Result createAuction(AuctionDto.Create request, MultipartFile[] files, String email);

    AuctionDto.Enter getAuctionById(long id);

    List<AuctionDto.BeforeStart> getAllBeforeStartAuction(int page, int size);

    AuctionDto.GetPage getAllMyAuctions(int page, int size, String email, String status);

    AuctionDto.Result placeDeposit(AuctionDto.Deposit request, Long auctionId, String email);

    List<DepositInfoVo> getAllDeposit(Long auctionId);

    AuctionDto.Result cancelDeposit(Long auctionId, String email);

    AuctionDto.BidResult placeBid(AuctionDto.Bid request, Long auctionId, String email);

    List<BidInfoVo> getAllBid(Integer page ,Integer size, Long auctionId);

    AuctionDto.Result cancelBid(Long auctionId, String email);

    AuctionDto.Result completeAuction(Long auctionId);

    int getMaxParticipation(Long id);

    int getMinimumPrice(Long id);

    String getStartAt(Long id);

}
