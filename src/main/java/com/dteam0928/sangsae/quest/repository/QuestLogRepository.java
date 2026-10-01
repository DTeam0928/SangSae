package com.dteam0928.sangsae.quest.repository;

import java.util.ArrayList;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestLog;

public class QuestLogRepository {
    private final List<QuestLog> questLogs = new ArrayList<>();
    private long sequence = 1L;
    
    //완료한 퀘스트 QuestLog에 저장
    public QuestLog save(QuestLog questLog) {
        questLog.setId(sequence++);
        questLogs.add(questLog);
        return questLog;
    }
    
    //전체 QuestLog 조회
    public List<QuestLog> findAll() {
        return questLogs;
    }
    
    //특정 사용자의 QuestLog 조회
    public List<QuestLog> findByUserId(Long userId) {
        return questLogs.stream()
                .filter(log -> log.getUserId().equals(userId))
                .toList();
    }
}
