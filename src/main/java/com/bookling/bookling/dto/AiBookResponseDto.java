package com.bookling.bookling.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiBookResponseDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String isbn13;
    private String emotion_tag;
}