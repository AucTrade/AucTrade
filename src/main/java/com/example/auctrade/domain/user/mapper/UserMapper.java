package com.example.auctrade.domain.user.mapper;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;

public class UserMapper {
    private  UserMapper(){}
    public static User CreateDTOToEntity(UserDTO.Create dto, String password) {
        return (dto == null) ? null : new User(
                dto.getEmail(),
                password,
                dto.getPhone(),
                dto.getAddress(),
                dto.getBirth(),
                dto.getRole(),
                dto.getPostcode()
        );
    }

    public static UserDTO.Login EntityToLoginDTO(User user) {
        return (user == null) ? null : new UserDTO.Login(user.getEmail(), user.getPassword(), user.getRole());
    }

    public static UserDTO.Info EntityToInfoDTO(User user){
        return (user == null) ? null : new UserDTO.Info(user.getEmail(), user.getRole());
    }

    public static UserDTO.Result CreateResultDTO(boolean isSuccess){
        return new UserDTO.Result(isSuccess);
    }
}
