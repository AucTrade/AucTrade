package com.example.auctrade.domain.limit.entity;

import java.time.LocalDateTime;

import com.example.auctrade.domain.user.entity.User;

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
@Table(name = "limit_payment")
public class Purchase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id; // 결제 ID

	@Column(name = "price", nullable = false)
	private Long price; // 거래가

	@Column(name="quantity", nullable = false)
	private int quantity; //거래 수량

	@Column(name = "date")
	private LocalDateTime date; // 결제일

	@Column(name = "is_finished", nullable = false)
	private Boolean isFinished; // 거래완료 여부

	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer; // 구매 회원

	@ManyToOne
	@JoinColumn(name = "limited_id", nullable = false)
	private Limits limit; // 한정 판매 매물

	// 결제가 완료될 때 date를 설정하는 메서드
	public void completePayment() {
		this.isFinished = true;
		this.date = LocalDateTime.now(); // 결제 완료 시점으로 설정
	}
}
