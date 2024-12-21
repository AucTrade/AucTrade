package com.example.auctrade.domain.user.controller;

import com.example.auctrade.domain.point.dto.PointDto;
import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.entity.UserDetailsImpl;
import com.example.auctrade.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto.Info> signup(@RequestBody UserDto.Create userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    @GetMapping
    public ResponseEntity<UserDto.Info> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getUserInfo(userDetails.getUsername()));
    }

    @GetMapping("/my/point")
    public ResponseEntity<UserDto.Point> getMyPoint(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getPoint(userDetails.getUsername()));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<UserDto.Result> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.logoutUser(userDetails.getUser().getEmail()));
    }
}
