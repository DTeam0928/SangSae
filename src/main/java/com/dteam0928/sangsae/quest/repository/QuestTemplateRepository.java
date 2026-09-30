package com.dteam0928.sangsae.quest.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestTemplate;

public class QuestTemplateRepository {
	
	private final List<QuestTemplate> quests = new ArrayList<>();
	
	public QuestTemplateRepository() {
		quests.add(new QuestTemplate(
				1L,
				1L,
				1L,
				"광안대교 야경 보면서 산책하기",
				"광안리에서 야경을 감상하며 산책해보세요.",
				"WALK",
				30,
				100,
				"LOCATION",
				"PUBLIC",
				null,
				24,
				LocalDateTime.now()
				));
		
		quests.add(new QuestTemplate(
                2L,
                1L,
                2L,
                "새로운 카페 방문하기",
                "가보지 않았던 카페를 방문해보세요.",
                "CAFE",
                60,
                150,
                "LOCATION",
                "PUBLIC",
                null,
                12,
                LocalDateTime.now()
        ));

        quests.add(new QuestTemplate(
                3L,
                1L,
                3L,
                "공원 풍경 사진 찍기",
                "주변 공원에서 마음에 드는 풍경을 촬영해보세요.",
                "PHOTO",
                30,
                120,
                "PHOTO",
                "PUBLIC",
                null,
                8,
                LocalDateTime.now()
        ));
	}
	
	public List<QuestTemplate> findAll() {
		return quests;
	}
	
	public QuestTemplate findById(Long id) {
	    return quests.stream()
	            .filter(quest -> quest.getId().equals(id))
	            .findFirst()
	            .orElse(null);
	}
	
	public List<QuestTemplate> findByIds(List<Long> ids) {
		return quests.stream()
				.filter(quest -> ids.contains(quest.getId()))
				.toList();
	}
}
