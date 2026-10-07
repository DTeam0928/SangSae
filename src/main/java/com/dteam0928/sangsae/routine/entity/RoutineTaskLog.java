package com.dteam0928.sangsae.routine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "routine_task_logs")
public class RoutineTaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_routine_task_id", nullable = false)
    private Long userRoutineTaskId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "completed_at", nullable = false)
    private LocalTime completedAt;

    @Column(name = "authenticated_location", columnDefinition = "POINT")
    private Point authenticatedLocation;

    @Column(name = "earned_xp", nullable = false)
    private int earnedXp = 100;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess = true;

    public RoutineTaskLog() {
    }

    public RoutineTaskLog(Long userId, Long userRoutineTaskId, LocalDate logDate, LocalTime completedAt, Point authenticatedLocation, int earnedXp) {
        this.userId = userId;
        this.userRoutineTaskId = userRoutineTaskId;
        this.logDate = logDate != null ? logDate : LocalDate.now();
        this.completedAt = completedAt != null ? completedAt : LocalTime.now();
        this.authenticatedLocation = authenticatedLocation;
        this.earnedXp = earnedXp > 0 ? earnedXp : 100;
        this.isSuccess = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserRoutineTaskId() {
        return userRoutineTaskId;
    }

    public void setUserRoutineTaskId(Long userRoutineTaskId) {
        this.userRoutineTaskId = userRoutineTaskId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public LocalTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalTime completedAt) {
        this.completedAt = completedAt;
    }

    public Point getAuthenticatedLocation() {
        return authenticatedLocation;
    }

    public void setAuthenticatedLocation(Point authenticatedLocation) {
        this.authenticatedLocation = authenticatedLocation;
    }

    public int getEarnedXp() {
        return earnedXp;
    }

    public void setEarnedXp(int earnedXp) {
        this.earnedXp = earnedXp;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
