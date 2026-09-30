package com.dteam0928.sangsae.quest.repository;

import java.util.ArrayList;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuestBookmark;

public class UserQuestBookmarkRepository {

    private final List<UserQuestBookmark> bookmarks = new ArrayList<>();
    
    //북마크 저장
    public UserQuestBookmark save(UserQuestBookmark bookmark) {
        bookmarks.add(bookmark);
        return bookmark;
    }
    
    //사용자의 저장된 북마크 조회
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

    //북마크 삭제
    public boolean delete(Long userId, Long questTemplateId) {
        return bookmarks.removeIf(bookmark ->
                bookmark.getUserId().equals(userId)
                && bookmark.getQuestTemplateId().equals(questTemplateId));
    }
    
    //사용자의 특정 폴더에 저장된 북마크 조회
    public List<UserQuestBookmark> findByUserIdAndFolderName(Long userId, String folderName) {
    	return bookmarks.stream()
    			.filter(bookmark -> bookmark.getUserId().equals(userId))
    			.filter(bookmark -> bookmark.getFolderName().equals(folderName))
    			.toList();
    }
    
    //북마크 폴더 이동
    public boolean moveToFolder(Long userId, Long questTemplateId, String folderName) {
    	UserQuestBookmark bookmark = findByUserIdAndQuestTemplateId(userId, questTemplateId);
    	if(bookmark==null) return false;
    	bookmark.setFolderName(folderName);
    	return true;
    }
}
