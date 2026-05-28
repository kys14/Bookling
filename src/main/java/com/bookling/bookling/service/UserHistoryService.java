package com.bookling.bookling.service;

import com.bookling.bookling.dto.HistoryContextResponseDto;
import com.bookling.bookling.dto.UserHistoryRequestDto;
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.entity.UserHistory;
import com.bookling.bookling.repository.DiaryRepository; // 패키지 경로 깔끔하게 임포트로 처리했어!
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
    private final DiaryRepository diaryRepository; // 이쪽으로 깔끔하게 모아뒀어!

    // ★ 수정된 메서드: 이제 단건이 아니라 리스트(List)를 반환해서 중복 도서 문제를 해결함!
    public List<HistoryContextResponseDto> getContextByBookId(Long temporaryBookId) {

        // 1. 임시 도서 ID로 해당하는 모든 기록을 리스트로 조회
        List<UserHistory> histories = userHistoryRepository.findByTemporaryBookId(temporaryBookId);

        // 2. 만약 해당 책으로 추천받은 이력이 아예 없으면 예외 처리
        if (histories.isEmpty()) {
            throw new IllegalArgumentException("해당 도서의 추천 이력이 존재하지 않습니다. ID: " + temporaryBookId);
        }

        // 3. 루프(Stream)를 돌면서 찾아온 여러 개의 기록을 전부 DTO 상자에 담아서 리스트로 반환
        return histories.stream()
                .map(history -> {
                    Diary diary = history.getDiary();
                    return HistoryContextResponseDto.builder()
                            .temporaryBookId(history.getTemporaryBookId())
                            .emotion(history.getEmotion())
                            .diaryId(diary != null ? diary.getId() : null)
                            .diaryTitle(diary != null ? diary.getTitle() : "일기 없이 감정만 선택한 하루입니다.")
                            .diaryContent(diary != null ? diary.getContent() : "")
                            .createdAt(history.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 기존에 윤서가 짜둔 저장 기능 - 전혀 안 건드리고 그대로 유지!
    @Transactional
    public Long saveHistory(UserHistoryRequestDto requestDto) {
        Diary diary = null;

        if (requestDto.getDiaryId() != null) {
            diary = diaryRepository.findById(requestDto.getDiaryId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 일기가 존재하지 않습니다. ID: " + requestDto.getDiaryId()));
        }

        UserHistory history = UserHistory.builder()
                .userId(requestDto.getUserId())
                .diary(diary)
                .emotion(requestDto.getEmotion())
                .temporaryBookId(requestDto.getTemporaryBookId())
                .build();

        return userHistoryRepository.save(history).getId();
    }

    // 기존에 윤서가 짜둔 전체 조회 기능 - 이것도 그대로 유지!
    public List<HistoryContextResponseDto> getAllHistories() {
        return userHistoryRepository.findAll().stream()
                .map(history -> HistoryContextResponseDto.builder()
                        .temporaryBookId(history.getTemporaryBookId())
                        .emotion(history.getEmotion())
                        .diaryId(history.getDiary() != null ? history.getDiary().getId() : null)
                        .diaryTitle(history.getDiary() != null ? history.getDiary().getTitle() : "일기 없음")
                        .diaryContent(history.getDiary() != null ? history.getDiary().getContent() : "")
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}