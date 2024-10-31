package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class DepositServiceImplTest {
    @Autowired
    DepositService depositService;

    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RedissonClient redissonClient;

    List<Auction> auctions = new ArrayList<>();

    List<User> sellerList = new ArrayList<>();
    List<User> buyerList = new ArrayList<>();
    static int MAX_PARTICIPATION_NUM = 3;

    @BeforeAll
    void createData(){

        for(int i = 0 ; i < 7; i++){
            sellerList.add(userRepository.save(User.builder()
                    .email("seller"+i+"@test.com")
                    .birth(LocalDate.now())
                    .phone("010-0000-0000")
                    .address("address"+i)
                    .role(UserRoleEnum.USER)
                    .postcode("12341")
                    .password("123")
                    .build()));
        }

        for(int i = 0 ; i < 30; i++){
            buyerList.add(userRepository.save(User.builder()
                    .email("buyer"+i+"@test.com")
                    .birth(LocalDate.now())
                    .phone("010-0000-0000")
                    .address("address"+i)
                    .role(UserRoleEnum.USER)
                    .postcode("12341")
                    .password("123")
                    .build()));
        }
        for(int i = 1 ; i < buyerList.size() ; i++){
            buyerList.get(i).addPoint(500000);
            userRepository.save(buyerList.get(i));
        }

        for(int i = 0 ; i < 13; i++){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            LocalDateTime endTime;

            if (i < 11) {
                // 0 ~ 10: 시작하지 않은 경매
                startTime = now.plusDays(2);
                endTime = startTime.plusDays(1);
            } else if (i < 12) {
                // 11: 진행 중인 경매
                startTime = now.minusDays(1);
                endTime = now.plusDays(1);
            } else {
                // 12: 이미 끝난 경매
                startTime = now.minusDays(2);
                endTime = now.minusDays(1);
            }

            auctions.add(auctionRepository.save(Auction.builder()
                    .title("제목" + i)
                    .description("내용" + i)
                    .startTime(startTime)
                    .endTime(endTime)
                    .maxParticipants(MAX_PARTICIPATION_NUM)
                    .minimumPrice(1000 + i)
                    .productId(i)
                    .sellerEmail(sellerList.get(i%4).getEmail())
                    .build()));
        }
    }

    @AfterAll
    void deleteData(){
        for(Auction auction : auctions){
            RBucket<Object> bucket = redissonClient.getBucket(REDIS_DEPOSIT_KEY + auction.getId());
            if (bucket.isExists()) bucket.delete();
        }
        for(User user : buyerList){
            depositService.removeMyDepositLog(user.getEmail());
        }
    }

    @Test
    @DisplayName("경매 예치금 등록시 유저 포인트가 부족한 경우 TEST")
    void registerUnderUserPointDeposit() {
        Auction target = auctions.get(0);
        DepositDTO.Result result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(2000)
                .email(buyerList.get(0).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(1000, userRepository.findPointByEmail(buyerList.get(0).getEmail()));
    }

    @Test
    @DisplayName("경매 예치금 정상 등록 TEST")
    void successRegisterDeposit() {
        Auction target = auctions.get(0);
        int beforePoint = userRepository.findPointByEmail(buyerList.get(1).getEmail());

        DepositDTO.Result result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(3000)
                .email(buyerList.get(1).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals((beforePoint - 3000), userRepository.findPointByEmail(buyerList.get(1).getEmail()));
    }

    @Test
    @DisplayName("경매 예치금 최저 입찰가 보다 낮은 금액 등록 TEST")
    void registerUnderMinDeposit() {
        Auction target = auctions.get(1);
        int beforePoint = userRepository.findPointByEmail(buyerList.get(2).getEmail());

        DepositDTO.Result result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(auctions.get(1).getId())
                .deposit(10)
                .email(buyerList.get(2).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(beforePoint, userRepository.findPointByEmail(buyerList.get(2).getEmail()));
    }

    @Test
    @DisplayName("이미 진행중인 경매 예치금 등록 TEST")
    void registerDepositAlreadyStart() {
        Auction target = auctions.get(11);
        int beforePoint = userRepository.findPointByEmail(buyerList.get(3).getEmail());

        DepositDTO.Result result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(3000)
                .email(buyerList.get(3).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(beforePoint, userRepository.findPointByEmail(buyerList.get(3).getEmail()));
    }

    @Test
    @DisplayName("이미 끝난 경매 예치금 등록 TEST")
    void registerDepositAlreadyEnd() {
        Auction target = auctions.get(12);
        int beforePoint = userRepository.findPointByEmail(buyerList.get(4).getEmail());

        DepositDTO.Result result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(3000)
                .email(buyerList.get(4).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(beforePoint, userRepository.findPointByEmail(buyerList.get(4).getEmail()));
    }

    @Test
    @DisplayName("최대 참여 인원수를 채운 경매 예치금 등록 TEST")
    void registerMaxParticipationDeposit() {
        Auction target = auctions.get(2);
        int beforePoint = userRepository.findPointByEmail(buyerList.get(8).getEmail());

        for(int i = 0 ; i < MAX_PARTICIPATION_NUM ; i++) {

            depositService.registerDeposit(DepositDTO.Create.builder()
                    .auctionId(target.getId())
                    .deposit(3000)
                    .email(buyerList.get(5+i).getEmail())
                    .maxParticipation(target.getMaxParticipants())
                    .startAt(target.getStartTime().toString())
                    .minimumPrice(target.getMinimumPrice())
                    .build());
        }
        // 최저 예치금 보다 낮은 금액 등록 시도
        DepositDTO.Result result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(2000)
                .email(buyerList.get(8).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(beforePoint, userRepository.findPointByEmail(buyerList.get(8).getEmail()));

        // 같은 금액 등록 시도
        result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(3000)
                .email(buyerList.get(8).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(beforePoint, userRepository.findPointByEmail(buyerList.get(8).getEmail()));

        // 높은 금액 등록 시도
        result = depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(4000)
                .email(buyerList.get(8).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals((beforePoint - 4000), userRepository.findPointByEmail(buyerList.get(8).getEmail()));
    }

    @Test
    @DisplayName("경매 최소가 가져오기 TEST")
    void getDeposit() {
        Auction target = auctions.get(3);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(5000)
                .email(buyerList.get(9).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(4000)
                .email(buyerList.get(10).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Integer result = depositService.getMinDeposit(target.getId());
        Assertions.assertEquals(4000, result);
    }

    @Test
    @DisplayName("특정 회원이 예치금 등록한 옥션 리스트 가져오기 TEST")
    void getMyAuctions() {
        Auction target = auctions.get(4);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(5000)
                .email(buyerList.get(11).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        List<Long> result = depositService.getMyAuctions(toPageable(1,10, "startAt"), buyerList.get(11).getEmail());
        Assertions.assertEquals(target.getId(), result.get(0));
    }

    @Test
    @DisplayName("특정 경매의 예치금 인원 수 가져오기 TEST")
    void getNowParticipation() {
        Auction target = auctions.get(5);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(4000)
                .email(buyerList.get(12).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(3000)
                .email(buyerList.get(13).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Integer result = depositService.getNowParticipation(target.getId());
        Assertions.assertEquals(2, result);
    }

    @Test
    @DisplayName("특정 회원의 예치금 경매 수 확인")
    void getMyDepositSize() {
        Auction target = auctions.get(6);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(auctions.get(6).getId())
                .deposit(5000)
                .email(buyerList.get(14).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        target = auctions.get(7);
        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(5000)
                .email(buyerList.get(14).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Long result = depositService.getMyDepositSize(buyerList.get(14).getEmail());
        Assertions.assertEquals(2L, result);
    }

    @Test
    @DisplayName("예치금 등록 취소")
    void cancelDeposit() {
        Auction target = auctions.get(8);
        int beforePoint = userRepository.findPointByEmail(buyerList.get(15).getEmail());

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(5000)
                .email(buyerList.get(15).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertFalse(depositService.cancelDeposit(target.getId(), buyerList.get(0).getEmail()).getSuccess());
        Assertions.assertEquals((beforePoint-5000), userRepository.findPointByEmail(buyerList.get(15).getEmail()));
        Assertions.assertTrue(depositService.cancelDeposit(target.getId(), buyerList.get(15).getEmail()).getSuccess());
        Assertions.assertEquals((beforePoint), userRepository.findPointByEmail(buyerList.get(15).getEmail()));
        Assertions.assertFalse(depositService.cancelDeposit(target.getId(), buyerList.get(15).getEmail()).getSuccess());
    }

    @Test
    @DisplayName("예치금 금액 확인 TEST")
    void getMyDepositByAuctionId() {
        Auction target = auctions.get(9);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(target.getId())
                .deposit(5000)
                .email(buyerList.get(16).getEmail())
                .maxParticipation(target.getMaxParticipants())
                .startAt(target.getStartTime().toString())
                .minimumPrice(target.getMinimumPrice())
                .build());

        Assertions.assertEquals(5000, depositService.getMyDepositByAuctionId(target.getId(),buyerList.get(16).getEmail()));
        Assertions.assertEquals(-1, depositService.getMyDepositByAuctionId(target.getId(),buyerList.get(17).getEmail()));

    }

    @Test
    @DisplayName("예치금 동시 입찰 ")
    void multiDepositRequest() throws InterruptedException{
        Auction target = auctions.get(10);
        int maxParticipants = 3;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);  // 동시 요청 개수 설정
        List<Boolean> results = new ArrayList<>();  // 성공 여부 확인 리스트

        for (int i = 20; i < 30; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    boolean success = depositService.registerDeposit(DepositDTO.Create.builder()
                            .auctionId(target.getId())
                            .deposit(2000)
                            .email(buyerList.get(finalI).getEmail())
                            .maxParticipation(target.getMaxParticipants())
                            .startAt(target.getStartTime().toString())
                            .minimumPrice(target.getMinimumPrice())
                            .build()).getSuccess();
                    synchronized (results) {
                        results.add(success);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        // 모든 스레드가 끝날 때까지 대기
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        long successfulDeposits = results.stream().filter(Boolean::booleanValue).count();
        Assertions.assertEquals(maxParticipants, successfulDeposits, "성공한 예치금 등록 요청 수는 최대 인원수와 같아야 합니다.");
        Assertions.assertEquals(maxParticipants, depositService.getNowParticipation(target.getId()), "성공한 예치금 등록 요청 수는 최대 인원수와 같아야 합니다.");
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}