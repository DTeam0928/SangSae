package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/** 선택한 날짜의 타임테이블 (시간순). date = "yyyy-MM-dd" */
public record Timeline(long routineId, String date, List<MyTask> tasks, int doneCount) {
}
