package com.bookling.bookling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 기록(JPA Auditing)
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자의 기록인지 구분
    private Long userId;

    // 일기와의 연동 고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = true)
    private Diary diary;

    // 감정 값
    private String emotion;

    // 임시 도서 ID (가상의 1001, 1002 같은 번호 저장)
    private Long temporaryBookId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}