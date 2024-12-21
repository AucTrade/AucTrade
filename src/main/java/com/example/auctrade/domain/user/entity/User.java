package com.example.auctrade.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "role")
    private UserRoleEnum role;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "point")
    private int point;
    @Builder
    public User(String email, String password, String phone, String address, LocalDate birth, UserRoleEnum role, String postcode) {
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.birth = birth;
        this.role = role;
        this.postcode = postcode;
        this.point = 0;
    }

    public void addPoint(int point){
        this.point += point;
    }

    public void subPoint(int point){
        this.point -= point;
    }
}
