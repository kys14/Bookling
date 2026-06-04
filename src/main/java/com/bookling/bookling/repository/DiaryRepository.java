package com.bookling.bookling.repository;

import com.bookling.bookling.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    // 특정 유저의 ID로 작성된 일기 목록만 조회
    List<Diary> findByUserId(Long userId);

    // 특정 단어 검색
    List<Diary> findByTitleContaining(String keyword);

    // 특정 기간 검색 (시작 날짜 ~ 종료 날짜)
    List<Diary> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 제목 키워드와 특정 기간을 동시 검색
    List<Diary> findByTitleContainingAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end);
}