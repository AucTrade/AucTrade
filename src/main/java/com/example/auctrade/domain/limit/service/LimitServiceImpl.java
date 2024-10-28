package com.example.auctrade.domain.limit.service;

import java.util.List;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	@Override
	@Transactional(readOnly = true)
	public List<LimitDTO.Get> findByUserId(Long userId) {
//		List<Limits> limits = limitRepository.findAllBySaleUserId(userId);

		List<Limits> limits = limitRepository.findAll();

		return limits.stream()
			.map(LimitMapper::toDto)
			.toList();
	}

	@Override
	public LimitDTO.GetPage getMyLimitedPage(int page, int size, String status, String email){
		if(status.equals("all")) return getAllMyLimits(page, size, email);

		return getAllMyLimits(page, size, email);

	}

	private LimitDTO.GetPage getAllMyLimits(int page, int size, String email){
		List<Limits> limits = limitRepository.findAll();
//		Page<Limits> limits = limitRepository.findBySaleUserId(userRepository.findByEmail(email).get().getId(),toPageable(page, size,"saleDate"));
//		return new LimitDTO.GetPage(limits.getContent().stream().map(LimitMapper::toDto).toList(), (long) limits.getTotalPages());
		return new LimitDTO.GetPage(limits.stream().map(LimitMapper::toDto).toList(),3L);
	}

	private Pageable toPageable(int page, int size, String target){
		return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
	}
}
