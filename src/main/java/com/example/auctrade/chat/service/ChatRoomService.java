package com.example.auctrade.chat.service;

import com.example.auctrade.chat.dto.ChatRoomDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private static final String CHAT_ROOM_KEY = "CHAT_ROOM";
    private final RedisTemplate<String, String> redisTemplate;

    public List<ChatRoomDTO> findAllRooms(){
            return  redisTemplate.opsForList().range(CHAT_ROOM_KEY, 0 ,10).stream().map(ChatRoomDTO::new).toList();
    }

    public ChatRoomDTO findRoomById(String id){
//        String title = redisTemplate.opsForList().leftPop(CHAT_ROOM_KEY);
        return ChatRoomDTO.builder()
                .id(id)
                .title(id)
                .count(1)
                .build();
    }

    public ChatRoomDTO create(String name){
        String id = UUID.randomUUID().toString();
        redisTemplate.opsForList().leftPush(CHAT_ROOM_KEY, name);
        return ChatRoomDTO.builder()
                .id(id)
                .title(name)
                .count(1)
                .build();
    }
}
