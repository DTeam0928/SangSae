package com.dteam0928.sangsae.user.service;

import com.dteam0928.sangsae.common.util.RandomUtil;

import java.util.List;

/**
 * "접두어 + 숫자" 형식. 예: 여행자4821, 탐험가0193
 * 접두어는 설정(nickname.prefixes)으로 바꿀 수 있다.
 */
public class RandomNicknameGenerator implements NicknameGenerator {

    private final List<String> prefixes;
    private final int digits;
    private final int bound;

    public RandomNicknameGenerator(List<String> prefixes, int digits) {
        if (prefixes.isEmpty()) throw new IllegalArgumentException("닉네임 접두어가 비어 있습니다.");
        if (digits < 1 || digits > 9) throw new IllegalArgumentException("digits 는 1~9");
        this.prefixes = List.copyOf(prefixes);
        this.digits = digits;
        this.bound = (int) Math.pow(10, digits);
    }

    @Override
    public String generate() {
        String prefix = prefixes.get(RandomUtil.nextInt(prefixes.size()));
        return prefix + String.format("%0" + digits + "d", RandomUtil.nextInt(bound));
    }
}
