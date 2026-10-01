package com.dteam0928.sangsae.user.service;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.db.DuplicateKeyException;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.user.dao.UserDao;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.model.User;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;


/**
 * 이메일 가입과 소셜 가입이 함께 쓰는 계정 공통 로직.
 * 트랜잭션은 호출하는 Service 가 열고, 여기서는 넘겨받은 EntityManager 만 사용한다.
 */
public class UserAccountService {

    /** 기본 닉네임 후보를 뽑는 최대 횟수 (이미 쓰는 닉네임이면 다시 뽑음) */
    private static final int NICKNAME_MAX_ATTEMPTS = 10;

    private final UserDao userDao;
    private final UserProfileDao profileDao;
    private final NicknameGenerator nicknameGenerator;

    public UserAccountService(UserDao userDao, UserProfileDao profileDao, NicknameGenerator nicknameGenerator) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.nicknameGenerator = nicknameGenerator;
    }

    /**
     * users + user_profiles(기본 닉네임) 생성.
     * 이메일 중복은 {@link DuplicateKeyException}(uk_users_email) 로 올라가므로 호출한 쪽에서 안내 문구로 바꾼다.
     */
    public UserProfile createAccount(EntityManager em, User user) {
        userDao.insert(em, user);
        UserProfile profile = new UserProfile(user.getId(), pickNickname(em));
        profileDao.insert(em, profile);
        return profile;
    }

    private String pickNickname(EntityManager em) {
        for (int i = 0; i < NICKNAME_MAX_ATTEMPTS; i++) {
            String candidate = nicknameGenerator.generate();
            if (!profileDao.existsByNickname(em, candidate)) return candidate;
        }
        throw new IllegalStateException("기본 닉네임 생성 실패: nickname.prefixes / nickname.digits 설정을 늘려 주세요.");
    }


    /** 로그인 가능한 상태인지 확인 */
    public void assertLoginable(User user) {
        switch (user.getStatus()) {
            case ACTIVE -> { }
            case SUSPENDED -> throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
            case WITHDRAWN -> throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
    }

    /**
     * 세션에 넣을 LoginUser 조립.
     * @param onboardingCompleted 방금 가입한 계정이면 false → 필터가 O-02 로 보낸다
     */
    public LoginUser toLoginUser(EntityManager em, User user, boolean onboardingCompleted) {
        String nickname = profileDao.findByUserId(em, user.getId()).map(UserProfile::getNickname).orElse(null);
        return new LoginUser(user.getId(), user.getEmail(), nickname, user.getOauthProvider(), onboardingCompleted);
    }

    public LoginUser loadLoginUser(EntityManager em, long userId, boolean onboardingCompleted) {
        User user = userDao.findById(em, userId).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return toLoginUser(em, user, onboardingCompleted);
    }
}
