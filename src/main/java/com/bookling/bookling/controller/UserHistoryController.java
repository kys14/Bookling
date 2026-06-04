package com.bookling.bookling.controller;

import com.bookling.bookling.dto.HistoryContextResponseDto;
import com.bookling.bookling.service.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;

    // 통합 조회
    @GetMapping
    public ResponseEntity<List<HistoryContextResponseDto>> getAllHistories(@RequestParam(value = "userId", required = false) Long userId) {

        // 테스트 및 관리용
        if (userId == null) {
            return ResponseEntity.ok(userHistoryService.getAllHistories());
        }

        // 마이페이지 서재
        List<HistoryContextResponseDto> userHistories = userHistoryService.getAllHistories().stream()
                .filter(history -> history.getDiaryId() == null ||
                        userHistoryService.isHistoryOwnedByUser(history.getDiaryId(), userId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userHistories);
    }

    // 특정 도서 ID 연동 과거 활동 기록(일기 문맥) 반환
    @GetMapping("/{bookId}")
    public ResponseEntity<List<HistoryContextResponseDto>> getHistoryContext(@PathVariable Long bookId) {
        return ResponseEntity.ok(userHistoryService.getContextByBookId(bookId));
    }
}