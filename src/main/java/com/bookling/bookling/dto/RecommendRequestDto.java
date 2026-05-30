package com.bookling.bookling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendRequestDto {
    private Long userId;       // 활동 기록 연동을 위한 유저 고유 번호
    private String title;      // 사용자가 쓴 일기 제목
    private String content;    // 사용자가 쓴 일기 본문
}