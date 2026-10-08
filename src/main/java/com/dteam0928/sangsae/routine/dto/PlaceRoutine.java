package com.dteam0928.sangsae.routine.dto;

/** 장소별 인기 루틴 한 줄 (내가 다니는 장소 섹션, 장소 상세 모달) */
public record PlaceRoutine(long routineId, String title, int performerCount, int fitScore) {
}
