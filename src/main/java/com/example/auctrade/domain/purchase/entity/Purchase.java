package com.example.auctrade.domain.purchase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase")
public class Purchase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "limit_id", nullable = false)
	private Long limitId; // Limit 게시글 ID

	@Column(name = "buyer_id", nullable = false)
	private Long buyerId; // 구매자 ID

	@Column(name = "seller_id", nullable = false)
	private Long sellerId; // 판매자 ID

	@Column(name = "quantity", nullable = false)
	private Integer quantity; // 구매 수량

	@Column(name = "total_price", nullable = false)
	private Long totalPrice; // 총 가격

	@Column(name = "is_auction", nullable = false)
	private Boolean isAuction; // 경매 여부

	@Column(name = "created_date", nullable = false)
	private LocalDateTime createdDate;

	@PrePersist
	public void prePersist() {
		this.createdDate = LocalDateTime.now();
	}
}
