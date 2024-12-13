package com.example.auctrade.domain.limit.service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LimitService {

	// 한정 판매 생성
	LimitDTO.Get createLimit(LimitDTO.Create limitDTO, MultipartFile[] imgFiles, String sellerEmail) throws IOException;

	// 모든 한정 판매 조회
	List<LimitDTO.Get> getAllLimits();

	// 특정 한정 판매 조회 (ID 기준)
	LimitDTO.Get getByLimitId(Long limitId);

	// 특정 판매자의 한정 판매 조회
	List<LimitDTO.Get> getLimitBySellerId(Long userId);

	// 한정 판매 상태를 종료로 설정
	void markLimitAsEnded(Long limitId);

	// 특정 판매자의 한정 판매 목록 페이징 처리
	LimitDTO.GetPage getMyLimitedPage(int page, int size, String status, String email);
}
