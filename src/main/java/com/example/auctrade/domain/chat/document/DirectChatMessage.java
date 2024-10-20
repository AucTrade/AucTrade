package com.example.auctrade.domain.chat.document;

import org.springframework.data.mongodb.core.mapping.Document;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Document(collection = "DirectChatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatMessage extends ChatMessage{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;	//도큐먼트 아이디
	private String directChatId;	//채팅룸 아이디

	public DirectChatMessage(String username, String message, Long directChatId){
		super(username, message);
		this.directChatId = directChatId.toString();
	}
}
