package com.dteam0928.sangsae.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** interest_categories (관심사 8종 테마 마스터) — DB 설계서 [도메인 1] #3. 조회 전용 */
@Entity
@Table(name = "interest_categories")
public class InterestCategory {

    @Id
    @Column(name = "id")
    private Short id;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    /** 표시명. 설계서 기준 '#맛집' 처럼 # 을 포함한다 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    public InterestCategory() {
    }

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public short getSortOrder() { return sortOrder; }
}
