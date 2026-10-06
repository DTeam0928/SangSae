package com.dteam0928.sangsae.user.service;

/** 기본 닉네임 생성 규칙. 규칙을 바꾸려면 구현체만 교체한다. */
public interface NicknameGenerator {

    /** 후보 닉네임 하나 생성 (중복 여부는 호출하는 쪽에서 확인) */
    String generate();
}
