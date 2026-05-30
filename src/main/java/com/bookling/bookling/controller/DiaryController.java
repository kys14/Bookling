package com.bookling.bookling.controller;

import com.bookling.bookling.dto.AiRecommendResponseDto;
import com.bookling.bookling.dto.DiaryRequestDto;
import com.bookling.bookling.dto.DiaryResponseDto;
import com.bookling.bookling.dto.RecommendRequestDto;
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.service.DiaryService;
import lombok.RequiredArgsConstructor;
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

    // [활동 2-A] 바디 전체를 RecommendRequestDto로 한 번에 받을 때의 정석 AI 추천 주소
    @PostMapping("/write")
    public ResponseEntity<AiRecommendResponseDto> writeDiaryWithAi(@RequestBody RecommendRequestDto requestDto) {
        AiRecommendResponseDto response = diaryService.saveDiaryWithAiAndHistory(requestDto);
        return ResponseEntity.ok(response);
    }

    // [활동 2-B] 현재 프론트엔드가 쏘는 주소 방식 처리 (?userId=1 + JSON Body)
    @PostMapping
    public ResponseEntity<AiRecommendResponseDto> saveDiaryWithAi(
            @RequestParam("userId") Long userId,
            @RequestBody DiaryRequestDto requestDto) {

        // 생성자 방식으로 파라미터와 바디 데이터를 RecommendRequestDto로 묶어줌
        RecommendRequestDto recommendDto = new RecommendRequestDto(userId, requestDto.getTitle(), requestDto.getContent());

        // AI 통신 성공 시 정상 저장 / 통신 오류 발생 시 catch문에서 '분석 실패'로 일기 내용만 백업 저장 작동!
        AiRecommendResponseDto response = diaryService.saveDiaryWithAiAndHistory(recommendDto);
        return ResponseEntity.ok(response);
    }

    // [활동 1] 사용자가 오직 감정만 선택해서 AI 도서 추천을 요청할 때의 전용 API
    @PostMapping("/emotion-recommend")
    public ResponseEntity<AiRecommendResponseDto> recommendByEmotion(
            @RequestParam("userId") Long userId,
            @RequestParam("emotion") String emotion) {

        AiRecommendResponseDto response = diaryService.saveEmotionAndGetRecommend(userId, emotion);
        return ResponseEntity.ok(response);
    }

    // 전체 일기 목록 조회
    @GetMapping
    public ResponseEntity<List<DiaryResponseDto>> findAllDiaries() {
        List<DiaryResponseDto> list = diaryService.findAll();
        return ResponseEntity.ok(list);
    }

    // 일기 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponseDto> findById(@PathVariable Long id) {
        DiaryResponseDto dto = diaryService.findById(id);
        return ResponseEntity.ok(dto);
    }

    // 일기 수정
    @PutMapping("/{id}")
    public ResponseEntity<Long> update(@PathVariable Long id, @RequestBody DiaryRequestDto requestDto) {
        Long updatedId = diaryService.update(id, requestDto);
        return ResponseEntity.ok(updatedId);
    }

    // 일기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> delete(@PathVariable Long id) {
        diaryService.delete(id);
        return ResponseEntity.ok(id);
    }

    // 일기 검색 (제목 키워드 및 날짜 기간 필터링)
    @GetMapping("/search")
    public ResponseEntity<List<Diary>> searchDiaries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Diary> diaries = diaryService.searchDiaries(keyword, start, end);
        return ResponseEntity.ok(diaries);
    }
}