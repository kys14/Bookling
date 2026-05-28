package com.bookling.bookling.repository;

import com.bookling.bookling.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    // 임시 도서 ID로 UserHistory 탐색
    List<UserHistory> findByTemporaryBookId(Long temporaryBookId);
}