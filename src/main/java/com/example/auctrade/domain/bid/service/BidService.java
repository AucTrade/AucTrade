package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;

public interface BidService {

    Boolean placeBid(BidVo bidVo);

    Boolean cancelBid(Long auctionId, String email);

    BidInfoVo getBidInfo(Long auctionId);
}
