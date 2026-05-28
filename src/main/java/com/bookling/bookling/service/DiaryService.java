package com.bookling.bookling.service;

import com.bookling.bookling.dto.AiRecommendResponseDto;
import com.bookling.bookling.dto.DiaryRequestDto;
import com.bookling.bookling.dto.DiaryResponseDto; // 추가됨
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)

public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final RestTemplate restTemplate;

    // 일기 작성 시 AI 서버에서 감정을 분석 받아 저장, 추천 도서 목록 반환
    @Transactional
    public AiRecommendResponseDto saveDiaryWithAi(String title, String content) {

        // 1. FastAPI 서버
        String fastapiUrl = "http://localhost:8000/recommend";

        // 2. AI 서버가 요구한 포맷으로 요청 데이터 구성
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", content); // 일기 본문을 보냄

        String emotionResult = "분석 실패";
        List<com.bookling.bookling.dto.AiBookResponseDto> recommendedBooks = List.of();

        try {
            // 3. AI 서버로 POST 요청 발사하여 분석 및 추천 결과 수신
            AiRecommendResponseDto aiResponse = restTemplate.postForObject(fastapiUrl, requestBody, AiRecommendResponseDto.class);

            if (aiResponse != null) {
                emotionResult = aiResponse.getEmotion();   // AI가 예측한 감정 라벨
                recommendedBooks = aiResponse.getBooks();  // AI가 추천한 도서 목록 배열
            }
        } catch (Exception e) {
            // AI 서버가 꺼져있을 때 백엔드 서버까지 오류남을 방지
            System.out.println("AI 서버 연결 실패: " + e.getMessage() + " (기본 값으로 일기를 먼저 저장합니다.)");
        }

        // 4. AI가 분석해준 감정 결과를 들고 DB에 Diary 엔티티 최종 적재
        Diary diary = new Diary();
        diary.setTitle(title);
        diary.setContent(content);
        diary.setEmotion(emotionResult);
        diaryRepository.save(diary);

        // 5. 안드로이드 앱이 화면에 추천 도서 팝업을 띄울 수 있게 AI 응답 포맷 그대로 리턴
        return new AiRecommendResponseDto(content, emotionResult, recommendedBooks);
    }

    // 저장 기능
    public Long save(DiaryRequestDto requestDto) {
        Diary diary = new Diary();
        diary.setTitle(requestDto.getTitle());
        diary.setContent(requestDto.getContent());

        return diaryRepository.save(diary).getId();
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<DiaryResponseDto> findAll() {
        return diaryRepository.findAll().stream()
                .map(DiaryResponseDto::new) // [1단계]에서 만든 DTO 생성자 사용
                .collect(Collectors.toList());
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public DiaryResponseDto findById(Long id) {
        Diary entity = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다. id=" + id)); // [3단계] 예외 처리
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

    // 날짜 및 제목 검색 일기 조회
    @Transactional(readOnly = true)
    public List<Diary> searchDiaries(String keyword, LocalDateTime start, LocalDateTime end) {
        // 키워드와 날짜 기간이 모두 들어온 경우
        if (keyword != null && start != null && end != null) {
            return diaryRepository.findByTitleContainingAndCreatedAtBetween(keyword, start, end);
        }
        // 키워드만 들어온 경우
        if (keyword != null) {
            return diaryRepository.findByTitleContaining(keyword);
        }
        // 날짜 기간만 들어온 경우
        if (start != null && end != null) {
            return diaryRepository.findByCreatedAtBetween(start, end);
        }
        // 아무 조건도 없으면 전체 조회
        return diaryRepository.findAll();
    }
}