package com.bookling.bookling.service;

import com.bookling.bookling.dto.HistoryContextResponseDto;
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.entity.UserHistory;
import com.bookling.bookling.repository.UserHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserHistoryService {

    private final UserHistoryRepository userHistoryRepository;

    // 특정 도서 ID 연동 과거 활동 기록(일기 문맥) 조회
    public List<HistoryContextResponseDto> getContextByBookId(Long bookId) {
        List<UserHistory> histories = userHistoryRepository.findByBookId(bookId);

        if (histories.isEmpty()) {
            throw new IllegalArgumentException("해당 도서의 추천 이력이 존재하지 않습니다. ID: " + bookId);
        }

        return histories.stream()
                .map(history -> {
                    Diary diary = history.getDiary();
                    return HistoryContextResponseDto.builder()
                            .bookId(history.getBookId())
                            .emotion(history.getEmotion())
                            .diaryId(diary != null ? diary.getId() : null)
                            .diaryTitle(diary != null ? diary.getTitle() : "일기 없이 감정만 선택한 하루입니다.")
                            .diaryContent(diary != null ? diary.getContent() : "")
                            .createdAt(history.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // [테스트 및 관리용] 전체 추천 이력 목록 조회
    public List<HistoryContextResponseDto> getAllHistories() {
        return userHistoryRepository.findAll().stream()
                .map(history -> HistoryContextResponseDto.builder()
                        .bookId(history.getBookId())
                        .emotion(history.getEmotion())
                        .diaryId(history.getDiary() != null ? history.getDiary().getId() : null)
                        .diaryTitle(history.getDiary() != null ? history.getDiary().getTitle() : "일기 없음")
                        .diaryContent(history.getDiary() != null ? history.getDiary().getContent() : "")
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}