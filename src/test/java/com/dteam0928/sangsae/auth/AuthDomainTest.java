package com.dteam0928.sangsae.auth;

import com.dteam0928.sangsae.auth.service.password.BCryptPasswordEncoder;
import com.dteam0928.sangsae.auth.service.password.PasswordEncoder;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.validation.Validators;
import com.dteam0928.sangsae.user.service.NicknameGenerator;
import com.dteam0928.sangsae.user.service.RandomNicknameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthDomainTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private final NicknameGenerator nicknameGenerator = new RandomNicknameGenerator(
            List.of("여행자", "탐험가", "산책러"), 4
    );

    @Test
    @DisplayName("인증: 비밀번호 BCrypt 해싱 및 일치 여부 검증")
    void testPasswordHashing() {
        String rawPassword = "password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded), "원본 비밀번호와 일치해야 합니다.");
        assertFalse(passwordEncoder.matches("wrongPassword", encoded), "틀린 비밀번호는 불일치해야 합니다.");
    }

    @Test
    @DisplayName("인증: 이메일 유효성 검증 및 소문자 정규화")
    void testEmailValidation() {
        assertEquals("test@example.com", Validators.email("test@example.com"));
        assertEquals("user@domain.co.kr", Validators.email("  USER@Domain.co.kr  "));

        assertThrows(BusinessException.class, () -> Validators.email(""));
        assertThrows(BusinessException.class, () -> Validators.email(null));
        assertThrows(BusinessException.class, () -> Validators.email("not-an-email"));
        assertThrows(BusinessException.class, () -> Validators.email("@missing-username.com"));
    }

    @Test
    @DisplayName("인증: 신규 비밀번호 복잡도 규칙 검증 (8~64자, 영문+숫자 필수)")
    void testPasswordComplexityValidation() {
        assertEquals("pass1234", Validators.newPassword("pass1234"));
        assertEquals("VerySecurePass123!", Validators.newPassword("VerySecurePass123!"));

        assertThrows(BusinessException.class, () -> Validators.newPassword("short1")); // 8자 미만
        assertThrows(BusinessException.class, () -> Validators.newPassword("lettersOnly")); // 숫자 누락
        assertThrows(BusinessException.class, () -> Validators.newPassword("12345678")); // 영문 누락
        assertThrows(BusinessException.class, () -> Validators.newPassword(""));
    }

    @Test
    @DisplayName("온보딩: 자동 닉네임 생성 규칙 검증 (접두사 + 4자리 숫자)")
    void testNicknameGeneration() {
        String nickname = nicknameGenerator.generate();

        assertNotNull(nickname);
        assertTrue(nickname.startsWith("여행자") || nickname.startsWith("탐험가") || nickname.startsWith("산책러"));
        // 4자리 숫자가 붙으므로 최소 6자 이상
        assertTrue(nickname.length() >= 6);
    }
}
