package com.dteam0928.sangsae.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H-00 홈 종합 대시보드 렌더링용 모델.
 * home.jsp 의 JSTL/EL 표현식 바인딩에 맞추어 구성.
 */
public class HomeModel {

    private String nickname;
    private DailyAggregateDto dailyAggregate;
    private UiConfig ui;
    private List<ManagementItem> trips = new ArrayList<>();
    private List<ManagementItem> routines = new ArrayList<>();
    private Map<String, Object> stats = new HashMap<>();
    private FeatureItem recommendedQuest;
    private FeatureItem communityFeed;

    public HomeModel() {
        this.ui = new UiConfig();
        this.dailyAggregate = new DailyAggregateDto();
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public DailyAggregateDto getDailyAggregate() { return dailyAggregate; }
    public void setDailyAggregate(DailyAggregateDto dailyAggregate) { this.dailyAggregate = dailyAggregate; }

    public UiConfig getUi() { return ui; }
    public void setUi(UiConfig ui) { this.ui = ui; }

    public List<ManagementItem> getTrips() { return trips; }
    public void setTrips(List<ManagementItem> trips) { this.trips = trips; }

    public List<ManagementItem> getRoutines() { return routines; }
    public void setRoutines(List<ManagementItem> routines) { this.routines = routines; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public FeatureItem getRecommendedQuest() { return recommendedQuest; }
    public void setRecommendedQuest(FeatureItem recommendedQuest) { this.recommendedQuest = recommendedQuest; }

    public FeatureItem getCommunityFeed() { return communityFeed; }
    public void setCommunityFeed(FeatureItem communityFeed) { this.communityFeed = communityFeed; }

    public static class DailyAggregateDto {
        private int routineTotalCount = 0;
        private int routineSuccessCount = 0;
        private int questSuccessCount = 0;
        private int earnedXp = 0;
        private int currentStreak = 1;

        public int getRoutineTotalCount() { return routineTotalCount; }
        public void setRoutineTotalCount(int routineTotalCount) { this.routineTotalCount = routineTotalCount; }

        public int getRoutineSuccessCount() { return routineSuccessCount; }
        public void setRoutineSuccessCount(int routineSuccessCount) { this.routineSuccessCount = routineSuccessCount; }

        public int getQuestSuccessCount() { return questSuccessCount; }
        public void setQuestSuccessCount(int questSuccessCount) { this.questSuccessCount = questSuccessCount; }

        public int getEarnedXp() { return earnedXp; }
        public void setEarnedXp(int earnedXp) { this.earnedXp = earnedXp; }

        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    }

    public static class ManagementItem {
        private Long id;
        private String title;
        private String startDate;
        private String endDate;
        private String status;

        public ManagementItem(Long id, String title, String startDate, String endDate, String status) {
            this.id = id;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getStatus() { return status; }
    }

    public static class FeatureItem {
        private String title;
        private String category;
        private String description;

        public FeatureItem(String title, String category, String description) {
            this.title = title;
            this.category = category;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
    }

    public static class UiConfig {
        private String pageTitle = "상새 - 일상을 새롭게";
        private String dashboardSuffix = "님의 대시보드";
        private String xpTitle = "오늘의 달성도";
        private String xpUnit = "XP";
        private String dateSeparator = " ~ ";
        private String editLabel = "이동";
        private String editPath = "/quest";
        private String editFragment = "management";
        private String extraTitle = "오늘의 추천";

        private String modalTitle = "{title}을(를) 삭제하시겠습니까?";
        private String modalDescription = "삭제한 데이터는 복구할 수 없습니다.";
        private String cancelLabel = "취소";
        private String confirmLabel = "삭제";

        private List<ManagementCardConfig> managementCards = new ArrayList<>();
        private List<StatsCardConfig> statsCards = new ArrayList<>();
        private List<FeatureCardConfig> featureCards = new ArrayList<>();

        public UiConfig() {
            initDefaults();
        }

        private void initDefaults() {
            // 1. 관리 카드 설정 (퀘스트 일정, 루틴 일정)
            List<FilterOption> tripFilters = List.of(
                    new FilterOption("all", "전체"),
                    new FilterOption("active", "진행중"),
                    new FilterOption("completed", "완료")
            );
            managementCards.add(new ManagementCardConfig(
                    "trip", "오늘의 퀘스트 일정", "trips", tripFilters,
                    "등록된 퀘스트가 없습니다.", "해당 조건의 퀘스트가 없습니다.",
                    "＋ 퀘스트 담으러 가기", "/quest", "/quest", "management"
            ));

            List<FilterOption> routineFilters = List.of(
                    new FilterOption("all", "전체"),
                    new FilterOption("active", "활성"),
                    new FilterOption("completed", "완료")
            );
            managementCards.add(new ManagementCardConfig(
                    "routine", "내 루틴 타임테이블", "routines", routineFilters,
                    "등록된 루틴이 없습니다.", "해당 조건의 루틴이 없습니다.",
                    "＋ 루틴 추가하기", "/routine", "/routine", "manage"
            ));

            // 2. 통계 카드 설정
            statsCards.add(new StatsCardConfig("오늘의 달성 현황", List.of(
                    new StatItemConfig("완료한 퀘스트", "questDone", "개", true),
                    new StatItemConfig("남은 퀘스트", "questRemaining", "개", false),
                    new StatItemConfig("완료한 루틴", "routineDone", "개", true),
                    new StatItemConfig("남은 루틴", "routineRemaining", "개", false)
            )));

            // 3. 피처 카드 (추천 퀘스트, 커뮤니티)
            featureCards.add(new FeatureCardConfig(
                    "quest", "오늘의 추천 퀘스트", "recommendedQuest",
                    "category", "description", "/quest", "퀘스트 도전하기", "추천 퀘스트가 없습니다."
            ));
            featureCards.add(new FeatureCardConfig(
                    "community", "상새 둘러보기", "communityFeed",
                    "category", "description", "/routine", "루틴 탐색하기", "함께 일상을 새롭게 시작해보세요!"
            ));
        }

        public String getPageTitle() { return pageTitle; }
        public String getDashboardSuffix() { return dashboardSuffix; }
        public String getXpTitle() { return xpTitle; }
        public String getXpUnit() { return xpUnit; }
        public String getDateSeparator() { return dateSeparator; }
        public String getEditLabel() { return editLabel; }
        public String getEditPath() { return editPath; }
        public String getEditFragment() { return editFragment; }
        public String getExtraTitle() { return extraTitle; }
        public String getModalTitle() { return modalTitle; }
        public String getModalDescription() { return modalDescription; }
        public String getCancelLabel() { return cancelLabel; }
        public String getConfirmLabel() { return confirmLabel; }
        public List<ManagementCardConfig> getManagementCards() { return managementCards; }
        public List<StatsCardConfig> getStatsCards() { return statsCards; }
        public List<FeatureCardConfig> getFeatureCards() { return featureCards; }

        public void setXpTitle(String xpTitle) {
            this.xpTitle = xpTitle;
        }
    }

    public static class ManagementCardConfig {
        private String key;
        private String title;
        private String listKey;
        private List<FilterOption> filters;
        private String emptyText;
        private String filteredEmptyText;
        private String addLabel;
        private String addPath;
        private String editPath;
        private String editFragment;

        public ManagementCardConfig(String key, String title, String listKey, List<FilterOption> filters,
                                    String emptyText, String filteredEmptyText, String addLabel, String addPath,
                                    String editPath, String editFragment) {
            this.key = key;
            this.title = title;
            this.listKey = listKey;
            this.filters = filters;
            this.emptyText = emptyText;
            this.filteredEmptyText = filteredEmptyText;
            this.addLabel = addLabel;
            this.addPath = addPath;
            this.editPath = editPath;
            this.editFragment = editFragment;
        }

        public String getKey() { return key; }
        public String getTitle() { return title; }
        public String getListKey() { return listKey; }
        public List<FilterOption> getFilters() { return filters; }
        public String getEmptyText() { return emptyText; }
        public String getFilteredEmptyText() { return filteredEmptyText; }
        public String getAddLabel() { return addLabel; }
        public String getAddPath() { return addPath; }
        public String getEditPath() { return editPath; }
        public String getEditFragment() { return editFragment; }
    }

    public static class FilterOption {
        private String value;
        private String label;

        public FilterOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    public static class StatsCardConfig {
        private String title;
        private List<StatItemConfig> items;

        public StatsCardConfig(String title, List<StatItemConfig> items) {
            this.title = title;
            this.items = items;
        }

        public String getTitle() { return title; }
        public List<StatItemConfig> getItems() { return items; }
    }

    public static class StatItemConfig {
        private String label;
        private String key;
        private String unit;
        private boolean accent;

        public StatItemConfig(String label, String key, String unit, boolean accent) {
            this.label = label;
            this.key = key;
            this.unit = unit;
            this.accent = accent;
        }

        public String getLabel() { return label; }
        public String getKey() { return key; }
        public String getUnit() { return unit; }
        public boolean isAccent() { return accent; }
    }

    public static class FeatureCardConfig {
        private String key;
        private String badgeLabel;
        private String dataKey;
        private String metaKey;
        private String bodyKey;
        private String path;
        private String linkLabel;
        private String emptyText;

        public FeatureCardConfig(String key, String badgeLabel, String dataKey, String metaKey,
                                 String bodyKey, String path, String linkLabel, String emptyText) {
            this.key = key;
            this.badgeLabel = badgeLabel;
            this.dataKey = dataKey;
            this.metaKey = metaKey;
            this.bodyKey = bodyKey;
            this.path = path;
            this.linkLabel = linkLabel;
            this.emptyText = emptyText;
        }

        public String getKey() { return key; }
        public String getBadgeLabel() { return badgeLabel; }
        public String getDataKey() { return dataKey; }
        public String getMetaKey() { return metaKey; }
        public String getBodyKey() { return bodyKey; }
        public String getPath() { return path; }
        public String getLinkLabel() { return linkLabel; }
        public String getEmptyText() { return emptyText; }
    }
}
