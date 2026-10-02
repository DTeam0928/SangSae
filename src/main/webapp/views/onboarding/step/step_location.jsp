<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<head>
    <title>onboarding</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/style-jsp.css">
    <style>
    /* ===== Step 4 (위치 권한) 고유 스타일 ===== */
    .location-container {
        width: min(440px, calc(100% - 40px));
        margin: 0 auto;
        background: var(--color-white);
        padding: 40px 30px;
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-card);
        border: 1px solid var(--color-border);
    }

    .location-container .step-indicator {
        font-size: var(--fs-small);
        font-weight: 700;
        color: var(--color-primary);
        margin-bottom: 8px;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        text-align: center;
    }

    .icon-wrapper {
        width: 72px;
        height: 72px;
        background: var(--color-main);
        color: var(--color-primary);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32px;
        margin: 0 auto 24px;
    }

    .location-container .title {
        font-size: var(--fs-h1);
        font-weight: var(--fw-h1);
        margin-bottom: 6px;
        text-align: center;
    }

    .location-container .desc {
        font-size: var(--fs-body);
        color: var(--color-muted);
        margin-bottom: 28px;
        text-align: center;
    }

    .location-container .btn-group {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .location-container .btn-group .btn {
        width: 100%;
        height: 50px;
        margin-top: 0;
        transition: background-color 0.2s;
        cursor: pointer;
    }

    .btn-allow {
        background-color: var(--color-primary);
        border: none;
        color: var(--color-white);
    }

    .btn-allow:hover {
        background-color: var(--color-dark);
    }

    .btn-later {
        background: var(--color-white);
        border: 1px solid var(--color-border);
        color: var(--color-muted);
    }

    .btn-later:hover {
        background: #f8fafc;
    }
    </style>
</head>


<div class="location-container">
    <div class="step-indicator">Step 2 of 2</div>
    <div class="icon-wrapper">📍</div>
    <div class="title">위치 권한 안내</div>
    <div class="desc">
        내 주변의 <b>GPS 기반 퀘스트와 루틴 탐색</b>, 그리고 장소 인증 기능을 원활하게 사용하기 위해 위치 권한이 필요합니다.
    </div>

    <div class="btn-group">
        <button type="button" class="btn btn-allow" onclick="requestLocationPermission()">위치 허용하기</button>
        <button type="button" class="btn btn-later" onclick="skipLocationPermission()">나중에 하기</button>
    </div>
</div>

<script>
    function requestLocationPermission() {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    alert('위치 권한이 허용되었습니다. 홈 화면으로 이동합니다.');
                    location.href = '${pageContext.request.contextPath}/views/home/home_main.jsp';
                },
                function(error) {
                    console.warn(`ERROR(${error.code}): ${error.message}`);
                    alert('위치 권한이 거부되었거나 가져올 수 없습니다. 수동 지역 선택 모드로 홈 화면으로 이동합니다.');
                    location.href = '${pageContext.request.contextPath}/views/home/home_main.jsp';
                },
                { timeout: 10000, maximumAge: 0 }
            );
        } else {
            alert('이 브라우저는 위치 정보를 지원하지 않습니다. 홈 화면으로 이동합니다.');
            location.href = '${pageContext.request.contextPath}/views/home/home_main.jsp';
        }
    }

    function skipLocationPermission() {
        alert('위치 권한 설정을 건너뛰었습니다. 지도 및 퀘스트 탐색 시 수동으로 지역을 선택할 수 있습니다.');
        location.href = '${pageContext.request.contextPath}/views/home/home_main.jsp';
    }
</script>