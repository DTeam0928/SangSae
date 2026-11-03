package com.dteam0928.sangsae.quest.dto;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 사용자 일정에 등록된 퀘스트와 목표 장소 정보를 함께 제공하는 응답 DTO
 */
public record UserQuestDetailResponseDto(
        Long id,
        Long userId,
        Long tripId,
        Long originTemplateId,
        Long placeId,
        String title,
        LocalDate scheduledDate,
        LocalTime targetTime,
        String authType,
        String status,
        int rewardXp,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Integer authRadiusM,
        PlaceResponseDto place
) {
    public static UserQuestDetailResponseDto of(UserQuest userQuest, PlaceResponseDto place) {
        return new UserQuestDetailResponseDto(
                userQuest.getId(),
                userQuest.getUserId(),
                userQuest.getTripId(),
                userQuest.getOriginTemplateId(),
                userQuest.getPlaceId(),
                userQuest.getTitle(),
                userQuest.getScheduledDate(),
                userQuest.getTargetTime(),
                userQuest.getAuthType(),
                userQuest.getStatus(),
                userQuest.getRewardXp(),
                userQuest.getMemo(),
                userQuest.getCreatedAt(),
                userQuest.getCompletedAt(),
                userQuest.getAuthRadiusM(),
                place
        );
    }
}
