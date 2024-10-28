package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import org.junit.jupiter.api.*;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

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
    RedissonClient redissonClient;

    List<Auction> auctions = new ArrayList<>();
    List<String> buyerList = new ArrayList<>();
    List<DepositDTO.Result> createResults = new ArrayList<>();

    @BeforeAll
    void createData(){

        for(int i = 0 ; i < 10; i++){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            LocalDateTime endTime;

            if (i < 4) {
                // 0, 1, 2, 3: 시작하지 않은 경매
                startTime = now.plusDays(2);
                endTime = startTime.plusDays(1);
            } else if (i < 9) {
                // 4, 5, 6, 7, 8: 진행 중인 경매
                startTime = now.minusDays(1);
                endTime = now.plusDays(1);
            } else {
                // 9: 이미 끝난 경매
                startTime = now.minusDays(2);
                endTime = now.minusDays(1);
            }

            auctions.add(auctionRepository.save(Auction.builder()
                    .title("제목" + i)
                    .description("내용" + i)
                    .startTime(startTime)
                    .endTime(endTime)
                    .maxParticipants(3)
                    .minimumPrice(1000 + i)
                    .productId(i)
                    .sellerEmail("test" + (i % 3) + "@test.com")
                    .build()));
        }
        for(int i = 0 ; i < 20; i++){
            buyerList.add("buyer"+i+"@test.com");
        }

        Auction targetAuction;
        //정상 등록
        for(int i = 0 ; i < 10 ; i++){
            targetAuction = auctions.get(i%5);
            createResults.add(depositService.registerDeposit(DepositDTO.Create.builder()
                            .auctionId(targetAuction.getId())
                            .deposit(3000)
                            .minPrice(targetAuction.getMinimumPrice())
                            .maxParticipation(targetAuction.getMaxParticipants())
                            .startTime(targetAuction.getStartTime().toString())
                            .email(buyerList.get(i))
                            .build()));
        }

        //최저 입찰가 보다 낮은 등록
        targetAuction = auctions.get(0);
        createResults.add(depositService.registerDeposit(DepositDTO.Create.builder()
                        .auctionId(targetAuction.getId())
                        .deposit(10)
                        .minPrice(targetAuction.getMinimumPrice())
                        .maxParticipation(targetAuction.getMaxParticipants())
                        .startTime(targetAuction.getStartTime().toString())
                        .email(buyerList.get(1))
                        .build()));


        //추가 등록 낮은 가격
        for(int i = 2 ; i < 7 ; i++){
            targetAuction = auctions.get(i-2);
            createResults.add(depositService.registerDeposit(DepositDTO.Create.builder()
                            .auctionId(targetAuction.getId())
                            .deposit(2000)
                            .minPrice(targetAuction.getMinimumPrice())
                            .maxParticipation(targetAuction.getMaxParticipants())
                            .startTime(targetAuction.getStartTime().toString())
                            .email(buyerList.get(i))
                            .build()));
        }

        //최대 인원수 등록 후 최저의 같은 가격 입찰
        targetAuction = auctions.get(0);
        createResults.add(depositService.registerDeposit(DepositDTO.Create.builder()
                        .auctionId(targetAuction.getId())
                        .deposit(2000)
                        .minPrice(targetAuction.getMinimumPrice())
                        .maxParticipation(targetAuction.getMaxParticipants())
                        .startTime(targetAuction.getStartTime().toString())
                        .email(buyerList.get(9))
                        .build()));
    }

    @AfterAll
    void deleteData(){
        for(Auction auction : auctions){
            RBucket<Object> bucket = redissonClient.getBucket(REDIS_DEPOSIT_KEY + auction.getId());
            if (bucket.isExists()) bucket.delete();
        }
        for(String email : buyerList){
            depositService.removeMyDepositLog(email);
        }
    }

    @Test
    @DisplayName("경매 입찰 성공 여부 확인")
    void registerDeposit() {
        //0 ~ 9 3000 정상 예치금 등록
        Assertions.assertTrue(createResults.get(0).getSuccess());
        //10 30 최소 낙찰금 보다 낮은 예치금 등록
        Assertions.assertFalse(createResults.get(10).getSuccess());
        //11 ~ 15 2000 최소이상 이전보다 낮은 금액 예치금 등록
        Assertions.assertTrue(createResults.get(11).getSuccess());
        //16 최대 인원수 등록된 경매에서 최저 예치금 보다 낮은 금액 입찰
        Assertions.assertFalse(createResults.get(16).getSuccess());
    }

    @Test
    @DisplayName("경매 최소가 가져오기")
    void getDeposit() {
        Integer result = depositService.getMinDeposit(auctions.get(0).getId(), auctions.get(0).getMaxParticipants());
        Assertions.assertEquals(2000, result);
    }

    @Test
    @DisplayName("특정 회원이 예치금을 등록한 옥션 리스트 가져오기")
    void getMyAuctions() {
        List<Long> result = depositService.getMyAuctions(toPageable(1,10, "startAt"), buyerList.get(0));
        Assertions.assertEquals(1L, result.get(0));
    }

    @Test
    @DisplayName("특정 경매의 예치금 인원 수 확인")
    void getNowParticipation() {
        Integer result = depositService.getNowParticipation(auctions.get(0).getId());
        Assertions.assertEquals(3, result);
    }

    @Test
    @DisplayName("특정 회원의 예치금 경매 수 확인")
    void getMyDepositSize() {
        Long result = depositService.getMyDepositSize(buyerList.get(6));
        Assertions.assertEquals(2L, result);
    }

    @Test
    @DisplayName("예치금 등록 취소")
    void cancelDeposit() {
        //있는 사용자 취소
        Assertions.assertTrue(depositService.cancelDeposit(auctions.get(4).getId(), buyerList.get(4)).getSuccess());

        //없는 사용자 취소
        Assertions.assertFalse(depositService.cancelDeposit(auctions.get(4).getId(), buyerList.get(0)).getSuccess());
    }

    @Test
    @DisplayName("예치금 동시 입찰 ")
    void multiDepositRequest() throws InterruptedException{
        Auction targetAuction = auctions.get(6);
        int maxParticipants = 3;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);  // 동시 요청 개수 설정
        List<Boolean> results = new ArrayList<>();  // 성공 여부 확인 리스트

        for (int i = 10; i < 20; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    boolean success = depositService.registerDeposit(DepositDTO.Create.builder()
                            .auctionId(targetAuction.getId())
                            .deposit(2000)
                            .minPrice(targetAuction.getMinimumPrice())
                            .maxParticipation(maxParticipants)
                            .startTime(targetAuction.getStartTime().toString())
                            .email(buyerList.get(finalI))
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
        Assertions.assertEquals(maxParticipants, depositService.getNowParticipation(targetAuction.getId()), "성공한 예치금 등록 요청 수는 최대 인원수와 같아야 합니다.");
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}