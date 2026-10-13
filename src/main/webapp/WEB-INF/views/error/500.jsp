<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>500</title>
    <style>
        body {
            background-color: #EFFAFA;
            margin: 0;
            padding: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
            font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;
            color: #333;
        }
        .container {
            text-align: center;
        }
        h1 {
            font-size: 80px;
            margin: 10px 0;
            color: #2c3e50;
        }
        img {
            max-width: 200px;
            height: auto;
            margin: 20px 0;
        }
        p {
            font-size: 16px;
            color: #666;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>500</h1>
    <img src="${pageContext.request.contextPath}/assets/css/error.png" alt="에러 이미지">
    <p>서버 내부에서 예상치 못한 문제가 발생했습니다.</p>
</div>
</body>
</html>