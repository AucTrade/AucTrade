package com.example.auctrade.domain.point.controller;

import com.example.auctrade.domain.point.dto.PointDTO;
import com.example.auctrade.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final PointService pointService;

    @PostMapping(value = "/points/recharge")
    public ResponseEntity<PointDTO.Result> rechargePoint(@RequestBody PointDTO.Recharge pointDTO, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(pointService.createPointLog(pointDTO, userDetails.getUsername()));
    }

    @GetMapping("/points/refund")
    public ResponseEntity<PointDTO.Result> refundPoint(@RequestBody PointDTO.Refund pointDTO, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pointService.createPointExchangeLog(pointDTO, userDetails.getUsername()));
    }
}
