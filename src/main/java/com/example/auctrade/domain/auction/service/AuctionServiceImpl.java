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
import com.example.auctrade.domain.product.service.FileService;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final BidService bidService;
    private final DepositService depositService;
    private final FileService fileService;

    /**
     * 회원이 입력한 여행후기 게시글 저장
     * @param request 회원이 입력한 경매 정보
     * @return 생성된 경매 정보
     */
    public AuctionDTO.Get save(AuctionDTO.Create request, MultipartFile[] files) {
        User user = findUser(request.getSaleUserEmail());
        ProductCategory category = findCategory(request.getProductCategory());
        Product product =
                productRepository.save(ProductMapper.toEntity(new ProductDTO.Create(request.getProductName(), request.getProductDetail(), category.getId()), category, user));
        try {
            fileService.uploadFile(files, product.getId());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return AuctionMapper.toDto(auctionRepository.save(AuctionMapper.toEntity(request, product, user)));
    }
    /**
     * 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Transactional(readOnly = true)
    public List<AuctionDTO.List> findAll() {
        return auctionRepository.findByStartedFalse(toPageable(1,10)).stream().map(res ->{
                Long bid = bidService.findBidPriceByAuctionId(res.getId());
                if (bid == null) return AuctionMapper.toDtoList(res);
                return AuctionMapper.toDtoList(res, bid);
            }).toList();
    }
    /**
     * 진행중인 경매 리스트 전체 조회
     * @return 진행 중인 경매 Id 리스트
     */
    public List<Long> findAllActiveAuctionIds() {
        return auctionRepository.findAllAuctionIds().stream().toList();
    }

    /**
     * 경매 정보 조회
     * @param id 경매 id
     * @return 조회된 경매 정보
     */
    @Transactional(readOnly = true)
    public AuctionDTO.Enter enter(Long id) {
        Auction auction = findAuction(id);
        return AuctionMapper.toEnterDto(auction, bidService.findBidUserByAuctionId(id), bidService.findBidPriceByAuctionId(id), fileService.getFiles(auction.getId()));
    }

    /**
     * 경매 입찰
     * @param request 입찰 관련 정보
     * @return 입찰 성공 여부
     */
    public AuctionDTO.BidResult bid(AuctionDTO.Bid request) {
        return new AuctionDTO.BidResult(request.getAuctionId(), request.getUsername(), request.getPrice(), bidService.updateBidPrice(AuctionMapper.toBidLogDto(request)));
    }
    /**
     * 경매 입찰 처리
     * @param id 진행중인 경매 id
     */
    public void processBids(long id){
        bidService.processBids(id);
    }
    /**
     * 경매 예치금 입금
     * @param request 예치금 관련 정보
     * @return 조회된 경매 정보
     */
    public AuctionDTO.Result deposit(AuctionDTO.Deposit request){
        long id = request.getId();

        if(findMinimumPriceById(id) > request.getDeposit())
            return new AuctionDTO.Result(false);

        return new AuctionDTO.Result(
                depositService.save(AuctionMapper.toDepositDto(request, findMaxPersonnelById(id))));
    }
    /**
     * 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Transactional(readOnly = true)
    public List<AuctionDTO.DepositList> getDepositList(int page, int size) {
        return auctionRepository.findByStartedFalse(toPageable(page, size)).stream().map(res ->
                AuctionMapper.toDepositList(
                        res,
                        depositService.getMinDeposit(res.getId(), findMaxPersonnelById(res.getId())),
                        depositService.getCurrentPersonnel(res.getId()))
                ).toList();
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

    private int findMaxPersonnelById(Long id){
        return auctionRepository.findPersonnelById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private int findMinimumPriceById(Long id){
        return auctionRepository.findMinimumPriceById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private Auction findAuction(Long id){
        return auctionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private User findUser(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private ProductCategory findCategory(String categoryName){
        return productCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }
    private Pageable toPageable(int page, int size){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
