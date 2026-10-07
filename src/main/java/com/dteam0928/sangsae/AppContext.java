package com.dteam0928.sangsae;

import com.dteam0928.sangsae.auth.oauth.OAuthProviderRegistry;
import com.dteam0928.sangsae.auth.service.AuthService;
import com.dteam0928.sangsae.auth.service.OAuthLoginService;
import com.dteam0928.sangsae.auth.service.password.BCryptPasswordEncoder;
import com.dteam0928.sangsae.auth.service.password.PasswordEncoder;
import com.dteam0928.sangsae.common.config.AppConfig;
import com.dteam0928.sangsae.common.db.DataSourceFactory;
import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.onboarding.service.OnboardingService;
import com.dteam0928.sangsae.user.dao.InterestDao;
import com.dteam0928.sangsae.user.dao.UserDao;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.dao.jpa.JpaInterestDao;
import com.dteam0928.sangsae.user.dao.jpa.JpaUserDao;
import com.dteam0928.sangsae.user.dao.jpa.JpaUserProfileDao;
import com.dteam0928.sangsae.user.service.NicknameGenerator;
import com.dteam0928.sangsae.user.service.RandomNicknameGenerator;
import com.dteam0928.sangsae.user.service.UserAccountService;
import com.dteam0928.sangsae.common.map.dao.PlaceDao;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.util.DBUtil;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.user.exp.ExpPolicy;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContext;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 객체 조립소 (수동 DI 컨테이너). 앱 시작 시 한 번 만들어 ServletContext 에 보관한다. (팀 공용)
 *
 * <p>DAO/Service 는 여기서 한 번만 생성해 공유한다(무상태 싱글톤).
 * 구현체를 바꾸려면 이 파일의 생성 부분만 고치면 되고, 서블릿·서비스는 인터페이스에만 의존한다.
 *
 * <p><b>다른 기능 담당자</b>: 자기 DAO/Service 를 아래 "각 기능 Service" 영역에 추가하고 getter 를 열어 주면 된다.
 * 서블릿에서는 {@code app().xxxService()} (BaseServlet 상속 시) 로 꺼내 쓴다.
 * 트랜잭션이 필요하면 {@link #tx()} 를, 직접 EntityManager 가 필요하면 {@link #entityManagerFactory()} 를 쓴다.
 */
public class AppContext implements AutoCloseable {

    public static final String ATTRIBUTE = AppContext.class.getName();

    private final AppConfig config;
    private final DataSource dataSource;
    private final EntityManagerFactory emf;
    private final TransactionTemplate tx;

    // ---- 인증/온보딩 ----
    private final AuthService authService;
    private final OAuthLoginService oAuthLoginService;
    private final OnboardingService onboardingService;
    private final OAuthProviderRegistry oAuthProviderRegistry;
    
    // ---- 지도 / 장소 ----
    private final PlaceDao placeDao;
    private final PlaceService placeService;
    private final LocationAuthService locationAuthService;

    // ---- 경험치 / 레벨 ----
    private final ExpPolicy expPolicy;
    private final ExperienceService experienceService;

    // ---- 퀘스트 ----
    private final QuestService questService;
    
    public AppContext(AppConfig config) {
        this(config, DataSourceFactory.create(config));
    }

    private AppContext(AppConfig config, DataSource dataSource) {
        this(config, createEmf(config, dataSource), dataSource);
    }

    /** 테스트에서 다른 EntityManagerFactory 를 넣을 수 있게 열어 둔 생성자 */
    public AppContext(AppConfig config, EntityManagerFactory emf, DataSource dataSource) {
        this.config = config;
        this.dataSource = dataSource;
        this.emf = emf;
        this.tx = new TransactionTemplate(emf);

        // ---- DAO ----
        UserDao userDao = new JpaUserDao();
        UserProfileDao profileDao = new JpaUserProfileDao();
        InterestDao interestDao = new JpaInterestDao();

        // ---- 공통 컴포넌트 ----
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(config.getInt("security.bcrypt.cost", 10));
        NicknameGenerator nicknameGenerator = new RandomNicknameGenerator(
                config.getList("nickname.prefixes", "여행자,탐험가,산책러,모험가"),
                config.getInt("nickname.digits", 4));
        UserAccountService accountService = new UserAccountService(userDao, profileDao, nicknameGenerator);

        // ---- 인증/온보딩 Service ----
        this.authService = new AuthService(tx, userDao, accountService, passwordEncoder);
        this.oAuthLoginService = new OAuthLoginService(tx, userDao, accountService);
        this.onboardingService = new OnboardingService(tx, profileDao, interestDao, accountService);
        this.oAuthProviderRegistry = OAuthProviderRegistry.fromConfig(config);

        // ---- 지도 / 장소 (단일 커넥션 풀 공유) ----
        DBUtil.setDataSource(dataSource);
        this.placeDao = new PlaceDao(dataSource);
        this.placeService = new PlaceService(placeDao);
        this.locationAuthService = new LocationAuthService(placeService);

        // ---- 경험치 / 레벨 ----
        this.expPolicy = new ExpPolicy(
                config.getInt("exp.quest.default", ExpPolicy.DEFAULT_QUEST_XP),
                config.getInt("exp.routine.default", ExpPolicy.DEFAULT_ROUTINE_XP),
                config.getInt("exp.level.per-xp", ExpPolicy.XP_PER_LEVEL),
                config.getInt("exp.level.max", ExpPolicy.MAX_LEVEL)
        );
        this.experienceService = new ExperienceService(tx, profileDao, expPolicy);

        // ---- 퀘스트 (지도·위치인증·경험치 서비스 주입) ----
        this.questService = new QuestService(tx, placeService, locationAuthService, experienceService);
    }

    /**
     * persistence.xml 의 persistence-unit 을 쓰되, DB 접속은 설정 파일로 만든 커넥션 풀을 넘긴다.
     * (persistence.xml 에 계정/비밀번호를 적지 않기 위함)
     */
    private static EntityManagerFactory createEmf(AppConfig config, DataSource dataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.nonJtaDataSource", dataSource);
        return Persistence.createEntityManagerFactory(config.get("jpa.persistence-unit", "default"), props);
    }

    public static AppContext from(ServletContext servletContext) {
        return (AppContext) servletContext.getAttribute(ATTRIBUTE);
    }

    public AppConfig config() { return config; }
    public EntityManagerFactory entityManagerFactory() { return emf; }
    public TransactionTemplate tx() { return tx; }

    public AuthService authService() { return authService; }
    public OAuthLoginService oAuthLoginService() { return oAuthLoginService; }
    public OnboardingService onboardingService() { return onboardingService; }
    public OAuthProviderRegistry oAuthProviderRegistry() { return oAuthProviderRegistry; }
    
    public PlaceDao placeDao() { return placeDao; }
    public PlaceService placeService() { return placeService; }
    public LocationAuthService locationAuthService() { return locationAuthService; }
    public ExpPolicy expPolicy() { return expPolicy; }
    public ExperienceService experienceService() { return experienceService; }
    public QuestService questService() { return questService; }

    @Override
    public void close() {
        if (emf.isOpen()) emf.close();
        if (dataSource instanceof AutoCloseable c) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}
