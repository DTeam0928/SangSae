package com.dteam0928.sangsae.user.dao;

import com.dteam0928.sangsae.user.model.InterestCategory;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;

/** interest_categories / user_interests 접근 */
public interface InterestDao {

    List<InterestCategory> findAllCategories(EntityManager em);

    /** 주어진 id 중 실제 존재하는 카테고리 수 */
    long countCategories(EntityManager em, Collection<Short> categoryIds);

    List<Short> findCategoryIdsByUserId(EntityManager em, long userId);

    /** 사용자의 관심사를 통째로 교체 */
    void replaceUserInterests(EntityManager em, long userId, Collection<Short> categoryIds);
}
