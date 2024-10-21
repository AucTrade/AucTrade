package com.example.auctrade.domain.trade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trade")
public class Trade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id; // 거래 ID

	@Column(name = "price", nullable = false)
	private Long price; // 거래가

	@Column(name="quantity", nullable = false)
	private int quantity; // 거래 수량

	@Column(name = "trade_date")
	private LocalDateTime tradeDate; // 거래일

	@Column(name = "is_finished")
	private Boolean isFinished; // 거래 완료 여부

	@Column(name = "buyer", nullable = false)
	private Long buyer; // 구매 회원

	@Column(name = "post_id", nullable = false)
	private Long postId; // 게시글 ID (Auction 또는 Limit의 ID)

	@Column(name = "post_type", nullable = false)
	private Boolean isAuction; // 게시글 타입 (Auction 또는 Limit 구분)

	// 결제가 완료될 때 date를 설정하는 메서드
	public void completePayment() {
		this.isFinished = true;
		this.tradeDate = LocalDateTime.now(); // 결제 완료 시점으로 설정
	}
}
