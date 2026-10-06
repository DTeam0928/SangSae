package com.dteam0928.sangsae.user.dao.jpa;

import com.dteam0928.sangsae.user.dao.InterestDao;
import com.dteam0928.sangsae.user.model.InterestCategory;
import com.dteam0928.sangsae.user.model.UserInterest;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;

public class JpaInterestDao implements InterestDao {

    @Override
    public List<InterestCategory> findAllCategories(EntityManager em) {
        return em.createQuery("SELECT c FROM InterestCategory c ORDER BY c.sortOrder, c.id", InterestCategory.class)
                .getResultList();
    }

    @Override
    public long countCategories(EntityManager em, Collection<Short> categoryIds) {
        if (categoryIds.isEmpty()) return 0;
        return em.createQuery("SELECT COUNT(c) FROM InterestCategory c WHERE c.id IN :ids", Long.class)
                .setParameter("ids", categoryIds)
                .getSingleResult();
    }

    @Override
    public List<Short> findCategoryIdsByUserId(EntityManager em, long userId) {
        return em.createQuery(
                        "SELECT ui.categoryId FROM UserInterest ui WHERE ui.userId = :userId ORDER BY ui.categoryId",
                        Short.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public void replaceUserInterests(EntityManager em, long userId, Collection<Short> categoryIds) {
        em.createQuery("DELETE FROM UserInterest ui WHERE ui.userId = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
        for (Short categoryId : categoryIds) {
            em.persist(new UserInterest(userId, categoryId));
        }
        em.flush();
    }
}
