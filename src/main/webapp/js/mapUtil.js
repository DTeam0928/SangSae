const MapUtil = (function () {
    'use strict';

    let activeMarkers = [];
    let currentClusterer = null;
    let currentInfoWindow = null;

    const THEME_COLORS = {
        FOOD: '#ef4444',
        CAFE: '#f59e0b',
        ACTIVITY: '#10b981',
        PHOTO: '#ec4899',
        WALK: '#3b82f6',
        STUDY: '#8b5cf6',
        CULTURE: '#6366f1',
        NATURE: '#14b8a6'
    };

    function init(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) {
            console.error(`[MapUtil] Target element '${containerId}' not found.`);
            return null;
        }

        if (typeof kakao === 'undefined' || !kakao.maps) {
            console.error('[MapUtil] Kakao Maps SDK is not loaded.');
            return null;
        }

        const centerLat = options.lat || 37.5665;
        const centerLng = options.lng || 126.9780;
        const level = options.level || 3;

        const map = new kakao.maps.Map(container, {
            center: new kakao.maps.LatLng(centerLat, centerLng),
            level: level
        });

        if (options.showControls !== false) {
            map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
            map.addControl(new kakao.maps.MapTypeControl(), kakao.maps.ControlPosition.TOPRIGHT);
        }

        return map;
    }

    function moveTo(map, lat, lng) {
        if (!map) return;
        map.panTo(new kakao.maps.LatLng(lat, lng));
    }

    function moveToCurrentLocation(map, onSuccess, onError) {
        if (!navigator.geolocation) {
            alert('이 브라우저는 위치 정보(GPS)를 지원하지 않습니다.');
            return;
        }

        navigator.geolocation.getCurrentPosition(
            function (position) {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;
                if (map) {
                    moveTo(map, lat, lng);
                }

                if (typeof onSuccess === 'function') {
                    onSuccess({ lat, lng });
                }
            },
            function (error) {
                console.warn('[MapUtil] GPS error:', error.message);
                if (typeof onError === 'function') {
                    onError(error);
                } else {
                    alert('현재 위치 정보를 가져올 수 없습니다. 브라우저 위치 권한을 확인하세요.');
                }
            },
            // 퀘스트 위치 인증 시 캐시된 좌표 재사용으로 인한 위치 스푸핑/부정 인증을 방지하기 위해 maximumAge: 0 및 고정밀 GPS 강제.
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
        );
    }

    function addMarker(map, item, onClick) {
        if (!map || !item || !item.lat || !item.lng) return null;

        const position = new kakao.maps.LatLng(item.lat, item.lng);
        const marker = new kakao.maps.Marker({
            position: position,
            map: map,
            title: item.title || ''
        });

        if (item.content || item.title) {
            const htmlContent = item.content || `
                <div style="padding:8px 12px; font-size:13px; font-weight:bold; white-space:nowrap; border-radius:4px;">
                    ${item.title}
                </div>`;

            const infowindow = new kakao.maps.InfoWindow({
                content: htmlContent,
                removable: true
            });

            kakao.maps.event.addListener(marker, 'click', function () {
                if (currentInfoWindow) {
                    currentInfoWindow.close();
                }
                infowindow.open(map, marker);
                currentInfoWindow = infowindow;

                if (typeof onClick === 'function') {
                    onClick(item);
                }
            });
        } else if (typeof onClick === 'function') {
            kakao.maps.event.addListener(marker, 'click', function () {
                onClick(item);
            });
        }

        activeMarkers.push(marker);
        return marker;
    }

    function addMarkers(map, items = [], onClick) {
        if (!map || !Array.isArray(items) || items.length === 0) return [];

        const markers = [];
        const bounds = new kakao.maps.LatLngBounds();

        items.forEach(item => {
            const marker = addMarker(map, item, onClick);
            if (marker) {
                markers.push(marker);
                bounds.extend(new kakao.maps.LatLng(item.lat, item.lng));
            }
        });

        if (markers.length > 0) {
            map.setBounds(bounds);
        }

        return markers;
    }

    function calculateDistance(lat1, lon1, lat2, lon2) {
        const EARTH_RADIUS_METERS = 6371000;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_METERS * c);
    }

    async function searchPlaces(params = {}) {
        if (!params.query || !params.query.trim()) {
            return { success: false, count: 0, data: [], message: '검색어를 입력해 주세요.' };
        }

        const queryParams = new URLSearchParams();
        queryParams.set('query', params.query.trim());
        if (params.type) queryParams.set('type', params.type);
        if (params.lat != null) queryParams.set('lat', params.lat);
        if (params.lng != null) queryParams.set('lng', params.lng);
        if (params.radius != null) queryParams.set('radius', params.radius);

        const response = await fetch(`/api/map/search?${queryParams.toString()}`);
        if (!response.ok) {
            const errorPayload = await response.json().catch(() => null);
            throw new Error((errorPayload && errorPayload.message) || `서버 통신 오류 (HTTP ${response.status})`);
        }
        return await response.json();
    }

    async function searchViaServer(type, query) {
        return searchPlaces({ type, query });
    }

    // 프론트에서 장소 선택 확정 시 백엔드 DB(places)에 Get-or-Create 저장하고 발급된 placeId를 즉시 획득.
    async function selectPlace(placeDto) {
        if (!placeDto) {
            throw new Error('선택할 장소 정보가 없습니다.');
        }

        const response = await fetch('/api/place/select', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            },
            body: JSON.stringify(placeDto)
        });

        if (!response.ok) {
            const err = await response.json().catch(() => null);
            throw new Error((err && err.message) || `장소 저장 실패 (HTTP ${response.status})`);
        }

        return await response.json();
    }

    function renderPlaceMarkers(map, placeList = [], options = {}) {
        if (!map) return [];

        if (options.clearPrevious !== false) {
            clearMarkers();
        }

        if (!Array.isArray(placeList) || placeList.length === 0) {
            return [];
        }

        const markers = [];
        const bounds = new kakao.maps.LatLngBounds();

        let clusterer = null;
        if (options.useClusterer && typeof kakao.maps.MarkerClusterer !== 'undefined') {
            if (!currentClusterer) {
                currentClusterer = new kakao.maps.MarkerClusterer({
                    map: map,
                    averageCenter: true,
                    minLevel: 6
                });
            }
            clusterer = currentClusterer;
        }

        placeList.forEach(place => {
            const lat = place.latitude;
            const lng = place.longitude;
            if (!lat || !lng) return;

            const position = new kakao.maps.LatLng(lat, lng);
            const marker = new kakao.maps.Marker({
                position: position,
                map: clusterer ? null : map,
                title: place.name
            });

            const themeColor = THEME_COLORS[place.themeCode] || '#3b82f6';
            let distanceHtml = '';
            if (place.distanceInMeters != null && place.distanceInMeters > 0) {
                const distText = place.distanceInMeters < 1000
                    ? `${place.distanceInMeters}m`
                    : `${(place.distanceInMeters / 1000).toFixed(1)}km`;
                distanceHtml = `<span style="font-size:11px; color:#64748b; font-weight:normal; margin-left:6px;">📍 ${distText}</span>`;
            }

            const infoContent = document.createElement('div');
            infoContent.style.cssText = 'padding:12px 14px; min-width:220px; max-width:280px; font-family:Pretendard,sans-serif; font-size:13px; line-height:1.4;';
            infoContent.innerHTML = `
                <div style="margin-bottom:6px; display:flex; align-items:center; justify-content:space-between;">
                    <span style="background:${themeColor}; color:white; font-size:10px; font-weight:700; padding:2px 6px; border-radius:10px;">
                        ${place.themeName || '#장소'}
                    </span>
                    ${distanceHtml}
                </div>
                <div style="font-weight:700; font-size:14px; color:#0f172a; margin-bottom:4px;">
                    ${place.name}
                </div>
                <div style="font-size:11px; color:#64748b; margin-bottom:8px;">
                    ${place.roadAddress || place.address || ''}
                </div>
                <div style="display:flex; gap:6px;">
                    <button type="button" class="btn-select-place" style="flex:1; background:#3b82f6; color:white; border:none; padding:5px 8px; border-radius:4px; font-size:11px; font-weight:600; cursor:pointer;">
                        이 장소 선택
                    </button>
                    ${place.placeUrl ? `<a href="${place.placeUrl}" target="_blank" style="display:inline-block; background:#f1f5f9; color:#475569; text-decoration:none; padding:5px 8px; border-radius:4px; font-size:11px; font-weight:600;">상세</a>` : ''}
                </div>
            `;

            const selectBtn = infoContent.querySelector('.btn-select-place');
            selectBtn.addEventListener('click', function (e) {
                // 장소 선택 버튼 클릭 시 부모 지도 레이어로 클릭 이벤트가 전파되어 인포윈도우가 닫히거나 지도가 클릭되는 현상 방지.
                e.stopPropagation();
                if (typeof options.onSelect === 'function') {
                    options.onSelect(place);
                }
            });

            const infowindow = new kakao.maps.InfoWindow({
                content: infoContent,
                removable: true
            });

            kakao.maps.event.addListener(marker, 'click', function () {
                if (currentInfoWindow) {
                    currentInfoWindow.close();
                }
                infowindow.open(map, marker);
                currentInfoWindow = infowindow;

                if (typeof options.onSelect === 'function') {
                    options.onSelect(place);
                }
            });

            markers.push(marker);
            activeMarkers.push(marker);
            bounds.extend(position);
        });

        if (clusterer) {
            clusterer.addMarkers(markers);
        }

        if (markers.length > 0) {
            map.setBounds(bounds);
        }

        return markers;
    }

    function clearMarkers() {
        if (currentInfoWindow) {
            currentInfoWindow.close();
            currentInfoWindow = null;
        }

        activeMarkers.forEach(marker => {
            marker.setMap(null);
        });
        activeMarkers = [];

        if (currentClusterer) {
            currentClusterer.clear();
        }
    }

    return {
        init: init,
        moveTo: moveTo,
        moveToCurrentLocation: moveToCurrentLocation,
        addMarker: addMarker,
        addMarkers: addMarkers,
        calculateDistance: calculateDistance,
        searchPlaces: searchPlaces,
        searchViaServer: searchViaServer,
        selectPlace: selectPlace,
        renderPlaceMarkers: renderPlaceMarkers,
        clearMarkers: clearMarkers
    };
})();
