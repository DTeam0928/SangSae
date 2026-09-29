<!-- list.jsp -->
<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>퀘스트 탐색</title>
</head>
<body>
    <h1>퀘스트 탐색</h1>

    <p>퀘스트 개수: ${quests.size()}</p>

    <p>첫 번째 퀘스트: ${quests[0].title}</p>
    <p>카테고리: ${quests[0].category}</p>
    <p>보상 XP: ${quests[0].rewardXp}</p>
</body>
</html>