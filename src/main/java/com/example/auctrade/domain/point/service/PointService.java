package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.dto.PointDTO;

public interface PointService {

    PointDTO.Result createPointLog(PointDTO.Recharge request, String email);

    PointDTO.Result createPointExchangeLog(PointDTO.Exchange request, String email);

}
