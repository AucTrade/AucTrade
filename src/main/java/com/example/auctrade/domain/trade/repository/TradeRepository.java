package com.example.auctrade.domain.trade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auctrade.domain.trade.entity.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {
	List<Trade> findByBuyerId(Long buyerId);
}
