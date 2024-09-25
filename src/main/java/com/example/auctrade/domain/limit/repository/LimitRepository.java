package com.example.auctrade.domain.limit.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auctrade.domain.limit.entity.Limits;
@Repository
public interface LimitRepository extends JpaRepository<Limits, Long> {
	List<Limits> findByStatusTrue();
	int findAmountById(long id);
	int findLimitById(long id);
	List<Limits> findAllBySaleUserId(Long saleUserId);
	Page<Limits> findBySaleUserId(Long saleUserId, Pageable pageable);

}
