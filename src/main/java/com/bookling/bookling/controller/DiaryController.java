package com.bookling.bookling.controller;

import com.bookling.bookling.dto.AiRecommendResponseDto;
import com.bookling.bookling.dto.DiaryRequestDto;
import com.bookling.bookling.dto.DiaryResponseDto; // 추가됨
import com.bookling.bookling.entity.Diary;
import com.bookling.bookling.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    // 일기 작성 및 AI 추천 통합 API
    @PostMapping("/write")
    public ResponseEntity<AiRecommendResponseDto> writeDiary(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        AiRecommendResponseDto response = diaryService.saveDiaryWithAi(title, content);

        return ResponseEntity.ok(response);
    }

    // 1. 저장 (POST)
    @PostMapping
    public Long save(@RequestBody DiaryRequestDto requestDto) {
        return diaryService.save(requestDto);
    }

    // 2. 전체 조회 (GET)
    @GetMapping
    public List<DiaryResponseDto> findAllDiaries() {
        return diaryService.findAll();
    }

    // 3. 상세 조회 (GET)
    @GetMapping("/{id}")
    public DiaryResponseDto findById(@PathVariable Long id) {
        return diaryService.findById(id);
    }

    // 4. 수정 (PUT)
    @PutMapping("/{id}")
    public Long update(@PathVariable Long id, @RequestBody DiaryRequestDto requestDto) {
        return diaryService.update(id, requestDto);
    }

    // 5. 삭제 (DELETE)
    @DeleteMapping("/{id}")
    public Long delete(@PathVariable Long id) {
        diaryService.delete(id);
        return id; // 삭제된 id를 반환
    }

    // Controller에 검색 API 연결
    @GetMapping("/search")
    public ResponseEntity<List<Diary>> searchDiaries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Diary> diaries = diaryService.searchDiaries(keyword, start, end);
        return ResponseEntity.ok(diaries);
    }
}