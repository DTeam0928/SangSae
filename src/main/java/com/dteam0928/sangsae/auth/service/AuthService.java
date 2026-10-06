package com.dteam0928.sangsae.auth.service;

import com.dteam0928.sangsae.auth.dto.LoginRequest;
import com.dteam0928.sangsae.auth.dto.SignupRequest;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.service.password.PasswordEncoder;
import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.validation.Validators;
import com.dteam0928.sangsae.user.dao.UserDao;
import com.dteam0928.sangsae.user.model.User;
import com.dteam0928.sangsae.user.service.UserAccountService;

/** 이메일(LOCAL) 로그인 / 회원가입 */
public class AuthService {

    private final TransactionTemplate tx;
    private final UserDao userDao;
    private final UserAccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(TransactionTemplate tx, UserDao userDao, UserAccountService accountService,
                       PasswordEncoder passwordEncoder) {
        this.tx = tx;
        this.userDao = userDao;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일 중복확인.
     * @return 정규화된(소문자) 이메일 — 컨트롤러가 세션에 "확인 완료 이메일"로 보관한다
     */
    public String checkEmailAvailable(String rawEmail) {
        String email = Validators.email(rawEmail);
        if (tx.read(em -> userDao.existsByEmail(em, email))) throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        return email;
    }

    /**
     * 회원가입. 검증 순서는 화면설계 O-01 을 따른다:
     * 이메일 형식 → 중복확인 여부 → 비밀번호 규칙 → 비밀번호 확인
     *
     * @param checkedEmail 세션에 저장된 중복확인 통과 이메일 (없으면 null)
     */
    public LoginUser signup(SignupRequest req, String checkedEmail) {
        String email = Validators.email(req.getEmail());
        if (!email.equals(checkedEmail)) throw new BusinessException(ErrorCode.EMAIL_NOT_CHECKED);
        String password = Validators.newPassword(req.getPassword());
        Validators.passwordConfirm(password, req.getPasswordConfirm());

               String hash = passwordEncoder.encode(password);   // 느린 작업이라 트랜잭션 밖에서
        return tx.inTransaction(em -> {
            // 중복확인 이후 다른 사람이 먼저 가입했을 수 있으므로 한 번 더 확인
            if (userDao.existsByEmail(em, email)) throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
            User user = User.local(email, hash);
            accountService.createAccount(em, user);
            return accountService.toLoginUser(em, user, false);   // 가입 직후 → O-02
        });
    }

    public LoginUser login(LoginRequest req) {
        String email = Validators.email(req.getEmail());
        String password = Validators.passwordPresent(req.getPassword());

        return tx.read(em -> {
            User user = userDao.findByEmail(em, email).orElse(null);
                       if (user == null) throw new BusinessException(ErrorCode.LOGIN_FAILED);
            if (user.getOauthProvider().isSocial()) {
                // 정책: 같은 이메일은 한 가지 가입 수단만 허용 → 가입한 수단을 안내
                throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_EMAIL, user.getOauthProvider().getDisplayName());
            }
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BusinessException(ErrorCode.LOGIN_FAILED);
            }
            accountService.assertLoginable(user);
            return accountService.toLoginUser(em, user, true);
        });
    }
}
