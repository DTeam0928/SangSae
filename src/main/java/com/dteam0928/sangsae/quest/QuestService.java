package com.dteam0928.sangsae.quest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class QuestService {

    private final QuestTemplateRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    
    public QuestService() {
        this.questRepository = new QuestTemplateRepository();
        this.userQuestRepository = new UserQuestRepository();
    }

    //공개된 퀘스트 전체 조회
    public List<QuestTemplate> getPublicQuests() {
        return questRepository.findAll()
                .stream()
                .filter(quest -> "PUBLIC".equals(quest.getVisibility()))
                .toList();
    }

    //카테고리별 공개 퀘스트 조회
    public List<QuestTemplate> getPublicQuestsByCategory(String category) {
        return getPublicQuests()
                .stream()
                .filter(quest -> category.equalsIgnoreCase(quest.getCategory()))
                .toList();
    }
    
    // 퀘스트 상세 조회
    public QuestTemplate getQuestById(Long id) {
        QuestTemplate quest = questRepository.findById(id);
        if(quest == null)
            return null;
        if(!"PUBLIC".equals(quest.getVisibility()))
            return null;
        return quest;
    }
    
    //공개 퀘스트를 사용자의 일정에 담기
    public UserQuest addQuestToSchedule(Long templateId, Long userId, Long tripId, LocalDate scheduledDate, LocalTime targetTime) {
        //1. 원본 퀘스트 조회
        QuestTemplate template = getQuestById(templateId);
        if (template == null)
            return null;

        //2. 원본 퀘스트를 기반으로 사용자 퀘스트 생성
        UserQuest userQuest = new UserQuest(
                null,
                userId,
                tripId,
                template.getId(),
                template.getPlaceId(),
                template.getTitle(),
                scheduledDate,
                targetTime,
                template.getAuthType(),
                "TODO",
                template.getRewardXp(),
                null,
                LocalDateTime.now(),
                null
        );
        //3. 사용자의 일정에 저장
        return userQuestRepository.save(userQuest);
    }
}
