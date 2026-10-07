package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.AppContext;
import com.dteam0928.sangsae.user.model.AuthProvider;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.config.AppConfig;
import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.web.SessionKeys;
import com.dteam0928.sangsae.routine.entity.RoutineTaskLog;
import com.dteam0928.sangsae.routine.entity.RoutineTemplate;
import com.dteam0928.sangsae.routine.entity.RoutineTemplateTask;
import com.dteam0928.sangsae.routine.entity.UserRoutine;
import com.dteam0928.sangsae.routine.entity.UserRoutineTask;
import com.dteam0928.sangsae.routine.repository.RoutineTaskLogRepository;
import com.dteam0928.sangsae.routine.repository.RoutineTemplateRepository;
import com.dteam0928.sangsae.routine.repository.UserRoutineRepository;
import com.dteam0928.sangsae.routine.service.RoutineService;
import com.dteam0928.sangsae.testutil.ServletTestHelper;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.exp.ExpPolicy;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class RoutineServletTest {

    private final Map<Long, RoutineTemplate> templateDb = new HashMap<>();
    private final Map<Long, List<RoutineTemplateTask>> templateTasksDb = new HashMap<>();
    private final Map<Long, UserRoutine> userRoutineDb = new HashMap<>();
    private final Map<Long, UserRoutineTask> userTaskDb = new HashMap<>();
    private final Map<Long, UserProfile> profileDb = new HashMap<>();

    private AppContext appContext;
    private Long idSequence = 1L;

    @BeforeEach
    void setUp() throws Exception {
        templateDb.clear();
        templateTasksDb.clear();
        userRoutineDb.clear();
        userTaskDb.clear();
        profileDb.clear();

        EntityManager mockEm = (EntityManager) Proxy.newProxyInstance(
            EntityManager.class.getClassLoader(),
            new Class<?>[]{EntityManager.class},
            (proxy, method, args) -> {
                if ("find".equals(method.getName()) && args != null && args.length >= 2) {
                    if (args[0] == UserProfile.class) {
                        return profileDb.get(args[1]);
                    }
                }
                return null;
            }
        );

        TransactionTemplate stubTx = new TransactionTemplate(null) {
            @Override
            public <T> T inTransaction(com.dteam0928.sangsae.common.db.EmFunction<T> work) {
                return work.apply(mockEm);
            }
            @Override
            public <T> T read(com.dteam0928.sangsae.common.db.EmFunction<T> work) {
                return work.apply(mockEm);
            }
        };

        RoutineTemplateRepository templateRepo = new RoutineTemplateRepository() {
            @Override
            public RoutineTemplate findById(EntityManager em, Long id) {
                return templateDb.get(id);
            }
            @Override
            public List<RoutineTemplate> findPublicTemplates(EntityManager em, String category) {
                return new ArrayList<>(templateDb.values());
            }
            @Override
            public List<RoutineTemplateTask> findTasksByTemplateId(EntityManager em, Long routineTemplateId) {
                return templateTasksDb.getOrDefault(routineTemplateId, new ArrayList<>());
            }
            @Override
            public void incrementAdoptedCount(EntityManager em, Long templateId) {
                RoutineTemplate t = templateDb.get(templateId);
                if (t != null) t.setAdoptedCount(t.getAdoptedCount() + 1);
            }
        };

        UserRoutineRepository userRoutineRepo = new UserRoutineRepository() {
            @Override
            public UserRoutine saveRoutine(EntityManager em, UserRoutine userRoutine) {
                if (userRoutine.getId() == null) userRoutine.setId(idSequence++);
                userRoutineDb.put(userRoutine.getId(), userRoutine);
                return userRoutine;
            }
            @Override
            public UserRoutineTask saveTask(EntityManager em, UserRoutineTask task) {
                if (task.getId() == null) task.setId(idSequence++);
                userTaskDb.put(task.getId(), task);
                return task;
            }
            @Override
            public UserRoutine findRoutineById(EntityManager em, Long id) {
                return userRoutineDb.get(id);
            }
            @Override
            public UserRoutineTask findTaskById(EntityManager em, Long id) {
                return userTaskDb.get(id);
            }
            @Override
            public List<UserRoutine> findActiveRoutinesByUserId(EntityManager em, Long userId) {
                return userRoutineDb.values().stream().filter(r -> r.getUserId().equals(userId) && r.isActive()).toList();
            }
            @Override
            public List<UserRoutineTask> findTasksByUserRoutineIds(EntityManager em, List<Long> routineIds) {
                return userTaskDb.values().stream().filter(t -> routineIds.contains(t.getUserRoutineId()) && t.isActive()).toList();
            }
        };

        RoutineTaskLogRepository logRepo = new RoutineTaskLogRepository() {
            @Override
            public RoutineTaskLog save(EntityManager em, RoutineTaskLog log) {
                if (log.getId() == null) log.setId(idSequence++);
                return log;
            }
            @Override
            public RoutineTaskLog findTodayLog(EntityManager em, Long userId, Long userRoutineTaskId, LocalDate date) {
                return null;
            }
            @Override
            public List<RoutineTaskLog> findTodayLogsByUser(EntityManager em, Long userId, LocalDate date) {
                return java.util.Collections.emptyList();
            }
        };

        UserProfileDao profileDao = new UserProfileDao() {
            @Override public void insert(EntityManager em, UserProfile profile) { profileDb.put(profile.getUserId(), profile); }
            @Override public Optional<UserProfile> findByUserId(EntityManager em, long userId) { return Optional.ofNullable(profileDb.get(userId)); }
            @Override public boolean existsByNickname(EntityManager em, String nickname) { return false; }
            @Override public void updateNickname(EntityManager em, long uid, String nickname) {}
            @Override public void updateLocation(EntityManager em, long uid, boolean permitted, com.dteam0928.sangsae.common.geo.GeoPoint location) {}
        };

        ExperienceService experienceService = new ExperienceService(stubTx, profileDao, new ExpPolicy(500, 100, 1000, 100));
        PlaceService placeService = new PlaceService(null);
        LocationAuthService locationAuthService = new LocationAuthService(placeService);

        RoutineService routineService = new RoutineService(stubTx, placeService, locationAuthService, experienceService,
                templateRepo, userRoutineRepo, logRepo);

        // AppContext 더미 객체에 필드 주입
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        appContext = (AppContext) unsafe.allocateInstance(AppContext.class);

        setField(appContext, "routineService", routineService);
        setField(appContext, "experienceService", experienceService);
        setField(appContext, "config", new AppConfig(new Properties()));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = AppContext.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("GET /routines: 공개 템플릿 목록 조회 성공")
    void getPublicRoutines_success() throws Exception {
        RoutineTemplate tpl = new RoutineTemplate(1L, null, "아침 조깅", "공원 조깅", "EXERCISE", "PUBLIC");
        tpl.setId(10L);
        templateDb.put(10L, tpl);

        RoutineCatalogServlet servlet = new RoutineCatalogServlet();
        ServletTestHelper.MockContext ctx = new ServletTestHelper.MockContext();
        ctx.contextAttributes.put(AppContext.ATTRIBUTE, appContext);
        ServletTestHelper.initServlet(servlet, ctx);

        servlet.doGet(ServletTestHelper.createRequest(ctx), ServletTestHelper.createResponse(ctx));

        assertEquals(200, ctx.status);
        String body = ctx.responseBody.toString();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("아침 조깅"));
    }

    @Test
    @DisplayName("UserRoutineServlet: 비로그인 접근 시 401 Unauthorized 차단")
    void userRoutineServlet_unauthorized() throws Exception {
        UserRoutineServlet servlet = new UserRoutineServlet();
        ServletTestHelper.MockContext ctx = new ServletTestHelper.MockContext();
        ctx.contextAttributes.put(AppContext.ATTRIBUTE, appContext);
        ServletTestHelper.initServlet(servlet, ctx);

        servlet.doGet(ServletTestHelper.createRequest(ctx), ServletTestHelper.createResponse(ctx));
        assertEquals(401, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("로그인이 필요합니다"));
    }

    @Test
    @DisplayName("POST /user-routines/adopt: 로그인 상태에서 템플릿 담기 성공")
    void adoptRoutine_authenticated_success() throws Exception {
        RoutineTemplate tpl = new RoutineTemplate(1L, null, "독서 루틴", "매일 20분", "STUDY", "PUBLIC");
        tpl.setId(5L);
        templateDb.put(5L, tpl);

        UserRoutineServlet servlet = new UserRoutineServlet();
        ServletTestHelper.MockContext ctx = new ServletTestHelper.MockContext();
        ctx.contextAttributes.put(AppContext.ATTRIBUTE, appContext);
        ctx.sessionAttributes.put(SessionKeys.LOGIN_USER, new LoginUser(100L, "test@test.com", "탐험가", AuthProvider.LOCAL, true));
        ctx.params.put("routineTemplateId", "5");

        ServletTestHelper.initServlet(servlet, ctx);

        var req = ServletTestHelper.createRequest(ctx);
        var proxyReq = (jakarta.servlet.http.HttpServletRequest) Proxy.newProxyInstance(
            jakarta.servlet.http.HttpServletRequest.class.getClassLoader(),
            new Class<?>[]{jakarta.servlet.http.HttpServletRequest.class},
            (proxy, method, args) -> {
                if ("getServletPath".equals(method.getName())) return "/user-routines/adopt";
                return method.invoke(req, args);
            }
        );

        servlet.doPost(proxyReq, ServletTestHelper.createResponse(ctx));

        assertEquals(200, ctx.status);
        String body = ctx.responseBody.toString();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("독서 루틴"));
    }

    @Test
    @DisplayName("POST /user-routines/tasks/complete: 실천 완료 시 100 XP 보상 JSON 반환")
    void completeTask_success_returns100XpReward() throws Exception {
        Long userId = 100L;
        UserProfile profile = new UserProfile(userId, "탐험가");
        profile.setTotalXp(0);
        profileDb.put(userId, profile);

        UserRoutine routine = new UserRoutine(userId, null, "명상 루틴", LocalDate.now(), null);
        routine.setId(1L);
        userRoutineDb.put(1L, routine);

        UserRoutineTask task = new UserRoutineTask(1L, null, null, "5분 명상", LocalTime.of(8, 0), "HEALTH", "DAILY", 100, 50);
        task.setId(10L);
        userTaskDb.put(10L, task);

        RoutineTaskCompleteServlet servlet = new RoutineTaskCompleteServlet();
        ServletTestHelper.MockContext ctx = new ServletTestHelper.MockContext();
        ctx.contextAttributes.put(AppContext.ATTRIBUTE, appContext);
        ctx.sessionAttributes.put(SessionKeys.LOGIN_USER, new LoginUser(userId, "test@test.com", "탐험가", AuthProvider.LOCAL, true));
        ctx.params.put("taskId", "10");

        ServletTestHelper.initServlet(servlet, ctx);

        servlet.doPost(ServletTestHelper.createRequest(ctx), ServletTestHelper.createResponse(ctx));

        assertEquals(200, ctx.status);
        String body = ctx.responseBody.toString();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"earnedXp\":100"));
        assertTrue(body.contains("\"newTotalXp\":100"));
    }
}
