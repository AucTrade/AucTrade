package com.example.auctrade.domain.chat.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "direct_chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DirectChatMessage{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	//도큐먼트 아이디
	private String directChatId;	//채팅룸 아이디

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "message", nullable = false)
	private String message;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	public DirectChatMessage(String email, String message, Long directChatId){
		this.email = email;
		this.message = message;
		this.directChatId = directChatId.toString();
	}
}
