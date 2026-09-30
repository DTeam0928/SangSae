<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dteam0928.sangsae.common.util.ConfigUtil" %>
<%
  String kakaoJsKey = ConfigUtil.getKakaoJsApiKey();
  boolean hasJsKey = (kakaoJsKey != null && !kakaoJsKey.isBlank() && !"YOUR_KAKAO_JAVASCRIPT_KEY".equals(kakaoJsKey));
  if (!hasJsKey) {
    kakaoJsKey = "YOUR_KAKAO_JAVASCRIPT_KEY";
  }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>상새(SangSae) - 공통 지도 및 장소 API 테스트 쇼케이스</title>

  <script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=<%= kakaoJsKey %>&libraries=services,clusterer"></script>
  <script src="${pageContext.request.contextPath}/js/mapUtil.js"></script>

  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
    body { background-color: #f1f5f9; color: #1e293b; padding: 24px; }
    .container { max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
    
    header { background: #ffffff; padding: 20px 24px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
    header h1 { font-size: 22px; color: #0f172a; margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }
    header p { font-size: 14px; color: #64748b; }
    
    .guide-banner {
      background-color: #eff6ff; border-left: 4px solid #3b82f6;
      padding: 12px 16px; border-radius: 6px; font-size: 13px; color: #1e40af; line-height: 1.5;
    }

    .main-grid { display: grid; grid-template-columns: 7fr 5fr; gap: 20px; }
    
    #map-wrapper {
      background: white; padding: 12px; border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06); position: relative;
    }
    #map { width: 100%; height: 580px; border-radius: 8px; }

    .control-panel { display: flex; flex-direction: column; gap: 16px; }
    .panel-card {
      background: white; padding: 18px; border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06); display: flex; flex-direction: column; gap: 12px;
    }
    .panel-card h3 { font-size: 15px; color: #1e293b; font-weight: 700; display: flex; align-items: center; justify-content: space-between; }
    
    .btn {
      padding: 9px 14px; border: none; border-radius: 6px; font-size: 13px;
      font-weight: 600; cursor: pointer; transition: all 0.2s; background: #3b82f6; color: white;
    }
    .btn:hover { background: #2563eb; }
    .btn-secondary { background: #64748b; }
    .btn-secondary:hover { background: #475569; }
    .btn-outline { background: white; border: 1px solid #cbd5e1; color: #334155; }
    .btn-outline:hover { background: #f8fafc; border-color: #94a3b8; }

    .input-group { display: flex; gap: 8px; }
    .input-group input {
      flex: 1; padding: 9px 12px; border: 1px solid #cbd5e1;
      border-radius: 6px; font-size: 13px; outline: none;
    }
    .input-group input:focus { border-color: #3b82f6; }

    .toggle-row { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #475569; }
    .toggle-row label { display: flex; align-items: center; gap: 6px; cursor: pointer; }

    .place-list {
      max-height: 240px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px;
    }
    .place-item {
      padding: 10px 12px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px;
      cursor: pointer; transition: all 0.15s; display: flex; flex-direction: column; gap: 4px;
    }
    .place-item:hover { border-color: #3b82f6; background: #eff6ff; }
    .place-item-header { display: flex; justify-content: space-between; align-items: center; }
    .place-item-name { font-weight: 700; font-size: 14px; color: #0f172a; }
    .place-item-addr { font-size: 12px; color: #64748b; }
    .place-item-dist { font-size: 11px; font-weight: 600; color: #3b82f6; }

    .badge {
      display: inline-block; padding: 2px 7px; border-radius: 12px;
      font-size: 10px; font-weight: 700; color: white;
    }
    .badge-FOOD { background-color: #ef4444; }
    .badge-CAFE { background-color: #f59e0b; }
    .badge-ACTIVITY { background-color: #10b981; }
    .badge-PHOTO { background-color: #ec4899; }
    .badge-WALK { background-color: #3b82f6; }
    .badge-STUDY { background-color: #8b5cf6; }
    .badge-CULTURE { background-color: #6366f1; }
    .badge-NATURE { background-color: #14b8a6; }

    .selected-box {
      background: #f0fdf4; border: 1px solid #86efac; border-radius: 8px;
      padding: 12px; font-size: 13px; color: #166534; line-height: 1.5;
    }
  </style>
</head>
<body>

<div class="container">
  <header>
    <h1>🗺️ 상새(SangSae) 공통 지도 & 장소 검색 쇼케이스</h1>
    <p>백엔드 프록시 서블릿(/api/map/search) + 상새 8대 테마 매핑 + MapUtil.js 연동 검증</p>
  </header>

  <div class="guide-banner">
    💡 <strong>안내:</strong> 백엔드 검색은 <code>.env</code>의 <strong>REST API 키</strong>를 사용하고,
    브라우저에 지도를 직접 띄우려면 <code>.env</code>에 <strong>KAKAO_JAVASCRIPT_KEY</strong>를 추가하시면 자동으로 연동됩니다!
  </div>

  <div class="main-grid">
    <div id="map-wrapper">
      <div id="map"></div>
      <% if (!hasJsKey) { %>
      <div style="position:absolute; top:24px; left:24px; right:24px; background:rgba(254, 242, 242, 0.95); border:1px solid #f87171; border-radius:10px; padding:16px 20px; font-size:13px; color:#991b1b; z-index:10; box-shadow:0 4px 6px -1px rgba(0,0,0,0.1); line-height:1.6;">
        <strong style="font-size:14px;">📍 카카오 지도(JavaScript 키) 연동 안내</strong><br>
        현재 백엔드 검색 API는 정상 작동 중이나, 브라우저에 실제 지도를 렌더링하려면 <strong>JavaScript 키</strong>가 필요합니다.<br>
        프로젝트 루트의 <code>.env</code> 파일에 아래 한 줄을 추가하고 새로고침(F5)하시면 지도가 바로 나타납니다:
        <pre style="background:#fee2e2; padding:8px 12px; border-radius:6px; margin-top:8px; font-family:monospace; font-weight:bold;">KAKAO_JAVASCRIPT_KEY=발급받으신_자바스크립트_키</pre>
      </div>
      <% } %>
    </div>

    <div class="control-panel">
      <div class="panel-card">
        <h3>📍 내 위치 (GPS)</h3>
        <div style="display:flex; gap:8px;">
          <button class="btn btn-secondary" style="flex:1;" onclick="locateUser()">내 위치 찾기</button>
          <button class="btn btn-outline" onclick="clearAllMarkers()">핀 모두 지우기</button>
        </div>
        <div id="gps-status" style="font-size:12px; color:#64748b;">위치 미조회</div>
      </div>

      <div class="panel-card">
        <h3>🔍 장소 검색 (/api/map/search)</h3>
        <div class="input-group">
          <input type="text" id="keywordInput" placeholder="장소명 입력 (예: 경복궁, 성수동 카페, 러닝코스)" value="경복궁" onkeydown="if(event.key==='Enter') searchPlaces()">
          <button class="btn" onclick="searchPlaces()">검색</button>
        </div>
        
        <div class="toggle-row">
          <label>
            <input type="checkbox" id="useLocationCheck" checked> 내 위치 반경(5km) 기준 검색
          </label>
          <label>
            <input type="checkbox" id="useClustererCheck"> 마커 클러스터러 적용
          </label>
        </div>

        <div id="place-results" class="place-list">
          <div style="font-size:13px; color:#94a3b8; text-align:center; padding:20px;">검색어를 입력하고 검색 버튼을 누르세요.</div>
        </div>
      </div>

      <div class="panel-card">
        <h3>✅ 선택된 장소 (퀘스트/루틴 저장용 DTO)</h3>
        <div id="selected-place-box" style="font-size:13px; color:#64748b;">
          지도 핀이나 목록에서 장소를 선택하세요.
        </div>
      </div>
    </div>
  </div>
</div>

<script>
  let mapInstance = null;
  let currentUserLocation = { lat: 37.5665, lng: 126.9780 };

  window.addEventListener('DOMContentLoaded', function () {
    try {
      mapInstance = MapUtil.init('map', {
        lat: currentUserLocation.lat,
        lng: currentUserLocation.lng,
        level: 4
      });
    } catch (e) {
      console.warn("Kakao SDK load error:", e);
    }
  });

  function locateUser() {
    var statusDiv = document.getElementById('gps-status');
    statusDiv.innerText = "GPS 위치 수신 중...";

    MapUtil.moveToCurrentLocation(mapInstance, function (pos) {
      currentUserLocation = pos;
      statusDiv.innerHTML = "위도: <b>" + pos.lat.toFixed(6) + "</b>, 경도: <b>" + pos.lng.toFixed(6) + "</b>";
      MapUtil.addMarker(mapInstance, {
        lat: pos.lat,
        lng: pos.lng,
        title: "📍 내 현재 위치"
      });
    }, function (err) {
      statusDiv.innerText = "위치 정보 획득 실패 (권한 허용 필요)";
    });
  }

  async function searchPlaces() {
    var query = document.getElementById('keywordInput').value.trim();
    if (!query) return;

    var useLocation = document.getElementById('useLocationCheck').checked;
    var useClusterer = document.getElementById('useClustererCheck').checked;
    var resultContainer = document.getElementById('place-results');
    resultContainer.innerHTML = '<div style="font-size:13px; color:#64748b; text-align:center; padding:15px;">검색 중...</div>';

    try {
      var searchParams = {
        query: query,
        radius: 5000
      };

      if (useLocation && currentUserLocation) {
        searchParams.lat = currentUserLocation.lat;
        searchParams.lng = currentUserLocation.lng;
      }

      var response = await MapUtil.searchPlaces(searchParams);

      if (!response.success || !response.data || response.data.length === 0) {
        resultContainer.innerHTML = '<div style="font-size:13px; color:#ef4444; text-align:center; padding:15px;">검색 결과가 없습니다.</div>';
        return;
      }

      renderResultList(response.data);
      MapUtil.renderPlaceMarkers(mapInstance, response.data, {
        useClusterer: useClusterer,
        onSelect: function (place) {
          selectPlace(place);
        }
      });

    } catch (err) {
      resultContainer.innerHTML = '<div style="font-size:12px; color:#ef4444; padding:12px;">오류: ' + (err.message || err) + '</div>';
    }
  }

  function renderResultList(places) {
    var container = document.getElementById('place-results');
    container.innerHTML = '';

    places.forEach(function (place, index) {
      var itemDiv = document.createElement('div');
      itemDiv.className = 'place-item';

      var distText = '';
      if (place.distanceInMeters != null && place.distanceInMeters > 0) {
        distText = place.distanceInMeters < 1000
          ? place.distanceInMeters + 'm'
          : (place.distanceInMeters / 1000).toFixed(1) + 'km';
      }

      var distSpan = distText ? '<span class="place-item-dist">📍 ' + distText + '</span>' : '';
      var addrText = place.roadAddress || place.address || '';

      itemDiv.innerHTML =
        '<div class="place-item-header">' +
          '<div style="display:flex; align-items:center; gap:6px;">' +
            '<span class="badge badge-' + place.themeCode + '">' + place.themeName + '</span>' +
            '<span class="place-item-name">' + (index + 1) + '. ' + place.name + '</span>' +
          '</div>' +
          distSpan +
        '</div>' +
        '<div class="place-item-addr">' + addrText + '</div>';

      itemDiv.addEventListener('click', function () {
        selectPlace(place);
        MapUtil.moveTo(mapInstance, place.latitude, place.longitude);
      });

      container.appendChild(itemDiv);
    });
  }

  async function selectPlace(place) {
    var box = document.getElementById('selected-place-box');
    box.className = 'selected-box';
    box.innerHTML = '<div style="color:#64748b;">DB 장소 저장 및 PK 발급 중...</div>';

    var addr = place.roadAddress || place.address || '';
    var regions = [place.region1Depth, place.region2Depth, place.region3Depth].filter(Boolean).join(' ');
    var distHtml = place.distanceInMeters ? '<br><small style="color:#15803d;">내 위치와의 거리: <b>' + place.distanceInMeters + 'm</b></small>' : '';

    try {
      var res = await MapUtil.selectPlace(place);
      var placeId = (res && res.data && res.data.placeId) ? res.data.placeId : '미발급';

      box.innerHTML =
        '<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">' +
          '<strong>' + place.name + '</strong>' +
          '<span style="background:#16a34a; color:white; font-size:11px; font-weight:bold; padding:2px 8px; border-radius:10px;">DB place_id: #' + placeId + '</span>' +
        '</div>' +
        '<small style="color:#15803d;">테마: ' + place.themeName + ' (' + place.themeCode + ')</small><br>' +
        '<small style="color:#15803d;">도로명: ' + addr + '</small><br>' +
        '<small style="color:#15803d;">행정구역: ' + regions + '</small><br>' +
        '<small style="color:#15803d;">좌표: (' + place.latitude.toFixed(6) + ', ' + place.longitude.toFixed(6) + ')</small>' +
        distHtml +
        '<div style="margin-top:8px; font-size:11px; color:#15803d; background:#dcfce7; padding:6px 8px; border-radius:4px;">' +
          '✅ DB 저장 완료! 퀘스트/루틴 생성 시 <code>place_id: ' + placeId + '</code>를 바로 사용할 수 있습니다.' +
        '</div>';
    } catch (e) {
      box.innerHTML =
        '<strong>' + place.name + '</strong><br>' +
        '<small style="color:#dc2626;">DB 저장 실패: ' + (e.message || e) + '</small>';
    }
  }

  function clearAllMarkers() {
    MapUtil.clearMarkers();
    document.getElementById('place-results').innerHTML = '<div style="font-size:13px; color:#94a3b8; text-align:center; padding:20px;">마커가 지워졌습니다.</div>';
  }
</script>

</body>
</html>
