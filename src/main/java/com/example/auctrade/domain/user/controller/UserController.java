package com.example.auctrade.domain.user.controller;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO userDTO) {
        UserDTO createdUser = userServiceImpl.createUser(userDTO);
        return ResponseEntity.ok(createdUser);
    }

    // 인증 테스트용 메소드
    @GetMapping("/test")
    public String test() {
        return "success";
    }


}
