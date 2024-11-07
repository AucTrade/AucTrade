package com.example.auctrade.domain.limit.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import com.example.auctrade.domain.limit.dto.LimitDTO;

public interface LimitService {
	LimitDTO.Get save(LimitDTO.Create limitDTO, MultipartFile[] imgFiles, String email) throws IOException;

	List<LimitDTO.Get> findAll();

	LimitDTO.Get findById(Long id);

	void endLimit(Long id);

	List<LimitDTO.Get> findByUserEmail(String email);

	LimitDTO.GetPage getMyLimitedPage(int page, int size, String status, String email);
}
