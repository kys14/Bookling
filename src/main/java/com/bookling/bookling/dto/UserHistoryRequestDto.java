package com.bookling.bookling.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserHistoryRequestDto {
    private Long userId;
    private Long diaryId;
    private String emotion;
    private Long temporaryBookId;
}