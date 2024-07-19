package com.example.auctrade.domain.user.dto;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private String email;
    private String password;
    private String phone;
    private String address;
    private LocalDate birth;
    private UserRoleEnum role;
    private String postcode;

    // 이 생성자는 point 필드를 1000으로 초기화합니다.
    public UserDTO(String email, String password, String phone, String address, LocalDate birth, UserRoleEnum role, String postcode) {
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.birth = birth;
        this.role = role;
        this.postcode = postcode;
    }
}
