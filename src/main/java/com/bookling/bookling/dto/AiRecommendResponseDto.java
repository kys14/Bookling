package com.bookling.bookling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendResponseDto {
    private String text;
    private String emotion;
    private List<AiBookResponseDto> books; // 위에서 만든 책 DTO를 리스트로 매핑!
}