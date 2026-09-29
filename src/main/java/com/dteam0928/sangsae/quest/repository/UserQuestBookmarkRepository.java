package com.dteam0928.sangsae.quest.repository;

import java.util.ArrayList;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuestBookmark;

public class UserQuestBookmarkRepository {

    private final List<UserQuestBookmark> bookmarks = new ArrayList<>();

    public UserQuestBookmark save(UserQuestBookmark bookmark) {
        bookmarks.add(bookmark);
        return bookmark;
    }

    public List<UserQuestBookmark> findByUserId(Long userId) {
        return bookmarks.stream()
                .filter(bookmark -> bookmark.getUserId().equals(userId))
                .toList();
    }

    public UserQuestBookmark findByUserIdAndQuestTemplateId(Long userId, Long questTemplateId) {
        return bookmarks.stream()
                .filter(bookmark -> bookmark.getUserId().equals(userId))
                .filter(bookmark -> bookmark.getQuestTemplateId()
                        .equals(questTemplateId))
                .findFirst()
                .orElse(null);
    }

    public boolean delete(Long userId, Long questTemplateId) {
        return bookmarks.removeIf(bookmark ->
                bookmark.getUserId().equals(userId)
                && bookmark.getQuestTemplateId().equals(questTemplateId));
    }
}
