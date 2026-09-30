package com.dteam0928.sangsae.quest.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.dto.QuestLog;
import com.dteam0928.sangsae.quest.dto.UserQuestBookmark;
import com.dteam0928.sangsae.quest.repository.QuestTemplateRepository;
import com.dteam0928.sangsae.quest.repository.UserQuestRepository;
import com.dteam0928.sangsae.quest.repository.QuestLogRepository;
import com.dteam0928.sangsae.quest.repository.UserQuestBookmarkRepository;

public class QuestService {

    private final QuestTemplateRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final QuestLogRepository questLogRepository;
    private final UserQuestBookmarkRepository bookmarkRepository;    
    
    public QuestService() {
        this.questRepository = new QuestTemplateRepository();
        this.userQuestRepository = new UserQuestRepository();
        this.questLogRepository = new QuestLogRepository();
        this.bookmarkRepository = new UserQuestBookmarkRepository();
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
    
    //사용자가 일정에 담은 퀘스트 전체 조회
    public List<UserQuest> getUserQuests() {
        return userQuestRepository.findAll();
    }
    
    //사용자가 일정에 담은 퀘스트 조건별 조회
    public List<UserQuest> getUserQuestsByDate(
            Long userId,
            Long tripId,
            LocalDate scheduledDate) {
        return userQuestRepository
                .findByUserIdAndTripIdAndScheduledDate(
                        userId,
                        tripId,
                        scheduledDate
                );
    }
    //퀘스트 완료 처리 및 저장
    public QuestLog completeQuest(Long userQuestId, Long userId, String proofImageUrl, String proofText) {
        UserQuest userQuest = userQuestRepository.findById(userQuestId);
        if (userQuest == null) return null;
        LocalDateTime completedAt = LocalDateTime.now();
        userQuest.setStatus("COMPLETED");
        userQuest.setCompletedAt(completedAt);
        QuestLog questLog = new QuestLog(
                null,
                userId,
                userQuest.getId(),
                completedAt,
                userQuest.getAuthType(),
                proofImageUrl,
                proofText,
                userQuest.getRewardXp()
        );
        return questLogRepository.save(questLog);
    }
    // 퀘스트 북마크 저장
    public UserQuestBookmark addBookmark(Long userId, Long questTemplateId, String folderName) {

        // 존재하고 공개된 퀘스트인지 확인
        QuestTemplate quest = getQuestById(questTemplateId);
        if (quest == null)
            return null;

        // 이미 저장되어 있으면 중복 저장하지 않음
        UserQuestBookmark existingBookmark =
                bookmarkRepository.findByUserIdAndQuestTemplateId(userId, questTemplateId);
        if (existingBookmark != null) 
            return existingBookmark;

        // DB 설계의 기본 폴더명과 동일하게 처리
        if (folderName == null || folderName.isBlank()) 
            folderName = "기본";
        UserQuestBookmark bookmark = new UserQuestBookmark(
                userId,
                questTemplateId,
                folderName,
                LocalDateTime.now()
        );
        return bookmarkRepository.save(bookmark);
    }
    
    //사용자가 저장한 퀘스트 북마크 조회
    public List<UserQuestBookmark> getBookmarksByUserId(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    //퀘스트 북마크 여부 확인
    public boolean isBookmarked(Long userId, Long questTemplateId) {
        return bookmarkRepository.findByUserIdAndQuestTemplateId(userId, questTemplateId) != null;
    }

    //퀘스트 북마크 삭제
    public boolean removeBookmark(Long userId, Long questTemplateId) {
        return bookmarkRepository.delete(userId, questTemplateId);
    }
    
    public List<QuestTemplate> getBookmarkedQuests(Long userId) {
    	List<UserQuestBookmark> bookmarks = bookmarkRepository.findByUserId(userId);
    	List<Long> questTemplateIds = bookmarks.stream()
    			.map(UserQuestBookmark::getQuestTemplateId)
    			.toList();
    	return questRepository.findByIds(questTemplateIds);
    }
    
    //특정 폴더에 북마크한 퀘스트 목록 조회
    public List<QuestTemplate> getBookmarkedQuestsByFolder(Long userId, String folderName) {
    	List<UserQuestBookmark> bookmarks =
    			bookmarkRepository.findByUserIdAndFolderName(userId, folderName);
    	List<Long> questTemplateIds = bookmarks.stream()
    			.map(UserQuestBookmark::getQuestTemplateId)
    			.toList();
    	return questRepository.findByIds(questTemplateIds);
    }
    
    //북마크를 다른 폴더로 이동
    public boolean moveBookmarkToFolder(Long userId, Long questTemplateId, String folderName) {
        if (folderName == null || folderName.isBlank()) folderName = "기본";
        return bookmarkRepository.moveToFolder(userId, questTemplateId, folderName);
    }
}
