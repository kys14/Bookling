package com.bookling.bookling.controller;

import com.bookling.bookling.dto.HistoryContextResponseDto;
import com.bookling.bookling.service.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;

    // 모든 추천 이력 목록 조회 (테스트 및 관리자용)
    @GetMapping
    public ResponseEntity<List<HistoryContextResponseDto>> getAllHistories() {
        List<HistoryContextResponseDto> list = userHistoryService.getAllHistories();
        return ResponseEntity.ok(list);
    }

    // 특정 도서 ID로 연동된 과거 활동 기록(일기 문맥) 반환 API
    @GetMapping("/{temporaryBookId}")
    public ResponseEntity<List<HistoryContextResponseDto>> getHistoryContext(@PathVariable Long temporaryBookId) {
        List<HistoryContextResponseDto> response = userHistoryService.getContextByBookId(temporaryBookId);
        return ResponseEntity.ok(response);
    }
}