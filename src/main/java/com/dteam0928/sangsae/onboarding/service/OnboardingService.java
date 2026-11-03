package com.dteam0928.sangsae.onboarding.service;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.db.DuplicateKeyException;
import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.common.validation.Validators;
import com.dteam0928.sangsae.onboarding.dto.LocationSetupRequest;
import com.dteam0928.sangsae.onboarding.dto.ProfileForm;
import com.dteam0928.sangsae.onboarding.dto.ProfileSetupRequest;
import com.dteam0928.sangsae.user.dao.InterestDao;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.model.UserProfile;
import com.dteam0928.sangsae.user.service.UserAccountService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** O-02 프로필·관심사 / O-03 위치 권한 */
public class OnboardingService {

    private final TransactionTemplate tx;
    private final UserProfileDao profileDao;
    private final InterestDao interestDao;
    private final UserAccountService accountService;

    public OnboardingService(TransactionTemplate tx, UserProfileDao profileDao, InterestDao interestDao,
                             UserAccountService accountService) {
        this.tx = tx;
        this.profileDao = profileDao;
        this.interestDao = interestDao;
        this.accountService = accountService;
    }

    /** O-02 화면 초기 데이터: 현재 닉네임(자동 생성값), 전체 칩, 이미 선택한 칩 */
    public ProfileForm getProfileForm(long userId) {
        return tx.read(em -> new ProfileForm(
                profileDao.findByUserId(em, userId).map(UserProfile::getNickname).orElse(""),
                interestDao.findAllCategories(em),
                interestDao.findCategoryIdsByUserId(em, userId),
                Validators.INTEREST_MAX_COUNT));
    }

    /**
     * O-02 [다음]. 닉네임 변경 + 관심사 교체를 한 트랜잭션으로 처리.
     * @return 저장된 닉네임 (세션 갱신용)
     */
    public String saveProfile(long userId, ProfileSetupRequest req) {
        String nickname = Validators.nickname(req.getNickname());
        Set<Short> interestIds = normalizeInterestIds(req.getInterestIds());

        try {
            tx.run(em -> {
                if (interestDao.countCategories(em, interestIds) != interestIds.size()) {
                    throw new BusinessException(ErrorCode.INTEREST_INVALID);
                }
                String current = profileDao.findByUserId(em, userId).map(UserProfile::getNickname).orElse(null);
                if (!nickname.equals(current)) {
                    if (profileDao.existsByNickname(em, nickname)) {
                        throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
                    }
                    profileDao.updateNickname(em, userId, nickname);
                }
                interestDao.replaceUserInterests(em, userId, interestIds);
            });
        } catch (DuplicateKeyException e) {
            if (e.isKey(UserProfileDao.UK_NICKNAME)) throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED, e);
            throw e;
        }
        return nickname;
    }

    /** 마이페이지: 위치 권한 허용 여부 (O-03 또는 마이페이지에서 허용하고 좌표를 저장했는지) */
    public boolean isLocationPermitted(long userId) {
        return tx.read(em -> profileDao.findByUserId(em, userId).map(UserProfile::isLocationPermitted).orElse(false));
    }

    /** O-03 [위치 허용하기] 결과 저장 → 온보딩 완료 */
    public LoginUser saveLocation(long userId, LocationSetupRequest req) {
        GeoPoint point = null;
        if (req.isPermitted()) {
            if (req.getLatitude() == null || req.getLongitude() == null
                    || !GeoPoint.isValid(req.getLatitude(), req.getLongitude()) 
                    || (req.getLatitude() == 0.0 && req.getLongitude() == 0.0)){
                throw new BusinessException(ErrorCode.LOCATION_INVALID);
            }
            point = GeoPoint.of(req.getLatitude(), req.getLongitude());
        }
        GeoPoint location = point;
        return tx.inTransaction(em -> {
            profileDao.updateLocation(em, userId, location != null, location);
            return accountService.loadLoginUser(em, userId, true);
        });
    }

    /** O-03 [나중에 하기] 또는 브라우저 거부: 좌표 없이 온보딩 완료 */
    public LoginUser skipLocation(long userId) {
        return saveLocation(userId, new LocationSetupRequest(false, null, null));
    }

    private static Set<Short> normalizeInterestIds(List<Short> ids) {
        Set<Short> set = new LinkedHashSet<>();
        if (ids != null) ids.stream().filter(Objects::nonNull).forEach(set::add);
        if (set.size() < Validators.INTEREST_MIN_COUNT) {
            throw new BusinessException(ErrorCode.INTEREST_REQUIRED);
        }
        if (set.size() > Validators.INTEREST_MAX_COUNT) {
            throw new BusinessException(ErrorCode.INTEREST_LIMIT, Validators.INTEREST_MAX_COUNT);
        }
        return set;
    }
}
