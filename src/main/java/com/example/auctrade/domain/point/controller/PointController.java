package com.example.auctrade.domain.point.controller;

import com.example.auctrade.domain.point.dto.PointDto;
import com.example.auctrade.domain.point.service.UserPointService;
import com.example.auctrade.domain.point.vo.PointInfoVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class PointController{
    private final UserPointService userPointService;

    public PointController(UserPointService userPointService){
        this.userPointService = userPointService;
    }

    @GetMapping("/points")
    public ResponseEntity<List<PointInfoVo>> getAllMyPoints(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userPointService.getAllPoint(page,size,userDetails.getUsername()));
    }

    @PostMapping("/points/charge")
    public ResponseEntity<PointDto.Result> chargePoint(@RequestBody PointDto.Create requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userPointService.chargePoint(requestDto, userDetails.getUsername()));
    }

    @PostMapping("/points/exchange")
    public ResponseEntity<PointDto.Result> exchangePoint(@RequestBody PointDto.Create requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userPointService.exchangePoint(requestDto, userDetails.getUsername()));
    }

    @DeleteMapping("/points/{pointId}")
    public ResponseEntity<PointDto.Result> cancelPoint(@PathVariable Long pointId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userPointService.cancelPoint(pointId, userDetails.getUsername()));
    }

}
