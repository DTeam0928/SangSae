package com.dteam0928.sangsae.quest.repository;

import java.util.ArrayList;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestLog;

public class QuestLogRepository {
    private final List<QuestLog> questLogs = new ArrayList<>();
    private long sequence = 1L;
    
    public QuestLog save(QuestLog questLog) {
        questLog.setId(sequence++);
        questLogs.add(questLog);
        return questLog;
    }
    
    public List<QuestLog> findAll() {
        return questLogs;
    }
}
