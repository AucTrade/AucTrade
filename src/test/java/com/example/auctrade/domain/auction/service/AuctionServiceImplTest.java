package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDto;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.point.repository.PointRepository;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@Transactional
class AuctionServiceImplTest {
    @Autowired
    AuctionService auctionService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductCategoryRepository productCategoryRepository;

    MultipartFile[] mockFiles;

    ProductCategory productCategory;
    Product product;

    @BeforeEach
    void createData(){
        mockFiles = new MultipartFile[]{new MockMultipartFile("file", "testfile.png",
                "image/png", "Hello World".getBytes())};

        productCategory = productCategoryRepository.save(ProductCategory.builder().categoryName("잡화").build());
        product = productRepository.save(Product.builder()
                .name("제품명")
                .detail("제품 상세 설명")
                .category(productCategory)
                .userId(1L)
                .build());
    }

    @AfterEach
    void deleteEachData(){
        userRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("경매 시작 시간이 현재 시간보다 이전일 경우 WRONG_AUCTION_STARTAT 메시지 발생")
    void createAuctionInvalidStartTime() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(-1, 0);
        AuctionDto.Create request = new AuctionDto.Create("잘못된 경매", "내용", 30, "제품1",
                "제품 상세1", productCategory.getId(), 1000, times[0], times[1]);
        User user = userRepository.save(generateUser());

        //When
        CustomException exception = assertThrows(CustomException.class,
                () -> auctionService.createAuction(request, mockFiles, user.getEmail()));

        //Then
        assertEquals(ErrorCode.WRONG_AUCTION_STARTAT.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("경매 종료 시간이 경매 시간보다 1시간 이전일 경우 WRONG_AUCTION_ENDAT 에러 메시지 발생")
    void createAuctionInvalidEndTime() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(1, 1);
        AuctionDto.Create request = new AuctionDto.Create("잘못된 경매", "내용", 30, "제품1",
                "제품 상세1", productCategory.getId(), 1000, times[0], times[1]);
        User user = userRepository.save(generateUser());

        //When
        CustomException exception = assertThrows(CustomException.class,
                () -> auctionService.createAuction(request, mockFiles, user.getEmail()));

        //Then
        assertEquals(ErrorCode.WRONG_AUCTION_ENDAT.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("경매 등록 성공시 저장한 경매 ID와 경매 성공 여부 true 반환")
    void createAuction() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(1, 2);
        AuctionDto.Create request = new AuctionDto.Create("정상 경매", "내용", 30, "제품1",
                "제품 상세1", productCategory.getId(), 1000, times[0], times[1]);
        User user = userRepository.save(generateUser());

        //When
        AuctionDto.Result result = auctionService.createAuction(request, mockFiles, user.getEmail());

        //Then
        assertNotNull(result.getAuctionId());
        assertTrue(result.getIsSuccess());
    }

    @Test
    @DisplayName("존재하지 않는 경매 아이디 조회시 AUCTION_NOT_FOUND 에러 메시지 발생.")
    void getAuctionByNotExistId() {
        //When
        CustomException exception = assertThrows(CustomException.class,
                () -> auctionService.getAuctionById(-1L));

        //Then
        assertEquals(ErrorCode.AUCTION_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("경매 id 조회시 입력된 auction 정보 반환")
    void getAuctionById() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(1, 2);
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 1",times[0], times[1]));

        //When
        AuctionDto.Enter result = auctionService.getAuctionById(auction.getId());

        //Then
        Assertions.assertAll(
                () -> assertEquals(auction.getTitle(), result.getTitle(), "조회된 경매의 제목이 예상값과 다릅니다."),
                () -> assertEquals(auction.getIntroduce(), result.getIntroduce(), "조회된 경매의 설명이 예상값과 다릅니다."),
                () -> assertEquals(auction.getStartAt(), result.getStartAt(), "조회된 경매의 시작 시간이 예상값과 다릅니다."),
                () -> assertEquals(auction.getEndAt(), result.getEndAt(), "조회된 경매의 종료 시간이 예상값과 다릅니다."),
                () -> assertEquals(user.getEmail(), result.getEmail(), "조회된 경매의 종료 시간이 예상값과 다릅니다."),
                () -> assertEquals(auction.getMinimumPrice(), result.getMinimumPrice(), "조회된 경매의 최소 입찰금이 예상값과 다릅니다.")
        );
    }
    @Test
    @DisplayName("시작된 경매만 존재시 시작 이전 경매 조회 시 빈 리스트 반환")
    void getAllBeforeStartAuctionWhenStartedExists() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(-1, 2);
        User user = userRepository.save(generateUser());
        auctionRepository.save(generateAuction(user.getId(),"시작된 경매",times[0], times[1]));

        //When
        List<AuctionDto.BeforeStart> result = auctionService.getAllBeforeStartAuction(1,10);

        //Then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("종료된 경매만 존재시 시작 이전 경매 조회 시 빈 리스트 반환")
    void getAllBeforeStartAuctionWhenEndedExists() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(-2, -1);
        User user = userRepository.save(generateUser());
        auctionRepository.save(generateAuction(user.getId(),"시작된 경매",times[0], times[1]));

        //When
        List<AuctionDto.BeforeStart> result = auctionService.getAllBeforeStartAuction(1,10);

        //Then
        assertEquals(0, result.size());
    }
    @Test
    @DisplayName("시작전 경매 존재시 리스트에 해당 정보 반환")
    void getAllBeforeStartAuctionWhenBeforeStartedExists() {
        //Given
        LocalDateTime[] times = getStartAndEndTime(1, 2);
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 1",times[0], times[1]));

        //When
        AuctionDto.BeforeStart result = auctionService.getAllBeforeStartAuction(1,10).get(0);

        //Then
        assertEquals(auction.getId(), result.getId(), "조회된 경매의 ID가 예상값과 다릅니다.");
    }

    @Test
    @DisplayName("시작전 경매 2개 이상 존재 시 시작 시간 내림차순 순으로 리스트에 해당 정보 반환")
    void getAllBeforeStartAuctionWhenBeforeStartedExistsMany() {
        //Given
        List<Auction> auctions = new ArrayList<>();
        for(int i = 2 ; i > 0 ; i--){
            LocalDateTime[] times = getStartAndEndTime(1 + i, 2 + i);
            User user = userRepository.save(generateUser());
            auctions.add(auctionRepository.save(generateAuction(user.getId(),"경매 " + i, times[0], times[1])));
        }

        //When
        List<AuctionDto.BeforeStart> result = auctionService.getAllBeforeStartAuction(1,10);

        //Then
        assertEquals(auctions.size(), result.size(), "조회된 시작전 경매의 수가 일치하지 않습니다.");

        for(int i = 0 ; i < result.size() ; i++){
            assertEquals(auctions.get(auctions.size()-1-i).getId(), result.get(i).getId(), "조회된 경매의 ID가 예상값과 다릅니다.");
        }
    }


    @Test
    @DisplayName("내가 생성한 모든 경매 생성 시간 내림차순 순으로 리스트에 해당 정보 반환")
    void getAllMyAuctionsWhenExistsMany() {
        //Given
        List<Auction> auctions = new ArrayList<>();
        User user = userRepository.save(generateUser());

        for(int i = 3 ; i > 0 ; i--){
            LocalDateTime[] times = getStartAndEndTime(2 - i, 3 - i);
            auctions.add(auctionRepository.save(generateAuction(user.getId(),"경매 " + i, times[0], times[1])));
        }

        //When
        AuctionDto.GetPage result = auctionService.getAllMyAuctions(1,10, user.getEmail(),"all");

        //Then
        assertEquals(auctions.size(), result.getAuctions().size(), "조회된 시작전 경매의 수가 일치하지 않습니다.");

        for(int i = 0 ; i < result.getAuctions().size() ; i++){
            assertEquals(auctions.get(auctions.size()-1-i).getId(), result.getAuctions().get(i).getId(), "조회된 경매의 ID가 예상값과 다릅니다.");
        }
    }

    @Test
    @DisplayName("전체 경매 중 내가 생성한 시작전 경매 정보 반환")
    void getAllMyBeforeStartedAuctionsWhenMyBeforeStartedExists() {
        //Given
        List<Auction> myAuctions = new ArrayList<>();
        User user = userRepository.save(generateUser());
        User anotherUser = userRepository.save(generateUser());

        for(int i = 3 ; i > 0 ; i--){
            LocalDateTime[] times = getStartAndEndTime(2 - i, 3 - i);
            myAuctions.add(auctionRepository.save(generateAuction(user.getId(),"경매 " + i, times[0], times[1])));
        }

        LocalDateTime[] times = getStartAndEndTime(1 , 2);
        auctionRepository.save(generateAuction(anotherUser.getId(),"경매 ", times[0], times[1]));

        //When
        AuctionDto.GetPage result = auctionService.getAllMyAuctions(1,10, user.getEmail(),"before");

        //Then
        assertEquals(1, result.getAuctions().size(), "내가 생성한 시작전 경매는 1건 이어야 합니다.");
        assertEquals(myAuctions.get(myAuctions.size()-1).getId(), result.getAuctions().get(0).getId(), "조회된 경매 ID가 일치하지 않습니다.");
    }
    @Test
    @DisplayName("전체 경매 중 내가 생성한 진행 중 경매 정보 반환")
    void getAllMyBeforeStartedAuctionsWhenMyStartedExists() {
        //Given
        List<Auction> myAuctions = new ArrayList<>();
        User user = userRepository.save(generateUser());
        User anotherUser = userRepository.save(generateUser());

        for(int i = 3 ; i > 0 ; i--){
            LocalDateTime[] times = getStartAndEndTime(2 - i, 3 - i);
            myAuctions.add(auctionRepository.save(generateAuction(user.getId(),"경매 " + i, times[0], times[1])));
        }

        LocalDateTime[] times = getStartAndEndTime(-1 , 2);
        auctionRepository.save(generateAuction(anotherUser.getId(),"경매 ", times[0], times[1]));

        //When
        AuctionDto.GetPage result = auctionService.getAllMyAuctions(1,10, user.getEmail(),"open");

        //Then
        assertEquals(1, result.getAuctions().size(), "내가 생성한 시작전 경매는 1건 이어야 합니다.");
        assertEquals(myAuctions.get(1).getId(), result.getAuctions().get(0).getId(), "조회된 경매 ID가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("전체 경매 중 내가 생성한 진행 중 경매 정보 반환")
    void getAllMyBeforeStartedAuctionsWhenMyEndedExists() {
        //Given
        List<Auction> myAuctions = new ArrayList<>();
        User user = userRepository.save(generateUser());
        User anotherUser = userRepository.save(generateUser());

        for(int i = 3 ; i > 0 ; i--){
            LocalDateTime[] times = getStartAndEndTime(2 - i, 3 - i);
            myAuctions.add(auctionRepository.save(generateAuction(user.getId(),"경매 " + i, times[0], times[1])));
        }

        LocalDateTime[] times = getStartAndEndTime(-2 , -1);
        auctionRepository.save(generateAuction(anotherUser.getId(),"경매 ", times[0], times[1]));

        //When
        AuctionDto.GetPage result = auctionService.getAllMyAuctions(1,10, user.getEmail(),"close");

        //Then
        assertEquals(1, result.getAuctions().size(), "내가 생성한 시작전 경매는 1건 이어야 합니다.");
        assertEquals(myAuctions.get(0).getId(), result.getAuctions().get(0).getId(), "조회된 경매 ID가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("특정 경매의 경매 가능 최대 인원 수 반환")
    void getMaxParticipation() {
        //Given
        User user = userRepository.save(generateUser());

        LocalDateTime[] times = getStartAndEndTime(2 , 3);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        //When
        Integer result = auctionService.getMaxParticipation(auction.getId());

        //Then
        assertEquals(auction.getMaxParticipants(), result, "조회한 최대 인원수가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("특정 경매의 경매 최소 입찰금 반환")
    void getMinimumPrice() {
        //Given
        User user = userRepository.save(generateUser());
        LocalDateTime[] times = getStartAndEndTime(2 , 3);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        //When
        Integer result = auctionService.getMinimumPrice(auction.getId());

        //Then
        assertEquals(auction.getMinimumPrice(), result, "조회한 최소 입찰금이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("특정 경매의 경매 시작 시간 반환")
    void getStartAt() {
        //Given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        User user = userRepository.save(generateUser());

        LocalDateTime[] times = getStartAndEndTime(2 , 3);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        //When
        String result = auctionService.getStartAt(auction.getId()).substring(0, 23);

        //Then
        assertEquals(formatter.format(auction.getStartAt()), result, "조회한 최소 입찰금이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("진행 중인 경매에 예치금 입력 요청 시 WRONG_DEPOSIT_DATE 에러 메시지 발생")
    void placeDepositAtStartedAuction() {
        //Given
        User user = userRepository.save(generateUser());

        LocalDateTime[] times = getStartAndEndTime(-1 , 1);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        //When
        CustomException exception = assertThrows(CustomException.class,
                () -> auctionService.placeDeposit(new AuctionDto.Deposit(3500), auction.getId(), user.getEmail()));

        //Then
        assertEquals(ErrorCode.WRONG_DEPOSIT_DATE.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("끝난 경매에 예치금 입력 요청 시 WRONG_DEPOSIT_DATE 에러 메시지 발생")
    void placeDepositAtEndedAuction() {
        //Given
        User user = userRepository.save(generateUser());
        LocalDateTime[] times = getStartAndEndTime(-2 , -1);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        //When
        CustomException exception = assertThrows(CustomException.class,
                () -> auctionService.placeDeposit(new AuctionDto.Deposit(3500), auction.getId(), user.getEmail()));

        //Then
        assertEquals(ErrorCode.WRONG_DEPOSIT_DATE.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("경매의 최소 입찰금 보다 낮은 금액 입력 요청 시 WRONG_DEPOSIT_AMOUNT 에러 메시지 발생")
    void placeDepositInvalidAmount() {
        //Given
        User user = userRepository.save(generateUser());
        LocalDateTime[] times = getStartAndEndTime(1, 2);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        //When
        CustomException exception = assertThrows(CustomException.class,
                () -> auctionService.placeDeposit(new AuctionDto.Deposit(1000), auction.getId(), user.getEmail()));

        //Then
        assertEquals(ErrorCode.WRONG_DEPOSIT_AMOUNT.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예치금 정상 등록 시 ")
    void placeDeposit() {
        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        LocalDateTime[] times = getStartAndEndTime(1, 2);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));
        int amount = 4000;

        //When
        AuctionDto.Result result =  auctionService.placeDeposit(new AuctionDto.Deposit(amount), auction.getId(), user.getEmail());

        //Then
        assertEquals(auction.getId(), result.getAuctionId());
        assertTrue(result.getIsSuccess());
        auctionService.cancelDeposit(auction.getId(),user.getEmail());
    }

    @Test
    @DisplayName("이전에 등록한 예치금 이하의 금액 수정 요청 시 WRONG_DEPOSIT_UPDATE 에러 메시지 발생")
    void placeDepositInvalidUpdateAmount() {
        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);
        LocalDateTime[] times = getStartAndEndTime(1, 2);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),"경매 " , times[0], times[1]));

        final int AMOUNT = 4000;
        String email = user.getEmail();
        auctionService.placeDeposit(new AuctionDto.Deposit(AMOUNT), auction.getId(), email);

        //When
        CustomException exception = assertThrows(CustomException.class,
        () -> auctionService.placeDeposit(new AuctionDto.Deposit(AMOUNT), auction.getId(), email));

        //Then
        assertEquals(ErrorCode.WRONG_DEPOSIT_UPDATE.getMessage(), exception.getMessage());

        auctionService.cancelDeposit(auction.getId(), user.getEmail());
    }

    private LocalDateTime[] getStartAndEndTime(int startOffset, int endOffset) {
        LocalDateTime now = LocalDateTime.now();
        return new LocalDateTime[]{
                now.plusDays(startOffset),
                now.plusDays(endOffset)
        };
    }

    private Auction generateAuction(Long userId, String title, LocalDateTime startAt, LocalDateTime endAt){
        return generateAuction(userId, title,30, 3000, startAt, endAt);
    }

    private User generateUser() {
        return User.builder()
                .email(UUID.randomUUID()+"@test.com")
                .password("123")
                .phone("010-0000-0000")
                .postcode("12341")
                .birth(LocalDate.now())
                .address("address")
                .role(UserRoleEnum.USER)
                .build();
    }

    private Auction generateAuction(Long userId, String title, Integer maxParticipants, Integer minimumPrice, LocalDateTime startAt, LocalDateTime endAt){
        return Auction.builder()
                .userId(userId)
                .title(title)
                .introduce("내용")
                .maxParticipants(maxParticipants)
                .productId(product.getId())
                .minimumPrice(minimumPrice)
                .startAt(startAt)
                .endAt(endAt)
                .isEnded(false)
                .build();
    }
}