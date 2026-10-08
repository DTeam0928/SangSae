package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.routine.repository.RoutineExploreRepository;

/**
 * 루틴 찜(♡).
 * "내 루틴에 추가"는 RoutineService.adoptRoutine (POST /user-routines/adopt) 를 사용한다.
 */
public class RoutineBookmarkService {

    private final TransactionTemplate tx;
    private final RoutineExploreRepository routineRepository;

    public RoutineBookmarkService(TransactionTemplate tx, RoutineExploreRepository routineRepository) {
        this.tx = tx;
        this.routineRepository = routineRepository;
    }

    /** 찜. 이미 찜한 상태면 그대로 둔다 */
    public void bookmark(long userId, long templateId) {
        tx.run(em -> {
            if (!routineRepository.existsPublicTemplate(em, templateId)) {
                throw new BusinessException(ErrorCode.ROUTINE_NOT_FOUND);
            }
            routineRepository.addBookmark(em, userId, templateId);
        });
    }

    /** 찜 해제. 찜하지 않은 상태여도 오류 없음 */
    public void unbookmark(long userId, long templateId) {
        tx.run(em -> routineRepository.removeBookmark(em, userId, templateId));
    }
}
