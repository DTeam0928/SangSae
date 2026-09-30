package com.dteam0928.sangsae.common.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import java.sql.SQLException;

/**
 * JPA 트랜잭션 경계. Service 에서 사용한다. (팀 공용)
 *
 * <pre>
 * return tx.inTransaction(em -> {
 *     userDao.insert(em, user);
 *     profileDao.insert(em, profile);
 *     return user.getId();
 * });
 * </pre>
 *
 * <ul>
 *   <li>작업 단위마다 EntityManager 를 새로 열고 끝나면 닫는다 (요청 간 공유하지 않음)</li>
 *   <li>람다 안에서 예외가 나면 전부 롤백</li>
 *   <li>UNIQUE 위반은 {@link DuplicateKeyException}, 그 외 DB 오류는 {@link DataAccessException} 으로 바뀐다
 *       (Hibernate 전용 예외 클래스에 의존하지 않도록 원인 SQLException 을 직접 확인)</li>
 * </ul>
 */
public class TransactionTemplate {

    private final EntityManagerFactory emf;

    public TransactionTemplate(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** 쓰기 작업: 하나의 트랜잭션으로 묶는다 */
    public <T> T inTransaction(EmFunction<T> work) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (RuntimeException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw translate(e);
        } finally {
            em.close();
        }
    }

    /** 반환값이 없는 쓰기 작업 */
    public void run(EmConsumer work) {
        inTransaction(em -> {
            work.accept(em);
            return null;
        });
    }

    /** 읽기 전용 작업 */
    public <T> T read(EmFunction<T> work) {
        EntityManager em = emf.createEntityManager();
        try {
            return work.apply(em);
        } catch (RuntimeException e) {
            throw translate(e);
        } finally {
            em.close();
        }
    }

    /** JPA/Hibernate 예외 → 공통 예외. 비즈니스 예외는 그대로 통과 */
    static RuntimeException translate(RuntimeException e) {
        if (e instanceof DataAccessException) return e;
        SQLException sql = findSqlException(e);
        if (sql != null && sql.getErrorCode() == DuplicateKeyException.MYSQL_DUPLICATE_ENTRY) {
            return new DuplicateKeyException(sql);
        }
        if (e instanceof PersistenceException || sql != null) {
            return new DataAccessException("DB 작업 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
        return e;
    }

    private static SQLException findSqlException(Throwable t) {
        for (Throwable c = t; c != null && c.getCause() != c; c = c.getCause()) {
            if (c instanceof SQLException s) {
                // 드라이버가 감싼 예외(BatchUpdateException 등) 안쪽까지 확인
                SQLException next = s;
                while (next != null) {
                    if (next.getErrorCode() == DuplicateKeyException.MYSQL_DUPLICATE_ENTRY) return next;
                    next = next.getNextException();
                }
                return s;
            }
        }
        return null;
    }
}
