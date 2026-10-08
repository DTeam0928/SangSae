package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/** 검색 결과 페이지 (page 는 1부터) */
public record PageResult<T>(List<T> items, int page, int size, boolean hasNext) {
}
