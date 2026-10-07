package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.routine.repository.MyPlaceRepository;
import com.dteam0928.sangsae.routine.dto.PlaceRegisterRequest;
import com.dteam0928.sangsae.routine.dto.PlaceSummary;

import java.util.List;

/** 내가 다니는 장소 (장소 관리) */
public class MyPlaceService {

    private final TransactionTemplate tx;
    private final MyPlaceRepository myPlaceRepository;
    private final RoutineSettings settings;

    public MyPlaceService(TransactionTemplate tx, MyPlaceRepository myPlaceRepository, RoutineSettings settings) {
        this.tx = tx;
        this.myPlaceRepository = myPlaceRepository;
        this.settings = settings;
    }

    public List<PlaceSummary> list(long userId, Double lat, Double lng) {
        GeoPoint origin = toPoint(lat, lng);
        return tx.read(em -> myPlaceRepository.findMyPlaces(em, userId, origin));
    }

    /** 등록 후 최신 목록 반환. 이미 등록된 장소면 목록만 반환 */
    public List<PlaceSummary> register(long userId, PlaceRegisterRequest req) {
        NewPlace newPlace = req.getPlaceId() == null ? NewPlace.from(req) : null;
        return tx.inTransaction(em -> {
            long placeId;
            if (newPlace == null) {
                placeId = req.getPlaceId();
                if (!myPlaceRepository.existsPlace(em, placeId)) throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
            } else {
                placeId = myPlaceRepository.upsertKakaoPlace(em, newPlace.kakaoPlaceId, newPlace.name, newPlace.roadAddress,
                        newPlace.region1, newPlace.region2, newPlace.region3, newPlace.point, newPlace.category);
            }
            List<PlaceSummary> current = myPlaceRepository.findMyPlaces(em, userId, null);
            final long id = placeId;
            boolean already = current.stream().anyMatch(p -> p.id() == id);
            if (!already && current.size() >= settings.myPlaceLimit()) {
                throw new BusinessException(ErrorCode.PLACE_LIMIT, settings.myPlaceLimit());
            }
            myPlaceRepository.addMyPlace(em, userId, placeId);
            return myPlaceRepository.findMyPlaces(em, userId, null);
        });
    }

    public List<PlaceSummary> remove(long userId, long placeId) {
        return tx.inTransaction(em -> {
            myPlaceRepository.removeMyPlace(em, userId, placeId);
            return myPlaceRepository.findMyPlaces(em, userId, null);
        });
    }

    private static GeoPoint toPoint(Double lat, Double lng) {
        if (lat == null || lng == null || !GeoPoint.isValid(lat, lng) || (lat == 0.0 && lng == 0.0)) return null;
        return GeoPoint.of(lat, lng);
    }

    /** 카카오 장소 입력값 검증 결과 */
    private record NewPlace(String kakaoPlaceId, String name, String roadAddress, String region1, String region2,
                            String region3, GeoPoint point, String category) {

        static NewPlace from(PlaceRegisterRequest req) {
            String kakaoId = required(req.getKakaoPlaceId(), 50);
            String name = required(req.getName(), 100);
            GeoPoint point = toPoint(req.getLatitude(), req.getLongitude());
            if (point == null) throw new BusinessException(ErrorCode.PLACE_INVALID);

            String[] parts = req.getAddressName() == null ? new String[0] : req.getAddressName().trim().split("\\s+");
            String r1 = required(firstNonBlank(req.getRegion1(), part(parts, 0)), 50);
            String r2 = required(firstNonBlank(req.getRegion2(), part(parts, 1)), 50);
            String r3 = required(firstNonBlank(req.getRegion3(), part(parts, 2)), 50);
            return new NewPlace(kakaoId, name, optional(req.getRoadAddress(), 255), r1, r2, r3, point,
                    optional(req.getCategory(), 50));
        }

        private static String required(String s, int max) {
            String v = optional(s, max);
            if (v == null) throw new BusinessException(ErrorCode.PLACE_INVALID);
            return v;
        }

        private static String optional(String s, int max) {
            if (s == null || s.isBlank()) return null;
            String v = s.trim();
            if (v.length() > max) throw new BusinessException(ErrorCode.PLACE_INVALID);
            return v;
        }

        private static String firstNonBlank(String a, String b) {
            return a != null && !a.isBlank() ? a : b;
        }

        private static String part(String[] parts, int idx) {
            return idx < parts.length ? parts[idx] : null;
        }
    }
}
