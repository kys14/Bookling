package com.bookling.bookling.service;

import com.bookling.bookling.dto.HistoryContextResponseDto;
import com.bookling.bookling.dto.UserHistoryRequestDto;
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.entity.User;
import com.bookling.bookling.entity.UserHistory;
import com.bookling.bookling.repository.DiaryRepository; // 패키지 경로 깔끔하게 임포트로 처리했어!
import com.bookling.bookling.repository.UserHistoryRepository;
import com.bookling.bookling.repository.UserRepository;
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
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    // 리스트 반환해 중복 도서 문제 해결
    public List<HistoryContextResponseDto> getContextByBookId(Long temporaryBookId) {

        // 1. 임시 도서 ID로 해당하는 모든 기록을 리스트로 조회
        List<UserHistory> histories = userHistoryRepository.findByTemporaryBookId(temporaryBookId);

        // 2. 해당 책으로 추천받은 이력이 없으면 예외 처리
        if (histories.isEmpty()) {
            throw new IllegalArgumentException("해당 도서의 추천 이력이 존재하지 않습니다. ID: " + temporaryBookId);
        }

        // 루프(Stream)를 돌면서 찾아온 여러 개의 기록을 전부 DTO 상자에 담아서 리스트로 반환
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

    @Transactional
    public Long saveHistory(UserHistoryRequestDto requestDto) {
        // 유저 정보를 DB에서 조회
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. ID: " + requestDto.getUserId()));

        Diary diary = null;

        if (requestDto.getDiaryId() != null) {
            diary = diaryRepository.findById(requestDto.getDiaryId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 일기가 존재하지 않습니다. ID: " + requestDto.getDiaryId()));
        }

        UserHistory history = UserHistory.builder()
                .user(user)
                .diary(diary)
                .emotion(requestDto.getEmotion())
                .temporaryBookId(requestDto.getTemporaryBookId())
                .build();

        return userHistoryRepository.save(history).getId();
    }

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