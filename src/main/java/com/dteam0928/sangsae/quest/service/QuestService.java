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
import com.dteam0928.sangsae.common.db.TransactionTemplate;

public class QuestService {

    private final QuestTemplateRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final QuestLogRepository questLogRepository;
    private final UserQuestBookmarkRepository bookmarkRepository;    
    private final TransactionTemplate tx;
    
    public QuestService(TransactionTemplate tx) {
    	this.tx = tx;
    	this.questRepository = new QuestTemplateRepository();
        this.userQuestRepository = new UserQuestRepository();
        this.questLogRepository = new QuestLogRepository();
        this.bookmarkRepository = new UserQuestBookmarkRepository();
    }

    //공개된 퀘스트 전체 조회
    public List<QuestTemplate> getPublicQuests() {
        return tx.read(em ->
            questRepository.findAll(em)
                .stream()
                .filter(quest -> "PUBLIC".equals(quest.getVisibility()))
                .toList()
        );
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
        return tx.read(em -> {
            QuestTemplate quest = questRepository.findById(em, id);
            if (quest == null)
                return null;
            if (!"PUBLIC".equals(quest.getVisibility()))
                return null;
            return quest;
        });
    }
    
    //공개 퀘스트를 사용자의 일정에 담기
    public UserQuest addQuestToSchedule(
            Long templateId,
            Long userId,
            Long tripId,
            LocalDate scheduledDate,
            LocalTime targetTime,
            Integer authRadiusM) {

        // scheduled_date는 DB에서 NOT NULL
        if (scheduledDate == null) {
            throw new IllegalArgumentException("퀘스트 날짜는 필수입니다.");
        }
        final Integer requestedAuthRadiusM = authRadiusM;
        return tx.inTransaction(em -> {

            // 1. 원본 퀘스트 조회
            QuestTemplate template = questRepository.findById(em, templateId);
            if (template == null || !"PUBLIC".equals(template.getVisibility())) {
                return null;
            }

            // 2. LOCATION 인증 퀘스트일 경우 인증 반경 검사
            Integer finalAuthRadiusM;
            if ("LOCATION".equals(template.getAuthType())) {
                if (requestedAuthRadiusM == null
                        || requestedAuthRadiusM < 100
                        || requestedAuthRadiusM > 1000) {
                    throw new IllegalArgumentException("위치 인증 반경은 100m 이상 1000m 이하로 설정해야 합니다.");
                }
                finalAuthRadiusM = requestedAuthRadiusM;
            } else {
                finalAuthRadiusM = null;
            }

            // 3. 원본 퀘스트를 기반으로 사용자 퀘스트 생성
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
                    null,
                    finalAuthRadiusM
            );

            // 4. 실제 user_quests 테이블에 저장
            return userQuestRepository.save(em, userQuest);
        });
    }

    // 사용자가 일정에 담은 퀘스트 조건별 조회
    public List<UserQuest> getUserQuestsByDate(
            Long userId,
            Long tripId,
            LocalDate scheduledDate) {
        return tx.read(em ->
            userQuestRepository.findByUserIdAndTripIdAndScheduledDate(
                    em,
                    userId,
                    tripId,
                    scheduledDate
            )
        );
    }

    // 사용자 퀘스트 일정 수정
    public UserQuest updateQuestSchedule(
            Long userQuestId,
            Long userId,
            LocalDate scheduledDate,
            LocalTime targetTime,
            String memo) {

        // scheduled_date는 DB에서 NOT NULL
        if (scheduledDate == null) {
            throw new IllegalArgumentException("퀘스트 날짜는 필수입니다.");
        }

        // memo는 DB에서 VARCHAR(255)
        if (memo != null && memo.length() > 255) {
            throw new IllegalArgumentException("메모는 255자 이하로 입력해야 합니다.");
        }
        return tx.inTransaction(em -> {

            // 1. 사용자 퀘스트 조회
            UserQuest userQuest = userQuestRepository.findById(em, userQuestId);
            if (userQuest == null) {
                return null;
            }

            // 2. 본인의 퀘스트인지 확인
            if (!userId.equals(userQuest.getUserId())) {
                throw new IllegalArgumentException("본인의 퀘스트만 수정할 수 있습니다.");
            }

            // 3. 일정 정보 수정
            userQuest.setScheduledDate(scheduledDate);
            userQuest.setTargetTime(targetTime);
            userQuest.setMemo(memo);
            return userQuest;
        });
    }
    
    //퀘스트 완료 처리 및 저장
    public QuestLog completeQuest(Long userQuestId, Long userId, String proofImageUrl, String proofText) {
        return tx.inTransaction(em -> {

            // 1. 사용자 퀘스트 조회
            UserQuest userQuest = userQuestRepository.findById(em, userQuestId);
            if (userQuest == null) {
                return null;
            }

            // 2. 해당 사용자의 퀘스트인지 확인
            if (!userId.equals(userQuest.getUserId())) {
                throw new IllegalArgumentException("본인의 퀘스트만 완료할 수 있습니다.");
            }

            // 3. 이미 완료된 퀘스트인지 확인
            if ("COMPLETED".equals(userQuest.getStatus())) {
                throw new IllegalStateException("이미 완료된 퀘스트입니다.");
            }

            // 4. 인증 방식에 따른 검증
            String authType = userQuest.getAuthType();
            if ("PHOTO".equals(authType)) {
                if (proofImageUrl == null || proofImageUrl.isBlank()) {
                    throw new IllegalArgumentException("사진 인증이 필요합니다.");
                }
            } else if ("TEXT".equals(authType)) {
                if (proofText == null || proofText.isBlank()) {
                    throw new IllegalArgumentException("텍스트 인증이 필요합니다.");
                }
            } else if ("LOCATION".equals(authType)) {
                // TODO places 테이블 연동 후
                // 퀘스트 장소 좌표와 현재 GPS 좌표의 거리를 계산하여
                // userQuest.getAuthRadiusM() 이내인지 확인해야 함.
                throw new IllegalStateException("위치 인증은 장소 좌표 연동 후 사용할 수 있습니다.");
            } else if (!"NONE".equals(authType)) {
                throw new IllegalArgumentException("지원하지 않는 인증 방식입니다.");
            }

            // 5. 완료 시간 생성
            LocalDateTime completedAt = LocalDateTime.now();

            // 6. UserQuest 완료 처리
            userQuest.setStatus("COMPLETED");
            userQuest.setCompletedAt(completedAt);

            // 7. 원본 퀘스트 완료 횟수 증가
            Long originTemplateId = userQuest.getOriginTemplateId();
            if (originTemplateId != null) {
                QuestTemplate template = questRepository.findById(em, originTemplateId);
                if (template != null) {
                    template.setCompletionCount(template.getCompletionCount() + 1);
                }
            }

            // 8. 완료 로그 생성
            QuestLog questLog = new QuestLog(
                    null,
                    userId,
                    userQuest.getId(),
                    completedAt,
                    userQuest.getAuthType(),
                    null,
                    proofImageUrl,
                    proofText,
                    userQuest.getRewardXp()
            );

            // 9. QuestLog 저장
            return questLogRepository.save(em, questLog);
        });
    }
    
    // 특정 사용자의 퀘스트 완료 기록 조회
    public List<QuestLog> getQuestLogsByUserId(Long userId) {
        return tx.read(em ->
            questLogRepository.findByUserId(em, userId)
        );
    }

    // 퀘스트 북마크 저장
    public UserQuestBookmark addBookmark(Long userId, Long questTemplateId, String folderName) {

        // DB 설계의 기본 폴더명과 동일하게 처리
        if (folderName == null || folderName.isBlank()) {
            folderName = "기본";
        }

        // folder_name은 DB에서 VARCHAR(50)
        if (folderName.length() > 50) {
            throw new IllegalArgumentException("폴더 이름은 50자 이하로 입력해야 합니다.");
        }
        final String finalFolderName = folderName;
        return tx.inTransaction(em -> {
        	
            // 존재하고 공개된 퀘스트인지 확인
            QuestTemplate quest = questRepository.findById(em, questTemplateId);
            if (quest == null || !"PUBLIC".equals(quest.getVisibility())) {
                return null;
            }

            // 이미 저장되어 있으면 중복 저장하지 않음
            UserQuestBookmark existingBookmark = bookmarkRepository.findByUserIdAndQuestTemplateId(
                            em,
                            userId,
                            questTemplateId
                    );
            if (existingBookmark != null) {
                return existingBookmark;
            }
            UserQuestBookmark bookmark = new UserQuestBookmark(
                            userId,
                            questTemplateId,
                            finalFolderName,
                            LocalDateTime.now()
                    );
            return bookmarkRepository.save(em, bookmark);
        });
    }

    // 사용자가 저장한 퀘스트 북마크 조회
    public List<UserQuestBookmark> getBookmarksByUserId(Long userId) {
        return tx.read(em ->
            bookmarkRepository.findByUserId(em, userId)
        );
    }

    // 퀘스트 북마크 여부 확인
    public boolean isBookmarked(Long userId, Long questTemplateId) {
        return tx.read(em ->
            bookmarkRepository.findByUserIdAndQuestTemplateId(
                    em,
                    userId,
                    questTemplateId
            ) != null
        );
    }

    // 퀘스트 북마크 삭제
    public boolean removeBookmark(Long userId, Long questTemplateId) {
        return tx.inTransaction(em ->
            bookmarkRepository.delete(
                    em,
                    userId,
                    questTemplateId
            )
        );
    }

    // 사용자가 북마크한 퀘스트 목록 조회
    public List<QuestTemplate> getBookmarkedQuests(Long userId) {
        return tx.read(em -> {
            List<UserQuestBookmark> bookmarks = bookmarkRepository.findByUserId(em, userId);
            List<Long> questTemplateIds = bookmarks.stream()
                            .map(UserQuestBookmark::getQuestTemplateId)
                            .toList();
            return questRepository.findByIds(
                    em,
                    questTemplateIds
            );
        });
    }

    // 특정 폴더에 북마크한 퀘스트 목록 조회
    public List<QuestTemplate> getBookmarkedQuestsByFolder(Long userId, String folderName) {
        return tx.read(em -> {
            List<UserQuestBookmark> bookmarks = bookmarkRepository.findByUserIdAndFolderName(
                            em,
                            userId,
                            folderName
                    );
            List<Long> questTemplateIds =
                    bookmarks.stream()
                            .map(UserQuestBookmark::getQuestTemplateId)
                            .toList();
            return questRepository.findByIds(
                    em,
                    questTemplateIds
            );
        });
    }

    // 북마크를 다른 폴더로 이동
    public boolean moveBookmarkToFolder(Long userId, Long questTemplateId, String folderName) {
        if (folderName == null || folderName.isBlank()) {
            folderName = "기본";
        }

        // folder_name은 DB에서 VARCHAR(50)
        if (folderName.length() > 50) {
            throw new IllegalArgumentException("폴더 이름은 50자 이하로 입력해야 합니다.");
        }
        final String finalFolderName = folderName;
        return tx.inTransaction(em ->
            bookmarkRepository.moveToFolder(
                    em,
                    userId,
                    questTemplateId,
                    finalFolderName
            )
        );
    }
}
