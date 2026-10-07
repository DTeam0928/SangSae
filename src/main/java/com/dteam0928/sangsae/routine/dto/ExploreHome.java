package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/** 루틴 탐색 탭 첫 화면 한 번에 내려주는 데이터 */
public record ExploreHome(LocationInfo location,
                          List<RoutineCategoryDto> categories,
                          List<PlaceCategoryDto> placeCategories,
                          List<NearbySpot> nearbySpots,
                          List<RoutineCard> trendingNearby,
                          List<RoutineCard> recommended,
                          List<RoutineCard> popular,
                          List<RoutineCard> latest,
                          List<PlaceSummary> myPlaces,
                          List<PlaceRoutine> myPlaceRoutines) {
}
