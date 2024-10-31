package com.example.auctrade.domain.user.repository;

import com.example.auctrade.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.point FROM User u WHERE u.email = :email")
    Integer findPointByEmail(@Param("email") String email);
}
