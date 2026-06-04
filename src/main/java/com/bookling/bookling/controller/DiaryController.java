package com.bookling.bookling.controller;

import com.bookling.bookling.dto.AiRecommendResponseDto;
import com.bookling.bookling.dto.DiaryRequestDto;
import com.bookling.bookling.dto.DiaryResponseDto;
import com.bookling.bookling.dto.RecommendRequestDto;
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    // [활동1] 일기 작성 및 AI 도서 추천
    @PostMapping
    public ResponseEntity<AiRecommendResponseDto> saveDiaryWithAi(
            @RequestParam("userId") Long userId,
            @RequestBody DiaryRequestDto requestDto) {

        RecommendRequestDto recommendDto = new RecommendRequestDto(userId, requestDto.getTitle(), requestDto.getContent());
        AiRecommendResponseDto response = diaryService.saveDiaryWithAiAndHistory(recommendDto);
        return ResponseEntity.ok(response);
    }

    // [활동1] 감정만 선택해 도서 추천 및 히스토리 저장
    @PostMapping("/emotion-recommend")
    public ResponseEntity<AiRecommendResponseDto> recommendByEmotion(
            @RequestParam("userId") Long userId,
            @RequestParam("emotion") String emotion) {

        AiRecommendResponseDto response = diaryService.saveEmotionAndGetRecommend(userId, emotion);
        return ResponseEntity.ok(response);
    }

    // 일기 목록 조회
    @GetMapping
    public ResponseEntity<List<DiaryResponseDto>> findAllDiaries(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok(diaryService.findAllByUserId(userId));
        }
        return ResponseEntity.ok(diaryService.findAll());
    }

    // 일기 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(diaryService.findById(id));
    }

    // 일기 수정
    @PutMapping("/{id}")
    public ResponseEntity<Long> update(@PathVariable Long id, @RequestBody DiaryRequestDto requestDto) {
        return ResponseEntity.ok(diaryService.update(id, requestDto));
    }

    // 일기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> delete(@PathVariable Long id) {
        diaryService.delete(id);
        return ResponseEntity.ok(id);
    }

    // 조건별 일기 검색 (키워드 및 기간 필터링)
    @GetMapping("/search")
    public ResponseEntity<List<Diary>> searchDiaries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(diaryService.searchDiaries(keyword, start, end));
    }
}