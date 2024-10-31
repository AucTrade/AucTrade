package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.dto.PointDTO;
import com.example.auctrade.domain.point.mapper.PointMapper;
import com.example.auctrade.domain.point.repository.PointLogRepository;
import com.example.auctrade.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PointServiceImpl implements PointService {
    private final PointLogRepository pointLogRepository;
    private final UserService userService;

    @Override
    public PointDTO.Result createPointLog(PointDTO.Recharge request, String email) {
        if(userService.addPoint(request.getRecharge(), email))
            return PointMapper.toResultDTO(pointLogRepository.save(PointMapper.rechargeLog(request, email)));

        return new PointDTO.Result(request.getRecharge(), false);
    }

    @Override
    public PointDTO.Result createPointExchangeLog(PointDTO.Exchange request, String email) {
        if(userService.subPoint(request.getExchange(), email))
            return PointMapper.toResultDTO(pointLogRepository.save(PointMapper.exchangeLog(request, email)));

        return new PointDTO.Result(request.getExchange(), false);
    }
}
