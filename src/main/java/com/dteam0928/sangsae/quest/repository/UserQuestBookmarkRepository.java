package com.dteam0928.sangsae.quest.repository;

import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuestBookmark;

import jakarta.persistence.EntityManager;

public class UserQuestBookmarkRepository {

    // 북마크 저장
    public UserQuestBookmark save(
            EntityManager em,
            UserQuestBookmark bookmark) {

        em.persist(bookmark);
        em.flush();
        return bookmark;
    }

    // 사용자의 저장된 북마크 조회
    public List<UserQuestBookmark> findByUserId(
            EntityManager em,
            Long userId) {

        return em.createQuery(
                "SELECT b FROM UserQuestBookmark b "
                + "WHERE b.userId = :userId",
                UserQuestBookmark.class
        )
        .setParameter("userId", userId)
        .getResultList();
    }

    // 특정 퀘스트 북마크 조회
    public UserQuestBookmark findByUserIdAndQuestTemplateId(
            EntityManager em,
            Long userId,
            Long questTemplateId) {

        return em.createQuery(
                "SELECT b FROM UserQuestBookmark b "
                + "WHERE b.userId = :userId "
                + "AND b.questTemplateId = :questTemplateId",
                UserQuestBookmark.class
        )
        .setParameter("userId", userId)
        .setParameter("questTemplateId", questTemplateId)
        .getResultList()
        .stream()
        .findFirst()
        .orElse(null);
    }

    // 북마크 삭제
    public boolean delete(
            EntityManager em,
            Long userId,
            Long questTemplateId) {

        UserQuestBookmark bookmark =
                findByUserIdAndQuestTemplateId(
                        em,
                        userId,
                        questTemplateId
                );

        if (bookmark == null) {
            return false;
        }

        em.remove(bookmark);
        return true;
    }

    // 사용자의 특정 폴더에 저장된 북마크 조회
    public List<UserQuestBookmark> findByUserIdAndFolderName(
            EntityManager em,
            Long userId,
            String folderName) {

        return em.createQuery(
                "SELECT b FROM UserQuestBookmark b "
                + "WHERE b.userId = :userId "
                + "AND b.folderName = :folderName",
                UserQuestBookmark.class
        )
        .setParameter("userId", userId)
        .setParameter("folderName", folderName)
        .getResultList();
    }

    // 북마크 폴더 이동
    public boolean moveToFolder(
            EntityManager em,
            Long userId,
            Long questTemplateId,
            String folderName) {

        UserQuestBookmark bookmark =
                findByUserIdAndQuestTemplateId(
                        em,
                        userId,
                        questTemplateId
                );

        if (bookmark == null) {
            return false;
        }

        bookmark.setFolderName(folderName);
        return true;
    }
}
