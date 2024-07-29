package com.example.auctrade.domain.user.mapper;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;

public class UserMapper {

    // 엔티티를 DTO로 변환하는 static 메서드
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getEmail(),
                user.getPassword(),
                user.getPhone(),
                user.getAddress(),
                user.getBirth(),
                user.getRole(),
                user.getPostcode()
        );
    }

    // DTO를 엔티티로 변환하는 static 메서드
    public static User toEntity(UserDTO userDTO, String password) {
        if (userDTO == null) {
            return null;
        }

        return new User(
                userDTO.getEmail(),
                password,
                userDTO.getPhone(),
                userDTO.getAddress(),
                userDTO.getBirth(),
                userDTO.getRole(),
                userDTO.getPostcode()
        );
    }
}
