package com.example.auctrade.domain.point.mapper;


import com.example.auctrade.domain.point.dto.PointDTO;
import com.example.auctrade.domain.point.entity.PointExchangeLog;
import com.example.auctrade.domain.point.entity.PointLog;


public class PointMapper {
    private PointMapper(){}

    public static PointLog toPointLog(PointDTO.Recharge dto, String email) {
        return (dto == null) ? null : PointLog.builder()
                .userId(email)
                .point(dto.getRecharge())
                .build();
    }

    public static PointExchangeLog toPointExchangeLog(PointDTO.Refund dto, String email) {
        return (dto == null) ? null :PointExchangeLog.builder()
                .userId(email)
                .point(dto.getRefund())
                .account("TBD")
                .build();
    }

    public static PointDTO.Result toResultDTO(PointLog entity) {
        return (entity == null) ? new PointDTO.Result(0L, false) : new PointDTO.Result(entity.getPoint(), true);
    }

    public static PointDTO.Result toResultDTO(PointExchangeLog entity) {
        return (entity == null) ? new PointDTO.Result(0L, false) : new PointDTO.Result(entity.getPoint(), true);
    }
}

