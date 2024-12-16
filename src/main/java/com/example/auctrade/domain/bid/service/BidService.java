package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidUserInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;

import java.util.List;

public interface BidService {

    Boolean placeBid(BidVo bidVo);

    Boolean cancelBid(Long auctionId, String email);

    BidUserInfoVo getBidUserInfo(Long auctionId);

    List<BidInfoVo> getAllMyBid(int page, int size, String email);

    List<BidInfoVo> getAllByAuctionId(int page, int size, Long auctionId);

    Boolean completeBid(Long auctionId);
}
