package com.example.auctrade.domain.auction.job;

import java.util.List;

/**
 * 경매 종료를 위한 dataSet <br>
 * id: 종료할 기준 auctionId <br>
 */
public class AuctionDataSet {
    private Long auctionId;
    private List<DepositDataSet> depositDataSetList;

    public AuctionDataSet(Long auctionId, List<DepositDataSet> depositDataSetList){
        this.auctionId = auctionId;
        this.depositDataSetList = depositDataSetList;
    }
    public Long getAuctionId(){
        return this.auctionId;
    }
    public List<DepositDataSet> getDepositDataSetList(){
        return this.depositDataSetList;
    }

    @Override
    public String toString() {
        return "{auctionId=" + auctionId + "depositDataSetList=" + depositDataSetList + "}";
    }
}
