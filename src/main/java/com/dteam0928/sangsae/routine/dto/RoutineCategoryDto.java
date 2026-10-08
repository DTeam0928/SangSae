package com.dteam0928.sangsae.routine.dto;

/**
 * 카테고리 칩 (routine_categories).
 * JSP(EL)에서 ${c.name} 으로 읽을 수 있게 record 대신 getter 가 있는 클래스로 둔다
 * (Tomcat 10.1 의 EL 은 record 접근자를 프로퍼티로 인식하지 못함).
 */
public final class RoutineCategoryDto {

    private final String code;
    private final String name;
    private final String emoji;

    public RoutineCategoryDto(String code, String name, String emoji) {
        this.code = code;
        this.name = name;
        this.emoji = emoji;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }

    // record 와 같은 이름으로도 부를 수 있게 (서비스 코드에서 사용)
    public String code() { return code; }
    public String name() { return name; }
    public String emoji() { return emoji; }
}
