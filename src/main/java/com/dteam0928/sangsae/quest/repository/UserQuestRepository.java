package com.dteam0928.sangsae.quest.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuest;

public class UserQuestRepository {
    private final List<UserQuest> userQuests = new ArrayList<>();
    private long sequence = 1L;

    public UserQuest save(UserQuest userQuest) {
        userQuest.setId(sequence++);
        userQuests.add(userQuest);
        return userQuest;
    }

    public List<UserQuest> findAll() {
        return userQuests;
    }
    //조건별 사용자 퀘스트 조회
    public List<UserQuest> findByUserIdAndTripIdAndScheduledDate(
            Long userId,
            Long tripId,
            LocalDate scheduledDate) {

        return userQuests.stream()
                .filter(quest -> quest.getUserId().equals(userId))
                .filter(quest -> quest.getTripId().equals(tripId))
                .filter(quest -> quest.getScheduledDate().equals(scheduledDate))
                .toList();
    }
    
    public UserQuest findById(Long id) {
        return userQuests.stream()
                .filter(quest -> quest.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
