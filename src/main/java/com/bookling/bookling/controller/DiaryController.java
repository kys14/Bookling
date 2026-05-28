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

@CrossOrigin(origins = "*") // 내일 프론트엔드 CORS 에러 방지용 필수 어노테이션!
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping("/write")
    public ResponseEntity<AiRecommendResponseDto> writeDiaryWithAi(@RequestBody RecommendRequestDto requestDto) {
        AiRecommendResponseDto response = diaryService.saveDiaryWithAiAndHistory(requestDto);
        return ResponseEntity.ok(response);
    }

    // 일반 일기 저장 (AI 연동 없는 백업용)
    @PostMapping
    public ResponseEntity<Long> save(
            @RequestParam("userId") Long userId,
            @RequestBody DiaryRequestDto requestDto) {
        Long id = diaryService.save(userId, requestDto);
        return ResponseEntity.ok(id);
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