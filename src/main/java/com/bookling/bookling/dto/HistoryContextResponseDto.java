package com.bookling.bookling.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class HistoryContextResponseDto {
    private Long temporaryBookId;
    private String emotion;
    private Long diaryId;
    private String diaryTitle;
    private String diaryContent;
    private LocalDateTime createdAt;
}