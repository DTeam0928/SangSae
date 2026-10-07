package com.dteam0928.sangsae.routine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "routine_task_schedules")
public class RoutineTaskSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_routine_task_id", nullable = false)
    private Long userRoutineTaskId;

    @Column(name = "day_of_week", nullable = false, length = 3)
    private String dayOfWeek;

    public RoutineTaskSchedule() {
    }

    public RoutineTaskSchedule(Long userRoutineTaskId, String dayOfWeek) {
        this.userRoutineTaskId = userRoutineTaskId;
        this.dayOfWeek = dayOfWeek;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserRoutineTaskId() {
        return userRoutineTaskId;
    }

    public void setUserRoutineTaskId(Long userRoutineTaskId) {
        this.userRoutineTaskId = userRoutineTaskId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
