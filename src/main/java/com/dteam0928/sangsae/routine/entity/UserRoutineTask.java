package com.dteam0928.sangsae.routine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_routine_tasks")
public class UserRoutineTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_routine_id", nullable = false)
    private Long userRoutineId;

    @Column(name = "origin_template_task_id")
    private Long originTemplateTaskId;

    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "content", nullable = false, length = 150)
    private String content;

    @Column(name = "target_time", nullable = false)
    private LocalTime targetTime = LocalTime.of(7, 0);

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "repeat_type", nullable = false, length = 20)
    private String repeatType = "DAILY";

    @Column(name = "alert_time")
    private LocalTime alertTime;

    @Column(name = "reward_xp", nullable = false)
    private int rewardXp = 100;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "allowed_radius_meters", nullable = false)
    private int allowedRadiusMeters = 50;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserRoutineTask() {
    }

    public UserRoutineTask(Long userRoutineId, Long originTemplateTaskId, Long placeId, String content, LocalTime targetTime, String category, String repeatType, int rewardXp, int allowedRadiusMeters) {
        this.userRoutineId = userRoutineId;
        this.originTemplateTaskId = originTemplateTaskId;
        this.placeId = placeId;
        this.content = content;
        this.targetTime = targetTime != null ? targetTime : LocalTime.of(7, 0);
        this.category = category;
        this.repeatType = repeatType != null ? repeatType : "DAILY";
        this.rewardXp = rewardXp > 0 ? rewardXp : 100;
        this.allowedRadiusMeters = allowedRadiusMeters > 0 ? allowedRadiusMeters : 50;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserRoutineId() {
        return userRoutineId;
    }

    public void setUserRoutineId(Long userRoutineId) {
        this.userRoutineId = userRoutineId;
    }

    public Long getOriginTemplateTaskId() {
        return originTemplateTaskId;
    }

    public void setOriginTemplateTaskId(Long originTemplateTaskId) {
        this.originTemplateTaskId = originTemplateTaskId;
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

    public LocalTime getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(LocalTime targetTime) {
        this.targetTime = targetTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public LocalTime getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(LocalTime alertTime) {
        this.alertTime = alertTime;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public void setRewardXp(int rewardXp) {
        this.rewardXp = rewardXp;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getAllowedRadiusMeters() {
        return allowedRadiusMeters;
    }

    public void setAllowedRadiusMeters(int allowedRadiusMeters) {
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
