package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.auctrade.global.constant.Constants.REDIS_AUCTION_KEY;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 회원이 입력한 여행후기 게시글 저장
     * @param requestDto 회원이 입력한 경매 정보
     * @return 생성된 경매 정보
     */
    public AuctionDTO.Get save(AuctionDTO.Create requestDto) {
        User user = findUser(requestDto.getSaleUserEmail());
        ProductCategory category = findCategory(requestDto.getProductCategory());
        Product product = productRepository.save(ProductMapper.toEntity(new ProductDTO(requestDto.getProductName(), requestDto.getProductDetail(), category.getId()), category, user));

        return AuctionMapper.toDto(auctionRepository.save(AuctionMapper.toEntity(requestDto, product, user)));
    }

    /**
     * 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Transactional(readOnly = true)
    public List<AuctionDTO.GetList> findAll() {

        return auctionRepository.findByStartedFalse().stream().map(res ->{
            Object obj = redisTemplate.opsForHash().get(REDIS_AUCTION_KEY + res.getId(), "bid");
            if (obj == null) return AuctionMapper.toDtoList(res);
            return AuctionMapper.toDtoList(res, Long.parseLong(obj.toString()));
            }).toList();
    }

    /**
     * 경매 정보 조회
     * @param id 경매 id
     * @return 조회된 경매 정보
     */
    @Transactional(readOnly = true)
    public AuctionDTO.Enter enter(Long id) {
        Object obj = redisTemplate.opsForHash().get(REDIS_AUCTION_KEY + id, "bid");

        return (obj == null) ? AuctionMapper.toEnterDTO(findAuction(id)):
                AuctionMapper.toEnterDTO(findAuction(id), Long.parseLong(obj.toString()));
    }

    /**
     * 지정된 경매 시작일 보다 빠르게 경매를 시작
     * @param id 경매 id
     */
    public void startAuction(Long id) {
        findAuction(id).start();
    }
    
    /**
     * 지정된 경매 종료일 보다 빠르게 경매를 종료
     * @param id 경매 id
     */
    public void endAuction(Long id) {
        findAuction(id).end();
    }

    private Auction findAuction(Long id){
        return auctionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private User findUser(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private ProductCategory findCategory(String categoryName){
        return productCategoryRepository.findByCategoryName(categoryName).orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }
}
