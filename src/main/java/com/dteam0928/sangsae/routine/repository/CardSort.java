package com.dteam0928.sangsae.routine.repository;

/** 루틴 카드 목록 정렬 */
public enum CardSort {
    /** 대표 장소가 가까운 순 (장소 없는 루틴은 뒤로) */
    DISTANCE,
    /** 누적 추가 인원 많은 순 */
    POPULAR,
    /** 최신 등록 순 */
    LATEST,
    /** 최근 N일 추가 수 많은 순 ("지금 이 근처에서 뜨는") */
    TRENDING,
    /** 내 관심사 적합도 높은 순, 같으면 인기순 ("당신에게 추천") */
    RECOMMEND
}
