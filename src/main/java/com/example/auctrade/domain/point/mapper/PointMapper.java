package com.example.auctrade.domain.point.mapper;


import com.example.auctrade.domain.point.dto.PointDTO;
import com.example.auctrade.domain.point.entity.Point;


public class PointMapper {
    private PointMapper(){}

    public static Point rechargeLog(PointDTO.Recharge dto, String email) {
        return (dto == null) ? null : Point.builder()
                .userId(email)
                .amount(dto.getRecharge())
                .build();
    }
    public static Point exchangeLog(PointDTO.Exchange dto, String email) {
        return (dto == null) ? null : Point.builder()
                .userId(email)
                .amount(dto.getExchange())
                .build();
    }


    public static PointDTO.Result toResultDTO(Point entity) {
        return (entity == null) ? new PointDTO.Result(0, false) : new PointDTO.Result(entity.getAmount(), true);
    }
}

