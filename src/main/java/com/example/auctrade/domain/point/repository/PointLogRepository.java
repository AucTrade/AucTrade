package com.example.auctrade.domain.point.repository;


import com.example.auctrade.domain.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
}
