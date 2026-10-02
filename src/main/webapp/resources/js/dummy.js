/*
 * JSON 구간은 JSP에서도 읽으므로 반드시 JSON 문법을 유지합니다.
 * 주석, 함수, trailing comma는 JSON 구간 내부에 넣지 않습니다.
 */
const mockData = /* HOME_MODEL_START */
        {
            "nickname": "게스트",
            "tripList": [
                {
                    "id": 1,
                    "title": "부산 여행",
                    "startDate": "2026-10-10",
                    "endDate": "2026-10-12"
                },
                {
                    "id": 2,
                    "title": "제주 힐링 코스",
                    "startDate": "2026-11-01",
                    "endDate": "2026-11-05"
                }
            ],
            "routineList": [
                {
                    "id": 1,
                    "title": "갓생 살기 루틴",
                    "startDate": "2026-09-01",
                    "endDate": "2026-12-31",
                    "status": "active"
                },
                {
                    "id": 2,
                    "title": "저녁 스트레칭",
                    "startDate": "2026-10-01",
                    "endDate": "2026-11-01",
                    "status": "active"
                }
            ],
            "stats": {
                "questCompleted": 8,
                "questRemaining": 4,
                "routineStreak": 3,
                "tasksCompleted": 15,
                "tasksRemaining": 2
            },
            "userXpInfo": {
                "totalXp": 1240,
                "title": "골목대장 탐험가",
                "progressPercent": 62,
                "remainingXp": 260
            },
            "recommendedQuest": {
                "title": "광안리에서 일몰 사진 남기기",
                "description": "해 질 무렵 바다를 바라보며 마음에 드는 사진 한 장을 남겨보세요.",
                "reward": "+120 XP"
            },
            "communityFeed": {
                "title": "광안대교 야경 보면서 산책하기 완료! ✨",
                "content": "\"밤바다가 너무 예쁘네요. 다들 오늘 산책 어떠신가요?\" - 여행러버",
                "timeAgo": "10분 전"
            },
            "ui": {
                "pageTitle": "일상을 새롭게, 상새 - 종합 대시보드",
                "dashboardSuffix": "님의 종합 대시보드",
                "xpTitle": "✨ 나의 활동 레벨",
                "totalXpLabel": "누적 경험치",
                "xpUnit": "XP",
                "nextTitleLabel": "다음 칭호까지",
                "remainingXpSuffix": "XP 남음",
                "editLabel": "수정",
                "deleteLabel": "삭제",
                "dateSeparator": " - ",
                "extraTitle": "🌟 오늘의 추천 퀘스트 & 소식",
                "modalTitle": "{type} 삭제",
                "modalDescription": "\"{name}\" {type}을(를) 정말 삭제하시겠습니까?\n관련 데이터가 모두 제거됩니다.",
                "cancelLabel": "취소",
                "confirmLabel": "삭제하기",
                "managementCards": [
                    {
                        "key": "trip",
                        "listKey": "tripList",
                        "title": "✈️ 내 여행 관리",
                        "typeLabel": "여행",
                        "emptyText": "등록된 여행이 없습니다.\n첫 여행을 만들어보세요!",
                        "filteredEmptyText": "선택한 조건에 해당하는 여행이 없습니다.",
                        "addLabel": "여행 및 퀘스트 추가하기",
                        "addPath": "/views/quest/quest_main.jsp",
                        "editPath": "/views/quest/quest_main.jsp",
                        "editFragment": "management",
                        "filters": [
                            { "value": "all", "label": "전체 여행" },
                            { "value": "active", "label": "진행 중 여행" },
                            { "value": "upcoming", "label": "예정된 여행" },
                            { "value": "completed", "label": "완료된 여행" }
                        ]
                    },
                    {
                        "key": "routine",
                        "listKey": "routineList",
                        "title": "🔄 내 루틴 관리",
                        "typeLabel": "루틴",
                        "emptyText": "등록된 루틴이 없습니다.\n새 루틴을 추가해보세요!",
                        "filteredEmptyText": "선택한 조건에 해당하는 루틴이 없습니다.",
                        "addLabel": "루틴 추가하기",
                        "addPath": "/views/routine/routine_main.jsp",
                        "editPath": "/views/routine/routine_main.jsp",
                        "editFragment": "manage",
                        "filters": [
                            { "value": "all", "label": "전체 루틴" },
                            { "value": "active", "label": "활성 루틴" },
                            { "value": "inactive", "label": "비활성 루틴" }
                        ]
                    }
                ],
                "statsCards": [
                    {
                        "title": "🎯 내 퀘스트 달성도",
                        "items": [
                            {
                                "key": "questCompleted",
                                "label": "달성한 퀘스트",
                                "unit": "개",
                                "accent": false
                            },
                            {
                                "key": "questRemaining",
                                "label": "남은 퀘스트",
                                "unit": "개",
                                "accent": true
                            }
                        ]
                    },
                    {
                        "title": "🔥 내 루틴 달성도",
                        "items": [
                            {
                                "key": "routineStreak",
                                "label": "연속 달성",
                                "unit": "일",
                                "accent": true
                            },
                            {
                                "key": "tasksCompleted",
                                "label": "달성한 할 일",
                                "unit": "개",
                                "accent": false
                            },
                            {
                                "key": "tasksRemaining",
                                "label": "남은 할 일",
                                "unit": "개",
                                "accent": false
                            }
                        ]
                    }
                ],
                "featureCards": [
                    {
                        "key": "quest",
                        "dataKey": "recommendedQuest",
                        "badgeLabel": "추천 챌린지",
                        "metaKey": "reward",
                        "bodyKey": "description",
                        "emptyText": "추천 퀘스트가 없습니다.",
                        "linkLabel": "퀘스트 탐색하러 가기",
                        "path": "/views/quest/quest_main.jsp"
                    },
                    {
                        "key": "community",
                        "dataKey": "communityFeed",
                        "badgeLabel": "커뮤니티 피드",
                        "metaKey": "timeAgo",
                        "bodyKey": "content",
                        "emptyText": "커뮤니티 소식이 없습니다.",
                        "linkLabel": "커뮤니티 구경하기",
                        "path": "/views/community/community_main.jsp"
                    }
                ]
            }
        }
    /* HOME_MODEL_END */;

window.mockData = mockData;
