package com.example.auctrade.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "user")
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
}
