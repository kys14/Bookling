package com.bookling.bookling.service;

import com.bookling.bookling.dto.AiBookResponseDto;
import com.bookling.bookling.dto.AiRecommendResponseDto;
import com.bookling.bookling.dto.DiaryRequestDto;
import com.bookling.bookling.dto.DiaryResponseDto;
import com.bookling.bookling.dto.RecommendRequestDto;
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.entity.User;
import com.bookling.bookling.entity.UserHistory;
import com.bookling.bookling.repository.DiaryRepository;
import com.bookling.bookling.repository.UserHistoryRepository;
import com.bookling.bookling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final RestTemplate restTemplate;

    //[활동 2] 일기 작성 및 AI 분석 추천 흐름 (안전성 강화 버전)
    @Transactional
    public AiRecommendResponseDto saveDiaryWithAiAndHistory(RecommendRequestDto dto) {
        // 1. 유저가 존재하는지 검증
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + dto.getUserId()));

        String ngrokUrl = "https://wrecking-aptitude-wrongly.ngrok-free.dev/recommend/similarity/gemini";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", dto.getContent());

        String emotionResult = "분석 실패";
        List<AiBookResponseDto> recommendedBooks = List.of();

        try {
            // AI 서버로 추천 결과 수신
            AiRecommendResponseDto aiResponse = restTemplate.postForObject(ngrokUrl, requestBody, AiRecommendResponseDto.class);

            if (aiResponse != null) {
                emotionResult = aiResponse.getEmotion();
                recommendedBooks = aiResponse.getBooks();
            }

            // [정상 흐름] AI 분석 성공 시 정상 적재
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
            System.out.println("🚨 AI 서버 통신 실패: " + e.getMessage() + " (일반 백업 저장 흐름으로 전환합니다.)");

            // 1. 일기 본문 백업 저장
            Diary backupDiary = new Diary();
            backupDiary.setTitle(dto.getTitle());
            backupDiary.setContent(dto.getContent());
            backupDiary.setEmotion("분석 실패");
            diaryRepository.save(backupDiary);

            // 2. AI는 실패했지만 유저 활동 히스토리에 '분석 실패' 상태로 흔적 남기기
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

    //[활동 1] 오직 감정만 선택했을 때의 AI 도서 추천 및 히스토리 저장 흐름 (최후의 방어선 탑재)
    @Transactional
    public AiRecommendResponseDto saveEmotionAndGetRecommend(Long userId, String emotion) {
        // 1. 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. 파이썬 FastAPI AI 추천 서버 주소 (현재 에러 로그에 찍힌 주소)
        String ngrokUrl = "https://wrecking-aptitude-wrongly.ngrok-free.dev/recommend/emotion";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("emotion", emotion);

        List<AiBookResponseDto> recommendedBooks = List.of();
        boolean isAiSuccess = false;

        try {
            // AI 서버로 추천 결과 수신 시도
            AiRecommendResponseDto aiResponse = restTemplate.postForObject(ngrokUrl, requestBody, AiRecommendResponseDto.class);
            if (aiResponse != null) {
                recommendedBooks = aiResponse.getBooks();
                isAiSuccess = true;
            }
        } catch (Exception e) {
            //  ngrok 오프라인 에러 포획 문두
            System.out.println("🚨 AI 서버 추천 실패(오프라인): " + e.getMessage() + " -> 감정 선택 기록 단독 백업을 가동합니다.");
        }

        // 3. 데이터베이스 적재 분기 처리
        if (isAiSuccess && recommendedBooks != null && !recommendedBooks.isEmpty()) {
            // [정상 흐름] AI 터널이 정상적으로 열려 있어서 책 추천 목록을 받아왔을 때
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
            // 책 목록 없을 시 감정만 저장
            UserHistory fallbackHistory = UserHistory.builder()
                    .user(user)
                    .diary(null)          // 일기 없음
                    .emotion(emotion)     // 사용자가 선택한 감정 (예: '슬픔')
                    .bookId(null)         // 추천 도서가 없으므로 null 매핑
                    .build();
            userHistoryRepository.save(fallbackHistory);
        }

        userHistoryRepository.flush();

        return new AiRecommendResponseDto(null, emotion, recommendedBooks);
    }

    // 💡 아래 기존 조회/수정/삭제 단순 CRUD 메서드들은 완벽히 보존 (변경 없음)
    public List<DiaryResponseDto> findAllByUserId(Long userId) {
        return diaryRepository.findAll().stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<DiaryResponseDto> findAll() {
        return diaryRepository.findAll().stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    public DiaryResponseDto findById(Long id) {
        Diary entity = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        return new DiaryResponseDto(entity);
    }

    @Transactional
    public Long update(Long id, DiaryRequestDto requestDto) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        diary.setTitle(requestDto.getTitle());
        diary.setContent(requestDto.getContent());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        diaryRepository.delete(diary);
    }

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