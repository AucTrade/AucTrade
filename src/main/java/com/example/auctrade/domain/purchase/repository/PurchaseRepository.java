package com.example.auctrade.domain.purchase.repository;

import com.example.auctrade.domain.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

	int countByBuyerIdAndLimitId(Long buyerId, Long limitId);
}

