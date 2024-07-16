package com.example.auctrade.domain.auction.entity;

import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auctions")
@EntityListeners(AuditingEntityListener.class)
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 경매장 id

    @Column(name = "title", nullable = false)
    private String title; // 경매 제목

    @Column(name = "introduce")
    private String introduce; // 경매 설명

    @Column(name = "personnel", nullable = false)
    private int personnel; // 경매 참여인(최대)

//    @Column(name = "products", nullable = false)
//    private String products; // Product 엔티티 생성시 수정 필요

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Product> products = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "auction_products", joinColumns = @JoinColumn(name = "auction_id"))
    @Column(name = "product_id")
    private List<Long> productIds; // Product ID 리스트

    /**
     * postgreSQL에서 배열 컬럼을 JPA에서 직접 다루는 건 방법이 드물 뿐더러 권장되지 않는다고 함
     * 일대다 연관관계를 매핑했을 때의 문제점?
     * - 만약 외부 요인으로 인해 경매가 강제 중단되어 경매 엔티티 인스턴스에 문제가 생겼을 경우
     * - 그렇다고 해도, 상품 엔티티는 온전해야 함
     * - 왜냐하면, 엔티티 생명주기상 상품 등록이 우선이고 경매 게시가 후순이기 때문
     * - 즉, 경매 엔티티를 생성할 때에 이미 상품은 존재해야 하고 경매가 중단되어도 상품이 DB 에서 사라지면 안됨
     * - @ElementCollection, @CollectionTable 활용 필요? -> 개념 더 공부 필요
     * - Auction 엔티티가 삭제되거나 변경되더라도 Product 엔티티는 영향을 받지 않게 하도록
     *
     * ALTER TABLE auction_products
     * ADD CONSTRAINT unique_product_id UNIQUE (product_id);
     * - auction_products 테이블에서 product_id에 고유 제약 조건을 추가하여
     * - 각 Product가 하나의 Auction에만 속할 수 있도록
     */

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User 객체 (경매 생성자)

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // 경매 시작예고시간, 시간 포맷팅 고려 필요

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 경매 채팅방 생성일자

    @Column(name = "minimum_price", nullable = false)
    private int minimumPrice; // 최소입찰금액

    @Column(name = "price", nullable = false)
    private int price; // 계속 업데이트하다가 경매 종료됐을 때의 price 를 최종낙찰가로

    @Column(name = "ended", nullable = false)
    private boolean ended; // price 가 진행 가격인지 최종 낙찰 가격인지

    @Column(name = "finish_date", nullable = false)
    private LocalDateTime finishDate; // 경매 종료예고시간, 시간 포맷팅 고려 필요
}
