package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.routine.repository.CardQuery;
import com.dteam0928.sangsae.routine.repository.CardSort;
import com.dteam0928.sangsae.routine.repository.MyPlaceRepository;
import com.dteam0928.sangsae.routine.repository.PlaceRoutineRow;
import com.dteam0928.sangsae.routine.repository.PlaceUsage;
import com.dteam0928.sangsae.routine.repository.RoutineExploreRepository;
import com.dteam0928.sangsae.routine.dto.ExploreHome;
import com.dteam0928.sangsae.routine.dto.LocationInfo;
import com.dteam0928.sangsae.routine.dto.NearbySpot;
import com.dteam0928.sangsae.routine.dto.PageResult;
import com.dteam0928.sangsae.routine.dto.PlaceCategoryDto;
import com.dteam0928.sangsae.routine.dto.PlaceDetail;
import com.dteam0928.sangsae.routine.dto.PlaceRoutine;
import com.dteam0928.sangsae.routine.dto.PlaceSummary;
import com.dteam0928.sangsae.routine.dto.RoutineCard;
import com.dteam0928.sangsae.routine.dto.RoutineCategoryDto;
import com.dteam0928.sangsae.routine.dto.RoutineDetail;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** R-00 루틴 탐색 탭 조회 */
public class RoutineExploreService {

    private static final int KEYWORD_MAX = 50;
    private static final int TAG_LIMIT = 3;

    private final TransactionTemplate tx;
    private final RoutineExploreRepository routineRepository;
    private final MyPlaceRepository myPlaceRepository;
    private final UserProfileDao profileDao;
    private final RoutineSettings settings;

    public RoutineExploreService(TransactionTemplate tx, RoutineExploreRepository routineRepository, MyPlaceRepository myPlaceRepository,
                                 UserProfileDao profileDao, RoutineSettings settings) {
        this.tx = tx;
        this.routineRepository = routineRepository;
        this.myPlaceRepository = myPlaceRepository;
        this.profileDao = profileDao;
        this.settings = settings;
    }

    public List<RoutineCategoryDto> categories() {
        return tx.read(routineRepository::findCategories);
    }

    /**
     * 탐색 탭 첫 화면 (모든 섹션).
     *
     * @param placeCategory 지도 [필터] 장소 유형 (GYM, CAFE …). 빈 값 또는 ALL 이면 전체.
     *                      주변 스팟과 "근처에서 뜨는" 루틴에 적용
     */
    public ExploreHome home(long userId, Double lat, Double lng, String placeCategory) {
        return tx.read(em -> {
            List<PlaceCategoryDto> placeCategories = routineRepository.findPlaceCategories(em);
            String placeCode = normalizePlaceCategory(placeCategories, placeCategory);
            LocationInfo location = resolveLocation(em, userId, lat, lng);
            GeoPoint origin = GeoPoint.of(location.latitude(), location.longitude());
            int n = settings.sectionSize();
            int days = settings.trendingDays();

            List<PlaceSummary> nearbyPlaces = routineRepository.findPlacesWithRoutines(
                    em, origin, settings.nearbyRadiusMeters(), placeCode, settings.mapPlaceLimit());
            List<PlaceSummary> myPlaces = myPlaceRepository.findMyPlaces(em, userId, origin);

            return new ExploreHome(
                    location,
                    routineRepository.findCategories(em),
                    placeCategories,
                    buildSpots(em, userId, nearbyPlaces),
                    routineRepository.findCards(em, CardQuery.of(userId, origin, days, CardSort.TRENDING, n)
                            .withRadius(settings.nearbyRadiusMeters())
                            .withPlaceCategory(placeCode)),
                    routineRepository.findCards(em, CardQuery.of(userId, origin, days, CardSort.RECOMMEND, n)
                            .excludingAdopted()),
                    routineRepository.findCards(em, CardQuery.of(userId, origin, days, CardSort.POPULAR, n)),
                    routineRepository.findCards(em, CardQuery.of(userId, origin, days, CardSort.LATEST, n)),
                    myPlaces,
                    myPlaces.isEmpty() ? List.of() : placeRoutines(em, userId, myPlaces.get(0).id()));
        });
    }

    /**
     * 검색 · 카테고리 칩 · 정렬.
     *
     * @param category      칩 코드. 빈 값 또는 ALL 이면 전체
     * @param placeCategory 대표 장소 유형 (GYM, CAFE …). 빈 값 또는 ALL 이면 전체
     * @param sort          distance(기본) / popular / latest / recommend
     * @param page          1부터
     */
    public PageResult<RoutineCard> search(long userId, Double lat, Double lng, String keyword, String category,
                                          String placeCategory, String sort, int page) {
        String kw = normalizeKeyword(keyword);
        CardSort cardSort = parseSort(sort);
        int size = settings.pageSize();
        int safePage = Math.max(page, 1);

        return tx.read(em -> {
            String code = normalizeCategory(em, category);
            String placeCode = normalizePlaceCategory(routineRepository.findPlaceCategories(em), placeCategory);
            LocationInfo location = resolveLocation(em, userId, lat, lng);
            GeoPoint origin = GeoPoint.of(location.latitude(), location.longitude());
            CardQuery query = CardQuery.of(userId, origin, settings.trendingDays(), cardSort, size)
                    .withKeyword(kw)
                    .withCategory(code)
                    .withPlaceCategory(placeCode)
                    .page((safePage - 1) * size, size + 1);   // 1개 더 읽어서 다음 페이지 여부 판단
            List<RoutineCard> found = routineRepository.findCards(em, query);
            boolean hasNext = found.size() > size;
            return new PageResult<>(hasNext ? found.subList(0, size) : found, safePage, size, hasNext);
        });
    }

