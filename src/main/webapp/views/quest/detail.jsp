<!-- detail.jsp -->
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
	<title>퀘스트 상세</title>
</head>
<body>
	<h1>퀘스트 상세</h1>
	<p>퀘스트ID: ${quest.id}</p>
	<p>제목: ${quest.title}</p>
	<p>설명: ${quest.description}</p>
	<p>카테고리: ${quest.category}</p>
	<p>예상시간: ${quest.estimatedMinutes}분</p>
	<p>보상XP: ${quest.rewardXp}</p>
	<p>인증방식: <strong>${quest.authType}</strong>
		<c:if test="${quest.authType eq 'LOCATION'}">
			<span style="color:#16a34a; font-weight:bold; font-size:12px; margin-left:6px;">📍 현장 반경 50m 이내 GPS 방문 인증 필수</span>
		</c:if>
	</p>
	
	<c:if test="${not empty place}">
		<div style="margin: 16px 0; padding: 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; max-width: 500px;">
			<h3 style="margin-bottom: 6px; font-size: 16px;">📍 퀘스트 추천 장소</h3>
			<p><strong>장소명:</strong> ${place.name()} <span style="background:#3b82f6; color:white; font-size:11px; padding:2px 6px; border-radius:10px;">${place.themeName()}</span></p>
			<p><strong>도로명:</strong> ${place.roadAddress()}</p>
			<p><strong>좌표:</strong> (${place.latitude()}, ${place.longitude()})</p>
			<div id="quest-mini-map" style="width: 100%; height: 220px; margin-top: 10px; border-radius: 6px; border: 1px solid #cbd5e1;"></div>
		</div>

		<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=<%= com.dteam0928.sangsae.common.util.ConfigUtil.getKakaoJsApiKey() != null ? com.dteam0928.sangsae.common.util.ConfigUtil.getKakaoJsApiKey() : "" %>&libraries=services,clusterer"></script>
		<script src="${pageContext.request.contextPath}/assets/js/mapUtil.js"></script>
		<script>
			window.addEventListener('DOMContentLoaded', function() {
				try {
					if (typeof MapUtil !== 'undefined') {
						var map = MapUtil.init('quest-mini-map', {
							lat: ${place.latitude()},
							lng: ${place.longitude()},
							level: 3
						});
						if (map) {
							MapUtil.addMarker(map, {
								lat: ${place.latitude()},
								lng: ${place.longitude()},
								title: '${place.name()}'
							});
						}
					}
				} catch(e) {
					console.warn("Kakao Map init skipped:", e);
				}
			});
		</script>
	</c:if>

	<hr>
	
	<h2>일정 추가 테스트</h2>
	<form action="${pageContext.request.contextPath}/quests" method="post">
	    <input type="hidden" name="templateId" value="${quest.id}">
	    <p>
	        사용자 ID:
	        <input type="number" name="userId" value="1">
	    </p>
	    <p>
	        여행 ID:
	        <input type="number" name="tripId" value="1">
	    </p>
	    <p>
	        날짜:
	        <input type="date" name="scheduledDate" value="2026-09-22">
	    </p>
	    <p>
	        시간:
	        <input type="time" name="targetTime" value="18:00">
	    </p>
	    <button type="submit">+ 일정 테스트</button>
	</form>
	
	<hr>

	<h2>저장 결과 확인</h2>
	<p>현재 일정에 담긴 퀘스트 수: ${userQuests.size()}</p>
</body>
</html>