package com.bookling.bookling.controller;

import com.bookling.bookling.dto.HistoryContextResponseDto;
import com.bookling.bookling.dto.UserHistoryRequestDto;
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

    // 모든 이력 목록
    @GetMapping
    public ResponseEntity<List<HistoryContextResponseDto>> getAllHistories() {
        List<HistoryContextResponseDto> list = userHistoryService.getAllHistories();
        return ResponseEntity.ok(list);
    }

    // 특정 도서 ID로 연동된 활동 기록 반환 API
    @GetMapping("/{temporaryBookId}")
    public ResponseEntity<List<HistoryContextResponseDto>> getHistoryContext(@PathVariable Long temporaryBookId) {
        List<HistoryContextResponseDto> response = userHistoryService.getContextByBookId(temporaryBookId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Long> createHistory(@RequestBody UserHistoryRequestDto requestDto) {
        Long historyId = userHistoryService.saveHistory(requestDto);
        return ResponseEntity.ok(historyId);
    }
}