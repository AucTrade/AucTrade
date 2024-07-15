package com.example.auctrade.chat.controller;

import com.example.auctrade.chat.dto.ChatRoomDTO;
import com.example.auctrade.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/chat")
@Log4j2
public class RoomController {

    private final ChatRoomService chatRoomService;

    //채팅방 목록 조회
    @GetMapping(value = "/roomList")
    public ResponseEntity<List<ChatRoomDTO>> roomList(){
        return ResponseEntity.ok(chatRoomService.findAllRooms());
    }

    @PostMapping("/room")
    public  ResponseEntity<List<ChatRoomDTO>> create(@RequestBody String name){
        chatRoomService.create(name);
        return ResponseEntity.ok(chatRoomService.findAllRooms());
    }

    @GetMapping("/room")
    public ResponseEntity<ChatRoomDTO> getRoom(@RequestParam("roomId") String roomId){
        return ResponseEntity.ok(chatRoomService.findRoomById(roomId));
    }
}