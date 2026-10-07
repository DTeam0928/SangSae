package com.dteam0928.sangsae.routine.dto;

/**
 * 탐색 기준 위치.
 *
 * @param source GPS(요청 좌표) / PROFILE(O-03 에서 저장한 위치) / DEFAULT(설정의 기본 위치, 서울 시청)
 */
public record LocationInfo(double latitude, double longitude, String source) {
}
