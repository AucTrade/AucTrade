package com.example.auctrade.domain.user.controller;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.UserDetailsImpl;
import com.example.auctrade.domain.user.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO userDTO) {
        UserDTO user = userServiceImpl.createUser(userDTO);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // 인증 테스트용 메소드
    @GetMapping("/test")
    public String test() {
        return "success";
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<UserDTO> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDTO user = userServiceImpl.logoutUser(userDetails.getUser());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
