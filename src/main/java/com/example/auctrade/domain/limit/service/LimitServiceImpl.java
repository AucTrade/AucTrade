package com.example.auctrade.domain.limit.service;

import java.io.IOException;
import java.util.List;

import com.example.auctrade.domain.product.dto.ProductDto;
import com.example.auctrade.domain.product.service.ProductFileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.product.service.ProductService;
// com.example.auctrade.domain.trade.service.TradeService;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService{
	private final UserService userService;
	private final ProductService productService;
	private final LimitRepository limitRepository;
	private final ProductFileService fileService;
//	private final TradeService tradeService;
@Override
public LimitDTO.Get createLimit(LimitDTO.Create limitDTO, MultipartFile[] imgFiles, String sellerEmail) throws IOException {
	Long sellerId = userService.getUserInfo(sellerEmail).getUserId();

	validateInput(limitDTO, imgFiles, sellerId);

	Long productId = createProductAndUploadFiles(limitDTO, imgFiles, sellerId);

	Limits limits = registerLimit(limitDTO, productId, sellerId);

	ProductDto.Get product = productService.getProduct(productId);

	return LimitMapper.toDto(limits, product, getUserEmailById(sellerId));
}

	private void validateInput(LimitDTO.Create limitDTO, MultipartFile[] imgFiles, Long sellerId) {
		if (limitDTO == null || sellerId == null) {
			throw new IllegalArgumentException("Invalid input data");
		}
	}

	private Long createProductAndUploadFiles(LimitDTO.Create limitDTO, MultipartFile[] imgFiles, Long sellerId) throws IOException {
		ProductDto.Get product = productService.createProduct(ProductDto.Create.builder()
				.name(limitDTO.getProductName())
				.detail(limitDTO.getProductDetail())
				.productCategoryId(limitDTO.getProductCategoryId())
				.build(), sellerId);

		if (!fileService.uploadFile(imgFiles, product.getProductId())) {
			throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);
		}

		return product.getProductId();
	}

	private Limits registerLimit(LimitDTO.Create limitDTO, Long productId, Long sellerId) {
		Limits limits = LimitMapper.toEntity(limitDTO, productId, sellerId);
		return limitRepository.save(limits);
	}

	@Override
	public List<LimitDTO.Get> getAllLimits() {
		List<Limits> limitsList = limitRepository.findAll();

		return limitsList.stream()
			.map(limit -> {
				ProductDto.Get product = getProduct(limit.getProductId());
				return LimitMapper.toDto(limit, product, getUserEmailById(limit.getSellerId()));
			})
			.toList();
	}

	private ProductDto.Get getProduct(Long productId) {
		return productService.getProduct(productId);
	}

	@Override
	public LimitDTO.Get getByLimitId(Long limitId) {
		Limits limit = limitRepository.findById(limitId)
			.orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
		ProductDto.Get product = getProduct(limit.getProductId());
		return LimitMapper.toDto(limit, product, getUserEmailById(limit.getSellerId()));
	}

	@Override
	public void markLimitAsEnded(Long limitId) {
		Limits limit = limitRepository.findById(limitId)
			.orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
		limit.end();
		limitRepository.save(limit);
	}

	@Override
	public List<LimitDTO.Get> getLimitBySellerId(Long userId) {
		List<Limits> limits = limitRepository.findAllBySellerId(userId);
		return limits.stream()
			.map(limit -> LimitMapper.toDto(limit, getProduct(limit.getProductId()), getUserEmailById(limit.getSellerId())))
			.toList();
	}

	private String getUserEmailById(Long userId) {
		return userService.getUserInfo(userId).getEmail();
	}

	private Long getUserIdByEmail(String email) {return userService.getUserInfo(email).getUserId();}


	@Override
	public LimitDTO.GetPage getMyLimitedPage(int page, int size, String status, String email){
		if(status.equals("all")) return getAllMyLimits(page, size, email);

		return getAllMyLimits(page, size, email);

	}

	private LimitDTO.GetPage getAllMyLimits(int page, int size, String email) {
		Long userId = getUserIdByEmail(email);
 		Page<Limits> limits = limitRepository.findBySellerId(userId, toPageable(page, size, "saleDate"));

		List<LimitDTO.Get> limitDTOList = limits.getContent().stream()
			.map(limit -> LimitMapper.toDto(limit, getProduct(limit.getProductId()), email))
			.toList();

		return new LimitDTO.GetPage(limitDTOList, (long) limits.getTotalPages());
	}
	private Pageable toPageable(int page, int size, String target){
		return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
	}

	// public boolean validateLimitTrade(LimitDTO.LimitTradeRequest limitTradeRequest) {
	// 	Long result = executeLuaScriptForLimit(limitTradeRequest.getPostId(), limitTradeRequest.getQuantity(), limitTradeRequest.getBuyer());
	// 	if (result == null) throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
	//
	// 	if (result >= 0 && calculatePrice(limitTradeRequest) <= getUserPoints(limitTradeRequest.getBuyer())) {
	// 		return true;
	// 	} else if (Long.valueOf(-1L).equals(result)) {
	// 		throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
	// 	} else if (Long.valueOf(-2L).equals(result)) {
	// 		throw new CustomException(ErrorCode.USER_LIMIT_EXCEEDED);
	// 	}
	//
	// 	throw new CustomException(ErrorCode.TRADE_PROCESS_FAILED);
	// }
	//
	// private Long executeLuaScriptForLimit(Long postId, int quantity, String buyer) {
	// 	String luaScript = "local stock_key = KEYS[1] " +
	// 		"local purchase_count_key = KEYS[2] " +
	// 		"local user_id = ARGV[1] " +
	// 		"local purchase_quantity = tonumber(ARGV[2]) " +
	// 		"local user_limit = tonumber(ARGV[3]) " +
	//
	// 		"local stock = redis.call('GET', stock_key) " +
	// 		"if not stock or tonumber(stock) < purchase_quantity then " +
	// 		"    return {-1} " +
	// 		"end " +
	//
	// 		"local purchase_count = redis.call('HGET', purchase_count_key, user_id) " +
	// 		"if not purchase_count then " +
	// 		"    purchase_count = 0 " +
	// 		"end " +
	//
	// 		"if tonumber(purchase_count) + purchase_quantity > user_limit then " +
	// 		"    return {-2} " +
	// 		"end " +
	//
	// 		"redis.call('DECRBY', stock_key, purchase_quantity) " +
	// 		"redis.call('HINCRBY', purchase_count_key, user_id, purchase_quantity) " +
	//
	// 		"local remaining_stock = tonumber(stock) - purchase_quantity " +
	// 		"return {remaining_stock} ";
	//
	// 	List<Object> result = redissonClient.getScript().eval(
	// 		RScript.Mode.READ_WRITE,
	// 		luaScript,
	// 		RScript.ReturnType.MULTI,
	// 		java.util.Arrays.asList("limit:" + postId + ":stock", "limit:" + postId + ":purchase_count"),
	// 		buyer, String.valueOf(quantity), String.valueOf(getUserLimit(postId, buyer))
	// 	);
	// 	return (Long) result.get(0);
	// }
	//
	// public int checkLimitStockFromRedis(Long postId) {
	// 	String redisKey = "limit:" + postId + ":stock";
	// 	RBucket<String> stockBucket = redissonClient.getBucket(redisKey);
	// 	String stockStr = stockBucket.get();
	// 	return stockStr != null ? Integer.parseInt(stockStr) : calculateRemainingStock(postId);
	// }
	//
	// private int calculateRemainingStock(Long postId) {
	// 	int totalStock = getLimit(postId).getAmount();
	// 	int purchasedQuantity = tradeService.findTotalPurchasedByPostId(postId); // TradeService를 통해 접근
	// 	int remainingStock = totalStock - purchasedQuantity;
	// 	redissonClient.getBucket("limit:" + postId + ":stock").set(String.valueOf(remainingStock));
	// 	return remainingStock;
	// }
	//
	// private int getUserLimit(Long postId, String buyer) {
	// 	String redisKey = "limit:" + postId + ":user_limit";
	// 	RBucket<String> userLimitBucket = redissonClient.getBucket(redisKey);
	// 	String userLimitStr = userLimitBucket.get();
	// 	if (userLimitStr != null) {
	// 		return Integer.parseInt(userLimitStr);
	// 	}
	// 	Limits limit = getLimit(postId);
	// 	int baseUserLimit = limit.getPersonalLimit();
	// 	int totalPurchased = tradeService.findTotalPurchasedByBuyerAndPostId(buyer, postId);
	// 	int remainingUserLimit = baseUserLimit - totalPurchased;
	// 	userLimitBucket.set(String.valueOf(remainingUserLimit));
	// 	return remainingUserLimit;
	// }
	//
	// private Limits getLimit(Long limitId) {
	// 	return limitRepository.findById(limitId)
	// 		.orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
	// }
	//
	// private long calculatePrice(LimitDTO.LimitTradeRequest limitTradeRequest) {
	// 	Limits limit = getLimit(limitTradeRequest.getPostId());
	// 	return limit.getPrice() * limitTradeRequest.getQuantity();
	// }
	//
	// private long getUserPoints(String buyer) {
	// 	// 실제 포인트 조회 로직 추가 필요
	// 	return userService.getUserPoint(buyer);
	// }
}
