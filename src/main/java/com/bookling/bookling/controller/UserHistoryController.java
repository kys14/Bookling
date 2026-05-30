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

    // [테스트 및 관리용] 전체 추천 이력 목록 조회
    @GetMapping
    public ResponseEntity<List<HistoryContextResponseDto>> getAllHistories() {
        return ResponseEntity.ok(userHistoryService.getAllHistories());
    }

    // 특정 도서 ID 연동 과거 활동 기록(일기 문맥) 반환
    @GetMapping("/{bookId}")
    public ResponseEntity<List<HistoryContextResponseDto>> getHistoryContext(@PathVariable Long bookId) {
        return ResponseEntity.ok(userHistoryService.getContextByBookId(bookId));
    }
}