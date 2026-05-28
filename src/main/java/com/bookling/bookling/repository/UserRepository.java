package com.bookling.bookling.repository;

import com.bookling.bookling.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 유저 찾기용 메서드
    Optional<User> findByEmail(String email);
}