package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.routine.dto.PlaceSummary;

import java.time.LocalDate;
import java.util.List;

/**
 * user_routines 한 줄 + 연결된 루틴 템플릿 정보.
 *
 * @param templateId        origin_template_id (직접 만든 비공개 루틴은 null)
 * @param templateCreatorId 템플릿을 만든 사람 (기본 제공 템플릿은 null)
 */
public record RoutineRow(long id, String title, LocalDate startDate, LocalDate endDate,
                         Long templateId, Long templateCreatorId, String visibility, String description,
                         List<String> categoryCodes, List<String> tags, PlaceSummary representativePlace,
                         int taskCount) {

    /** 내가 만든 템플릿과 연결돼 있으면 그 id (공개용 사본), 아니면 null */
    public Long ownTemplateId(long userId) {
        return templateId != null && templateCreatorId != null && templateCreatorId == userId ? templateId : null;
    }

    /** 다른 사람(또는 기본 제공) 루틴을 추가한 것 */
    public boolean adopted(long userId) {
        return templateId != null && ownTemplateId(userId) == null;
    }

    public boolean covers(LocalDate date) {
        return !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }
}
