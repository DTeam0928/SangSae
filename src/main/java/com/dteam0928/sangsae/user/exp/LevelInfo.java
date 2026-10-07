package com.dteam0928.sangsae.user.exp;

// 사용자 레벨 및 경험치 세부 정보 DTO
// UI 프로그레스바 및 마이페이지/헤더 렌더링에 필요한 파생 수치를 불변으로 캡슐화
public record LevelInfo(
        int level,
        String title,
        int totalXp,
        int currentLevelXp,
        int nextLevelRemainingXp,
        double progressPercent,
        boolean isMaxLevel
) {}
