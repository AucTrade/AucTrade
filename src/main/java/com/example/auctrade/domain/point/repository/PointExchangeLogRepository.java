package com.example.auctrade.domain.point.repository;


import com.example.auctrade.domain.point.entity.PointExchangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointExchangeLogRepository extends JpaRepository<PointExchangeLog, Long> {
}