    /** 루틴 상세 모달 */
    public RoutineDetail detail(long userId, long templateId, Double lat, Double lng) {
        return tx.read(em -> {
            LocationInfo location = resolveLocation(em, userId, lat, lng);
            RoutineCard card = routineRepository.findCard(em, userId, templateId,
                            GeoPoint.of(location.latitude(), location.longitude()), settings.trendingDays())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ROUTINE_NOT_FOUND));
            return new RoutineDetail(card, routineRepository.findTasks(em, templateId),
                    routineRepository.countPerformers(em, templateId), routineRepository.hasActiveFork(em, userId, templateId));
        });
    }

    /** 장소 상세 모달 / 내가 다니는 장소 셀렉트 변경 */
    public PlaceDetail placeDetail(long userId, long placeId, Double lat, Double lng) {
        return tx.read(em -> {
            LocationInfo location = resolveLocation(em, userId, lat, lng);
            PlaceSummary place = routineRepository.findPlace(em, placeId,
                            GeoPoint.of(location.latitude(), location.longitude()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
            return new PlaceDetail(buildSpots(em, userId, List.of(place)).get(0), placeRoutines(em, userId, placeId));
        });
    }

    // ------------------------------------------------------------------ 내부

    /** 기준 위치: 요청 좌표(GPS) → O-03 저장 위치 → 기본 위치 */
    LocationInfo resolveLocation(EntityManager em, long userId, Double lat, Double lng) {
        if (lat != null && lng != null && GeoPoint.isValid(lat, lng) && !(lat == 0.0 && lng == 0.0)) {
            return new LocationInfo(lat, lng, "GPS");
        }
        GeoPoint saved = profileDao.findByUserId(em, userId).map(UserProfile::getLastKnownLocation).orElse(null);
        if (saved != null) return new LocationInfo(saved.getLatitude(), saved.getLongitude(), "PROFILE");
        return new LocationInfo(settings.defaultLatitude(), settings.defaultLongitude(), "DEFAULT");
    }

    private List<NearbySpot> buildSpots(EntityManager em, long userId, List<PlaceSummary> places) {
        if (places.isEmpty()) return List.of();
        List<Long> ids = places.stream().map(PlaceSummary::id).toList();
        Map<Long, List<PlaceRoutineRow>> byPlace = routineRepository.findRoutinesByPlaces(em, userId, ids).stream()
                .collect(Collectors.groupingBy(PlaceRoutineRow::placeId));
        Map<Long, Map<Integer, Integer>> plannedHours = routineRepository.findTaskHoursByPlaces(em, ids);
        Map<Long, PlaceUsage> usage = routineRepository.findUsageByPlaces(em, ids, settings.usageDays());
        Map<String, RoutineCategoryDto> categories = categoryMap(em);

        return places.stream().map(place -> {
            List<PlaceRoutineRow> rows = byPlace.getOrDefault(place.id(), List.of());
            return new NearbySpot(place, rows.size(), PlaceInsights.topTags(rows, categories, TAG_LIMIT),
                    PlaceInsights.timeInsight(usage.get(place.id()), plannedHours.get(place.id()),
                            settings.surgeRatio(), settings.surgeMinCount()),
                    PlaceInsights.weightedFit(rows));
        }).toList();
    }

    /** 장소의 인기 루틴: 실천 중 인원 → 누적 추가 인원 순 */
    private List<PlaceRoutine> placeRoutines(EntityManager em, long userId, long placeId) {
        return routineRepository.findRoutinesByPlaces(em, userId, List.of(placeId)).stream()
                .sorted(Comparator.comparingInt(PlaceRoutineRow::performerCount).reversed()
                        .thenComparing(Comparator.comparingInt(PlaceRoutineRow::adoptedCount).reversed()))
                .limit(settings.sectionSize())
                .map(r -> new PlaceRoutine(r.templateId(), r.title(), r.performerCount(), r.fitScore()))
                .toList();
    }

    private Map<String, RoutineCategoryDto> categoryMap(EntityManager em) {
        Map<String, RoutineCategoryDto> map = new LinkedHashMap<>();
        for (RoutineCategoryDto c : routineRepository.findCategories(em)) map.put(c.code(), c);
        return map;
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String kw = keyword.trim();
        return kw.length() > KEYWORD_MAX ? kw.substring(0, KEYWORD_MAX) : kw;
    }

    private String normalizeCategory(EntityManager em, String category) {
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category.trim())) return null;
        String code = category.trim().toUpperCase(Locale.ROOT);
        boolean exists = routineRepository.findCategories(em).stream().anyMatch(c -> Objects.equals(c.code(), code));
        if (!exists) throw new BusinessException(ErrorCode.INVALID_INPUT);
        return code;
    }

    private static String normalizePlaceCategory(List<PlaceCategoryDto> placeCategories, String placeCategory) {
        if (placeCategory == null || placeCategory.isBlank() || "ALL".equalsIgnoreCase(placeCategory.trim())) {
            return null;
        }
        String code = placeCategory.trim().toUpperCase(Locale.ROOT);
        boolean exists = placeCategories.stream().anyMatch(c -> Objects.equals(c.code(), code));
        if (!exists) throw new BusinessException(ErrorCode.INVALID_INPUT);
        return code;
    }

    private static CardSort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return CardSort.DISTANCE;
        return switch (sort.trim().toLowerCase(Locale.ROOT)) {
            case "distance" -> CardSort.DISTANCE;
            case "popular" -> CardSort.POPULAR;
            case "latest" -> CardSort.LATEST;
            case "recommend" -> CardSort.RECOMMEND;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }
}
