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
</body>
</html>