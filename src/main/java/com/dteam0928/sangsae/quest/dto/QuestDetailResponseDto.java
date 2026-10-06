package com.dteam0928.sangsae.quest.dto;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;

import java.time.LocalDateTime;

/**
 * 퀘스트 상세 정보와 연동된 장소 정보를 함께 제공하는 응답 DTO
 */
public record QuestDetailResponseDto(
        Long id,
        Long creatorId,
        Long placeId,
        String title,
        String description,
        String category,
        int estimatedMinutes,
        int rewardXp,
        String authType,
        String visibility,
        String thumbnailUrl,
        int bookmarkCount,
        Integer completionCount,
        LocalDateTime createdAt,
        PlaceResponseDto place,
        boolean bookmarked
) {
    public static QuestDetailResponseDto of(QuestTemplate template, PlaceResponseDto place, boolean bookmarked) {
        return new QuestDetailResponseDto(
                template.getId(),
                template.getCreatorId(),
                template.getPlaceId(),
                template.getTitle(),
                template.getDescription(),
                template.getCategory(),
                template.getEstimatedMinutes(),
                template.getRewardXp(),
                template.getAuthType(),
                template.getVisibility(),
                template.getThumbnailUrl(),
                template.getBookmarkCount(),
                template.getCompletionCount(),
                template.getCreatedAt(),
                place,
                bookmarked
        );
    }
}
