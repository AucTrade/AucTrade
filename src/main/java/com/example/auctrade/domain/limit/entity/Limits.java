package com.example.auctrade.domain.limit.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "limited_sale")
@EntityListeners(AuditingEntityListener.class)
public class Limits {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;	//한정 판매 아이디

	@Column(name = "title", nullable = false)
	private String title;	//한정 판매글 제목

	@Column(name = "introduce")
	private String introduce;	//한정 판매 상세 설명

	@Column(name = "price", nullable = false)
	private Long price;	//상품 가격

	@Setter
	@Column(name = "amount", nullable = false)
	private Integer amount;	//상품 수량

	@Column(name = "sale_date", nullable = false)
	private LocalDateTime saleDate;	//판매 일자

	@CreatedDate
	@Column(name = "created", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime created;	//판매 게시글 생성 일자

	@Column(name = "personal_limit")
	private Integer personalLimit;	//인당 구매 제한

	@Column(name = "status", nullable = false)
	private Integer status;	//판매 상태 : 판매 예정 0, 판매 중 1, 판매완료 2

	@Column(name = "productId", nullable = false)
	private Long productId;

	@Column(name = "sellerId", nullable = false)
	private Long sellerId;

	public void start(){this.status=1;}
	public void end(){this.status=2;}

	public void decrease(int quantity){
		this.amount = Math.max(this.amount-quantity,0);
	}

}
