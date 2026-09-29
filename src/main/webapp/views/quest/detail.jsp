<!-- detail.jsp -->
<%@ page contentType="text/html; charset=UTF-8"%>
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
	<p>인증방식: ${quest.authType}</p>
	
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