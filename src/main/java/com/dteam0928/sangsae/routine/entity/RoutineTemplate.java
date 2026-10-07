package com.dteam0928.sangsae.routine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "routine_templates")
public class RoutineTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "representative_place_id")
    private Long representativePlaceId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility = "PUBLIC";

    @Column(name = "adopted_count", nullable = false)
    private int adoptedCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RoutineTemplate() {
    }

    public RoutineTemplate(Long creatorId, Long representativePlaceId, String title, String description, String category, String visibility) {
        this.creatorId = creatorId;
        this.representativePlaceId = representativePlaceId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.visibility = visibility != null ? visibility : "PUBLIC";
        this.adoptedCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getRepresentativePlaceId() {
        return representativePlaceId;
    }

    public void setRepresentativePlaceId(Long representativePlaceId) {
        this.representativePlaceId = representativePlaceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public int getAdoptedCount() {
        return adoptedCount;
    }

    public void setAdoptedCount(int adoptedCount) {
        this.adoptedCount = adoptedCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
