package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.bid.service.BidService;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.deposit.service.DepositService;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.product.entity.ProductFile;
import com.example.auctrade.domain.product.service.ProductFileService;
import com.example.auctrade.domain.product.service.ProductService;
import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "Auction Service")
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductService productService;
    private final ProductFileService productFileService;
    private final BidService bidService;
    private final UserService userService;
    private final DepositService depositService;

    public AuctionServiceImpl(AuctionRepository auctionRepository, ProductService productService, ProductFileService productFileService, BidService bidService, UserService userService, DepositService depositService){
        this.auctionRepository = auctionRepository;
        this.productService = productService;
        this.productFileService = productFileService;
        this.bidService = bidService;
        this.userService = userService;
        this.depositService = depositService;
    }

    /**
     * 경매 등록
     * @param request 등록할 경매 정보
     * @param files 제품 이미지 파일
     * @param email 등록한 유저 email
     * @return 생성된 경매 ID
     */
    @Override
    public AuctionDto.Result createAuction(AuctionDto.Create request, MultipartFile[] files, String email) {
        LocalDateTime now = LocalDateTime.now();

        if(request.getStartAt().isBefore(now))
            throw new CustomException(ErrorCode.WRONG_AUCTION_STARTAT);

        if(request.getEndAt().isBefore(request.getStartAt().plusHours(1L)))
            throw new CustomException(ErrorCode.WRONG_AUCTION_ENDAT);

        UserDto.Info userInfo = userService.getUserInfo(email);
        long productId = productService.createProduct(AuctionMapper.toProductDto(request), userInfo.getUserId()).getProductId();

        try {
            productFileService.uploadFile(files, productId);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);
        }

        Auction auction = auctionRepository.save(AuctionMapper.toEntity(request, productId, userInfo.getUserId()));
        return AuctionMapper.toResultDto(auction.getId(), true);
    }

    /**
     * 경매 조회
     * @param auctionId 조회할 경매 ID
     * @return 조회한 경매 정보
     */
    @Override
    public AuctionDto.Enter getAuctionById(long auctionId) {
        Auction auction = findAuction(auctionId);
        UserDto.Info userInfo = userService.getUserInfo(auction.getUserId());
        return AuctionMapper.toEnterDto(auction, userInfo.getEmail(), productService.getProduct(auction.getProductId()), productFileService.getFiles(auctionId), bidService.getBidUserInfo(auctionId));
    }

    /**
     * 아직 시작하지 않은 경매 리스트를 반환
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @return 경매 리스트
     */
    @Override
    public List<AuctionDto.BeforeStart> getAllBeforeStartAuction(int page, int size) {
        Page<Auction> auctions = auctionRepository.findBeforeStartAuctions(LocalDateTime.now(), toPageable(page, size, "createdAt"));
        List<AuctionDto.BeforeStart> result = new ArrayList<>();
        for(Auction auction : auctions.getContent()){
            ProductFile file = productFileService.getThumbnail(auction.getProductId());
            result.add(AuctionMapper.toBeforeStartDto(
                    auction,
                    depositService.getMinDepositAmount(auction.getId()),
                    depositService.getNowParticipants(auction.getId()),
                    productService.getProduct(auction.getProductId()).getCategoryName(),
                    file == null ? null : file.getFilePath()));
        }
        return result;
    }
    
    /**
     * 경매 리스트 조회
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @param email 조회할 경매 판매자 이메일
     * @return 진행 중인 해당 경매 정보
     */
    @Override
    public AuctionDto.GetPage getAllMyAuctions(int page, int size, String email, String status) {
        Page<Auction> auctions;
        String sort = "startAt";
        UserDto.Info userInfo = userService.getUserInfo(email);

        if(status.equals("before"))
            auctions = auctionRepository.findAllBeforeStartAuctionsByUserId(LocalDateTime.now(), userInfo.getUserId(), toPageable(page, size, sort));

        else if(status.equals("open"))
            auctions = auctionRepository.findActivateAuctionsByUserId(LocalDateTime.now(), userInfo.getUserId(), toPageable(page, size, sort));

        else if(status.equals("close"))
            auctions = auctionRepository.findEndAuctionsByUserId(LocalDateTime.now(), userInfo.getUserId(), toPageable(page, size, sort));

        else
            auctions = auctionRepository.findByUserId(userInfo.getUserId(), toPageable(page, size, sort));

        return AuctionMapper.toGetPageDto(auctionListToDto(auctions.getContent()), auctions.getTotalPages());
    }

    /**
     * 경매 예치금 등록
     * @param request 예치금 정보
     * @param auctionId 대상 경매 ID
     * @param email 예치금 등록한 유저 email
     * @return 예치금 등록 성공 여부
     */
    @Override
    public AuctionDto.Result placeDeposit(AuctionDto.Deposit request, Long auctionId, String email) {
        Auction auction = findAuction(auctionId);
        UserDto.Info userInfo = userService.getUserInfo(email);
        if (request.getAmount() < auction.getMinimumPrice())
            throw new CustomException(ErrorCode.WRONG_DEPOSIT_AMOUNT);

        if(auction.getStartAt().isBefore(LocalDateTime.now()))
            throw new CustomException(ErrorCode.WRONG_DEPOSIT_DATE);

        depositService.placeDeposit(AuctionMapper.toDepositVo(request, auction.getMaxParticipants(), auctionId,userInfo.getUserId()));
        return AuctionMapper.toResultDto(auctionId,true);
    }

    /**
     * 특정 경매의 유효한 예치금 리스트
     * @param auctionId 대상 경매 ID
     * @return 유효한 예치금 리스트
     */
    @Override
    public List<DepositInfoVo> getAllDeposit(Long auctionId) {
        return depositService.getAllDepositInfo(auctionId);
    }

    /**
     * 경매 예치금 취소
     * @param auctionId 대상 경매 ID
     * @param email 예치금 취소할 유저 email
     * @return 예치금 취소 성공 여부
     */
    @Override
    public AuctionDto.Result cancelDeposit(Long auctionId, String email) {
        Auction auction = findAuction(auctionId);
        UserDto.Info userInfo = userService.getUserInfo(email);
        return AuctionMapper.toResultDto(auction.getId(), depositService.cancelDeposit(auctionId, userInfo.getUserId()));
    }

    /**
     * 경매 입찰 등록
     * @param request 입찰 정보
     * @param auctionId 대상 경매 ID
     * @param email 입찰한 유저 email
     * @return 입찰 성공 여부
     */
    @Override
    public AuctionDto.BidResult placeBid(AuctionDto.Bid request, Long auctionId, String email) {
        Auction auction = findAuction(auctionId);
        UserDto.Info userInfo = userService.getUserInfo(email);
        LocalDateTime now = LocalDateTime.now();

        if(request.getAmount() < auction.getMinimumPrice())
            throw new CustomException(ErrorCode.WRONG_BID_CREATE);

        if(now.isBefore(auction.getStartAt()) || auction.getEndAt().isBefore(now))
            throw new CustomException(ErrorCode.WRONG_BID_DATE);

        Boolean result = bidService.placeBid(AuctionMapper.toBidVo(request, auctionId,userInfo.getUserId(), email));
        return AuctionMapper.toBidResultDto(auctionId, request.getAmount(), result);
    }
    /**
     * 입찰 정보 리스트 조회
     * @param page 페이지 정보
     * @param size 데이터 수
     * @param auctionId 대상 경매 ID
     * @return 입찰 정보 리스트
     */
    @Override
    public List<BidInfoVo> getAllBid(Integer page, Integer size, Long auctionId) {
        return bidService.getAllByAuctionId(page, size, auctionId);
    }

    /**
     * 경매 입찰 취소
     * @param auctionId 대상 경매 ID
     * @param email 입찰 취소할 유저 email
     * @return 입찰 취소 성공 여부
     */
    @Override
    public AuctionDto.Result cancelBid(Long auctionId, String email) {
        Auction auction = findAuction(auctionId);
        return AuctionMapper.toResultDto(auction.getId(), bidService.cancelBid(auctionId, email));
    }

    /**
     * 경매 종료
     * @param auctionId 대상 경매 ID
     * @return 경매 종료 성공 여부
     */
    @Override
    public AuctionDto.Result completeAuction(Long auctionId) {
        Auction auction = findAuction(auctionId);
        return AuctionMapper.toResultDto(auction.getId(), bidService.completeBid(auctionId));
    }


    /**
     * 경매 시작 시간 조회
     * @param id 조회할 경매 ID
     * @return 경매 시작 시간
     */
    @Override
    public String getStartAt(Long id) {
        return auctionRepository.findStartAtById(id).toString();
    }


    /**
     * 경매 최대 인원 조회
     * @param id 조회할 경매 ID
     * @return 해당 경매 최대 인원 수
     */
    @Override
    public int getMaxParticipation(Long id){
        return auctionRepository.findMaxParticipantsById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    /**
     * 경매 최소 입찰금 조회
     * @param id 조회할 경매 ID
     * @return 해당 최소 입찰금
     */
    @Override
    public int getMinimumPrice(Long id){
        return auctionRepository.findMinimumPriceById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private List<AuctionDto.GetList> auctionListToDto(List<Auction> auctions){
        List<AuctionDto.GetList> result = new ArrayList<>();
        for(Auction auction : auctions){
            ProductFile file = productFileService.getThumbnail(auction.getProductId());
            result.add(AuctionMapper.toGetListDto(auction,
                    depositService.getNowParticipants(auction.getId()),
                    productService.getProduct(auction.getProductId()).getCategoryName(),
                    file == null ? null : file.getFilePath()
            ));
        }
        return result;
    }

    private Auction findAuction(Long id){
        return auctionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
