package com.bookling.bookling.service;

import com.bookling.bookling.dto.*;
import com.bookling.bookling.entity.*;
import com.bookling.bookling.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final RestTemplate restTemplate;

    // [활동1] 일기 저장 및 AI 분석 기반 추천 도서 적재 (활동 2)
    @Transactional
    public AiRecommendResponseDto saveDiaryWithAiAndHistory(RecommendRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + dto.getUserId()));

        String ngrokUrl = "https://wrecking-aptitude-wrongly.ngrok-free.dev/recommend/similarity/gemini";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", dto.getContent());

        String emotionResult = "분석 실패";
        List<AiBookResponseDto> recommendedBooks = List.of();

        try {
            AiRecommendResponseDto aiResponse = restTemplate.postForObject(ngrokUrl, requestBody, AiRecommendResponseDto.class);
            if (aiResponse != null) {
                emotionResult = aiResponse.getEmotion();
                recommendedBooks = aiResponse.getBooks();
            }

            Diary diary = new Diary();
            diary.setTitle(dto.getTitle());
            diary.setContent(dto.getContent());
            diary.setEmotion(emotionResult);
            diaryRepository.save(diary);

            if (recommendedBooks != null && !recommendedBooks.isEmpty()) {
                for (AiBookResponseDto book : recommendedBooks) {
                    UserHistory history = UserHistory.builder()
                            .user(user)
                            .diary(diary)
                            .emotion(emotionResult)
                            .bookId(book.getId())
                            .build();
                    userHistoryRepository.save(history);
                }
                userHistoryRepository.flush();
            }

        } catch (Exception e) {
            System.err.println("[AI External Error] 통신 실패로 인한 일반 백업 저장 프로세스 전환: " + e.getMessage());

            Diary backupDiary = new Diary();
            backupDiary.setTitle(dto.getTitle());
            backupDiary.setContent(dto.getContent());
            backupDiary.setEmotion("분석 실패");
            diaryRepository.save(backupDiary);

            UserHistory backupHistory = UserHistory.builder()
                    .user(user)
                    .diary(backupDiary)
                    .emotion("분석 실패")
                    .bookId(null)
                    .build();
            userHistoryRepository.save(backupHistory);
            userHistoryRepository.flush();
        }

        return new AiRecommendResponseDto(dto.getContent(), emotionResult, recommendedBooks);
    }

    // [활동2] 감정만 선택 시 도서 추천 및 활동 기록 저장
    @Transactional
    public AiRecommendResponseDto saveEmotionAndGetRecommend(Long userId, String emotion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        String ngrokUrl = "https://wrecking-aptitude-wrongly.ngrok-free.dev/recommend/emotion";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("emotion", emotion);

        List<AiBookResponseDto> recommendedBooks = List.of();
        boolean isAiSuccess = false;

        try {
            AiRecommendResponseDto aiResponse = restTemplate.postForObject(ngrokUrl, requestBody, AiRecommendResponseDto.class);
            if (aiResponse != null) {
                recommendedBooks = aiResponse.getBooks();
                isAiSuccess = true;
            }
        } catch (Exception e) {
            System.err.println("[AI External Error] Ngrok 오프라인으로 인한 감정 단독 백업 저장 프로세스 작동: " + e.getMessage());
        }

        if (isAiSuccess && recommendedBooks != null && !recommendedBooks.isEmpty()) {
            for (AiBookResponseDto book : recommendedBooks) {
                UserHistory history = UserHistory.builder()
                        .user(user)
                        .diary(null)
                        .emotion(emotion)
                        .bookId(book.getId())
                        .build();
                userHistoryRepository.save(history);
            }
        } else {
            UserHistory fallbackHistory = UserHistory.builder()
                    .user(user)
                    .diary(null)
                    .emotion(emotion)
                    .bookId(null)
                    .build();
            userHistoryRepository.save(fallbackHistory);
        }

        userHistoryRepository.flush();
        return new AiRecommendResponseDto(null, emotion, recommendedBooks);
    }

    // 전체 일기 목록 조회
    public List<DiaryResponseDto> findAll() {
        return diaryRepository.findAll().stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiaryResponseDto> findAllByUserId(Long userId) {
        return diaryRepository.findByUserId(userId).stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    // 일기 상세 조회
    public DiaryResponseDto findById(Long id) {
        Diary entity = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        return new DiaryResponseDto(entity);
    }

    // 일기 수정
    @Transactional
    public Long update(Long id, DiaryRequestDto requestDto) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        diary.setTitle(requestDto.getTitle());
        diary.setContent(requestDto.getContent());
        return id;
    }

    // 일기 삭제
    @Transactional
    public void delete(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        diaryRepository.delete(diary);
    }

    // 조건별 일기 검색
    public List<Diary> searchDiaries(String keyword, LocalDateTime start, LocalDateTime end) {
        if (keyword != null && start != null && end != null) {
            return diaryRepository.findByTitleContainingAndCreatedAtBetween(keyword, start, end);
        }
        if (keyword != null) {
            return diaryRepository.findByTitleContaining(keyword);
        }
        if (start != null && end != null) {
            return diaryRepository.findByCreatedAtBetween(start, end);
        }
        return diaryRepository.findAll();
    }
}