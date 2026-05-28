package com.bookling.bookling.dto;

import com.bookling.bookling.entity.Diary;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"id", "title", "content", "createdAt"})
public class DiaryResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환
    public DiaryResponseDto(Diary entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.createdAt = entity.getCreatedAt();
    }
}