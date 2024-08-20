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
    public ResponseEntity<UserDTO.Result> signup(@RequestBody UserDTO.Create userDTO) {
        return new ResponseEntity<>(userServiceImpl.createUser(userDTO), HttpStatus.OK);
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<UserDTO.Result> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return new ResponseEntity<>(userServiceImpl.logoutUser(userDetails.getUser().getEmail()), HttpStatus.OK);
    }
}
