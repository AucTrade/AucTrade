package com.example.auctrade.domain.limit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auctrade.domain.limit.entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
	List<Purchase> findByBuyerId(Long buyerId);
	List<Purchase> findByLimitId(Long limitId);
}
