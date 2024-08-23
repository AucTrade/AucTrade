package com.example.auctrade.domain.limit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final LimitRepository limitRepository;

	@Override
	public LimitDTO.Get save(LimitDTO.Create dto) {
		User user = userRepository.findById(1L).orElseThrow();
		Product product = productRepository.findById(1L).orElseThrow();

		Limits limits = LimitMapper.toEntity(dto, product, user);
		limitRepository.save(limits);

		return LimitMapper.toDto(limits);
	}

	@Override
	@Transactional(readOnly = true)
	public List<LimitDTO.Get> findAll() {
		return limitRepository.findAll().stream()
			.map(LimitMapper::toDto)
			.toList();
	}


	@Override
	@Transactional(readOnly = true)
	public LimitDTO.Get findById(Long id) {
		Limits limit = limitRepository.findById(id).orElseThrow();
		return LimitMapper.toDto(limit);
	}

	@Override
	public void endLimit(Long id) {
		Limits limit = limitRepository.findById(id).orElseThrow();
		limit.end();
	}
}
