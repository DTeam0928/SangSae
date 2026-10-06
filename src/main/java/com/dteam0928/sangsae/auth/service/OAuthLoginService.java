package com.dteam0928.sangsae.auth.service;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.oauth.OAuthUserInfo;
import com.dteam0928.sangsae.common.db.DuplicateKeyException;
import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.user.dao.UserDao;
import com.dteam0928.sangsae.user.model.User;
import com.dteam0928.sangsae.user.service.UserAccountService;

import java.util.Locale;

/**
 * 소셜 로그인 결과로 로그인/가입 처리.
 *
 * <ol>
 * <li>(provider, oauth_id) 로 가입된 계정이 있으면 → 로그인</li>
 * <li>같은 이메일이 다른 수단으로 이미 가입돼 있으면 → 가입 거부 (정책: 1 이메일 = 1 가입 수단)</li>
 * <li>그 외 → 신규 가입 (기본 닉네임 자동 생성, O-02 로 이동)</li>
 * </ol>
 */
public class OAuthLoginService {

    private final TransactionTemplate tx;
    private final UserDao userDao;
    private final UserAccountService accountService;

    public OAuthLoginService(TransactionTemplate tx, UserDao userDao, UserAccountService accountService) {
        this.tx = tx;
        this.userDao = userDao;
        this.accountService = accountService;
    }

    public LoginUser login(OAuthUserInfo info) {
        String email = info.getEmail() == null ? null : info.getEmail().trim().toLowerCase(Locale.ROOT);
        try {
            return tx.inTransaction(em -> {
                User existing = userDao.findByOAuth(em, info.getProvider(), info.getProviderUserId()).orElse(null);
                if (existing != null) {
                    accountService.assertLoginable(existing);
                    return accountService.toLoginUser(em, existing, true);
                }

                if (email != null) {
                    userDao.findByEmail(em, email).ifPresent(other -> {
                        throw new BusinessException(ErrorCode.OAUTH_EMAIL_DUPLICATED,
                                other.getOauthProvider().getDisplayName());
                    });
                }

                User user = User.social(info.getProvider(), info.getProviderUserId(), email);
                accountService.createAccount(em, user);
                return accountService.toLoginUser(em, user, false); // 신규 가입 → O-02
            });
        } catch (DuplicateKeyException e) {
            if (e.isKey(UserDao.UK_EMAIL)) {
                throw new BusinessException(ErrorCode.OAUTH_EMAIL_DUPLICATED, e, "기존 가입 수단");
            }
            // 같은 Google 계정 콜백이 동시에 두 번 들어온 경우: 먼저 만들어진 계정으로 로그인
            if (e.isKey(UserDao.UK_OAUTH))
                return login(info);
            throw e;
        }
    }
}
