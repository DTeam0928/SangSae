<!-- user-quest-list.jsp -->
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 퀘스트 일정</title>
</head>
<body>
    <h1>내 퀘스트 일정</h1>
    <p>일정에 등록된 퀘스트 수: ${userQuests.size()}</p>
    
    <hr>

    <h2>첫번째 일정</h2>
    <p>제목: ${userQuests[0].title}</p>
    <p>날짜: ${userQuests[0].scheduledDate}</p>
    <p>시간: ${userQuests[0].targetTime}</p>
    <p>상태: ${userQuests[0].status}</p>
    <p>보상XP: ${userQuests[0].rewardXp}</p>
</body>
</html>