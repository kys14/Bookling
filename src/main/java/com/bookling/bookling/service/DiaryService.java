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

    @Transactional
    public AiRecommendResponseDto saveDiaryWithAiAndHistory(RecommendRequestDto dto) {

        // 1. 유저가 존재하는지 검증 (없으면 404 예외 처리)
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + dto.getUserId()));

        // 2. 파이썬 FastAPI AI 추천 서버(ngrok) 가동 주소
        String ngrokUrl = "https://wrecking-aptitude-wrongly.ngrok-free.dev/recommend/similarity/gemini";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", dto.getContent()); // AI 서버 스펙에 맞게 일기 본문 세팅

        String emotionResult = "분석 실패";
        List<AiBookResponseDto> recommendedBooks = List.of();

        try {
            // AI 서버로 추천 결과(감정 + 책 목록) 수신
            AiRecommendResponseDto aiResponse = restTemplate.postForObject(ngrokUrl, requestBody, AiRecommendResponseDto.class);

            if (aiResponse != null) {
                emotionResult = aiResponse.getEmotion();   // 감정 라벨
                recommendedBooks = aiResponse.getBooks();  // 추천된 도서 목록 배열
            }
        } catch (Exception e) {
            // AI 서버가 꺼져있거나 예외가 발생했을 때 방어막 구축
            System.out.println("AI 서버 통신 실패: " + e.getMessage() + " (기본 분석 값으로 진행합니다.)");
        }

        // 3. AI가 분석해준 감정 결과와 함께 DB에 Diary 최종 저장
        Diary diary = new Diary();
        diary.setTitle(dto.getTitle());
        diary.setContent(dto.getContent());
        diary.setEmotion(emotionResult);
        diaryRepository.save(diary);

        // 4. 추천된 여러 권의 책 ID를 반복문 돌며 UserHistory 테이블에 각각 한 줄씩 전부 연동 저장
        if (recommendedBooks != null && !recommendedBooks.isEmpty()) {
            for (AiBookResponseDto book : recommendedBooks) {

                // 루프가 돌 때마다 완전히 새로운 객체를 "뉴(new)" 생성하여 덮어쓰기 방지
                UserHistory history = UserHistory.builder()
                        .user(user)
                        .diary(diary)
                        .emotion(emotionResult)
                        .bookId(book.getId())
                        .build();

                // 각 책 ID마다 데이터베이스에 즉시 insert 쿼리가 발사되도록 영속화
                userHistoryRepository.save(history);
            }
            userHistoryRepository.flush();
        }

        // 5. 안드로이드 앱 화면에 결과 팝업(추천 도서 리스트)을 바로 띄울 수 있게 AI 응답 스펙 그대로 리턴
        return new AiRecommendResponseDto(dto.getContent(), emotionResult, recommendedBooks);
    }

    // 일반 저장 기능 (AI 미연동 백업용)
    @Transactional
    public Long save(Long userId, DiaryRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        Diary diary = new Diary();
        diary.setTitle(requestDto.getTitle());
        diary.setContent(requestDto.getContent());

        return diaryRepository.save(diary).getId();
    }

    // 전체 조회 - 특정 유저 일기 목록
    public List<DiaryResponseDto> findAllByUserId(Long userId) {
        return diaryRepository.findAll().stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    // 전체 조회
    public List<DiaryResponseDto> findAll() {
        return diaryRepository.findAll().stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    // 상세 조회
    public DiaryResponseDto findById(Long id) {
        Diary entity = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        return new DiaryResponseDto(entity);
    }

    // 수정 기능
    @Transactional
    public Long update(Long id, DiaryRequestDto requestDto) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        diary.setTitle(requestDto.getTitle());
        diary.setContent(requestDto.getContent());
        return id;
    }

    // 삭제 기능
    @Transactional
    public void delete(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id));
        diaryRepository.delete(diary);
    }

    // 날짜 및 제목 검색
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