package com.example.auctrade.domain.trade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auctrade.domain.trade.entity.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {
	List<Trade> findByBuyer(Long buyerId);

	// TradeRepository.java
	@Query("SELECT SUM(t.quantity) FROM Trade t WHERE t.buyer = :buyerId AND t.postId = :postId")
	Integer  findTotalPurchasedByBuyerAndPostId(@Param("buyerId") Long buyerId, @Param("postId") Long postId);

	// 특정 postId에 대해 구매된 총 수량을 계산
	@Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Trade t WHERE t.postId = :postId")
	int findTotalPurchasedByPostId(@Param("postId") Long postId);

}
