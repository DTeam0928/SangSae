package com.dteam0928.sangsae.routine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "routine_template_tasks")
public class RoutineTemplateTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "routine_template_id", nullable = false)
    private Long routineTemplateId;

    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "content", nullable = false, length = 150)
    private String content;

    @Column(name = "default_time", nullable = false)
    private LocalTime defaultTime = LocalTime.of(7, 0);

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "reward_xp", nullable = false)
    private int rewardXp = 100;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 1;

    @Column(name = "allowed_radius_meters", nullable = false)
    private int allowedRadiusMeters = 50;

    public RoutineTemplateTask() {
    }

    public RoutineTemplateTask(Long routineTemplateId, Long placeId, String content, LocalTime defaultTime, String category, int rewardXp, int sortOrder, int allowedRadiusMeters) {
        this.routineTemplateId = routineTemplateId;
        this.placeId = placeId;
        this.content = content;
        this.defaultTime = defaultTime != null ? defaultTime : LocalTime.of(7, 0);
        this.category = category;
        this.rewardXp = rewardXp > 0 ? rewardXp : 100;
        this.sortOrder = sortOrder;
        this.allowedRadiusMeters = allowedRadiusMeters > 0 ? allowedRadiusMeters : 50;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoutineTemplateId() {
        return routineTemplateId;
    }

    public void setRoutineTemplateId(Long routineTemplateId) {
        this.routineTemplateId = routineTemplateId;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalTime getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(LocalTime defaultTime) {
        this.defaultTime = defaultTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public void setRewardXp(int rewardXp) {
        this.rewardXp = rewardXp;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getAllowedRadiusMeters() {
        return allowedRadiusMeters;
    }

    public void setAllowedRadiusMeters(int allowedRadiusMeters) {
        this.allowedRadiusMeters = allowedRadiusMeters;
    }
}
