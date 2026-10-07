package com.dteam0928.sangsae.quest.dto;

import com.dteam0928.sangsae.user.exp.ExpRewardResult;

// 퀘스트 완료 처리 결과 (완료 로그 및 경험치/레벨업 보상 결과 결합)
public record QuestCompleteResult(
        QuestLog questLog,
        ExpRewardResult expRewardResult
) {}
