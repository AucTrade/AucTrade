package com.example.auctrade.domain.auction.job;

public class DepositDataSet {
    private Long depositLogId;
    private Long userId;
    private Integer amount;

    public DepositDataSet(Long depositLogId, Long userId, Integer amount){
        this.depositLogId = depositLogId;
        this.userId = userId;
        this.amount = amount;
    }
    public Long getDepositLogId(){
        return this.depositLogId;
    }
    public Long getUserId(){
        return this.userId;
    }

    public Integer getAmount(){
        return this.amount;
    }

    @Override
    public String toString() {
        return "{depositLogId=" + depositLogId + "userId=" + userId + ",amount=" + amount + "}";
    }
}
