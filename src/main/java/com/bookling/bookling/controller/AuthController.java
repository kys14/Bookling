package com.bookling.bookling.controller;

import com.bookling.bookling.entity.User;
import com.bookling.bookling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    // 1. 디바이스 고유값 기반 자동 가입 및 로그인
    @PostMapping("/device-login")
    @Transactional
    public ResponseEntity<?> deviceLogin(@RequestBody Map<String, String> request) {
        String deviceUuid = request.get("deviceUuid");

        if (deviceUuid == null || deviceUuid.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bad Request",
                    "message", "디바이스 고유값(deviceUuid)은 필수입니다."
            ));
        }

        User user = userRepository.findByDeviceUuid(deviceUuid)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setDeviceUuid(deviceUuid);

                    // 직접 엄선한 11개의 감성 행동/독서 테마 배열
                    String[] adjectives = {
                            "사색하는", "밑줄긋는", "글을짓는", "마음을읽는", "감상하는",
                            "책장을넘기는", "기억을남기는", "갈피를잡는", "마음기록",
                            "문장수집", "도서수집"
                    };

                    int randomIdx = (int)(Math.random() * adjectives.length);
                    String selectedAdjective = adjectives[randomIdx];

                    int randomNumber = (int)(Math.random() * 9000) + 1000;

                    newUser.setName(selectedAdjective + "_기록가_" + randomNumber);

                    return userRepository.save(newUser);
                });

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "name", user.getName()
        ));
    }

    // 2. 유저 닉네임 변경 및 설정 API (프론트에서 입력, 처리)
    @PutMapping("/update-nickname")
    @Transactional
    public ResponseEntity<?> updateNickname(@RequestBody Map<String, Object> request) {
        Object userIdObj = request.get("userId");
        String nickname = (String) request.get("nickname");

        if (userIdObj == null || nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bad Request",
                    "message", "userId와 nickname 값은 필수입니다."
            ));
        }

        Long userId = Long.valueOf(userIdObj.toString());

        // 유저 조회 후 닉네임 변경
        return userRepository.findById(userId)
                .map(user -> {
                    user.setName(nickname.trim());
                    userRepository.save(user); // Setter 구조를 활용한 변경 내역 저장
                    return ResponseEntity.ok(Map.of(
                            "message", "닉네임 변경 성공",
                            "userId", user.getId(),
                            "name", user.getName()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                        "error", "Not Found",
                        "message", "존재하지 않는 유저 고유 ID입니다."
                )));
    }
}