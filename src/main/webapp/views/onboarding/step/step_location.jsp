<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div class="location-container">
    <div class="step-indicator">Step 2 of 2</div>
    <div class="icon-wrapper">📍</div>
    <div class="title">위치 권한 안내</div>
    <div class="desc">
        내 주변의 <b>GPS 기반 퀘스트와 루틴 탐색</b>, 그리고 장소 인증 기능을 원활하게 사용하기 위해 위치 권한이 필요합니다.
    </div>

    <div class="btn-group">
        <button type="button" class="btn btn-allow" onclick="App.onboarding.requestLocationPermission()">위치 허용하기</button>
        <button type="button" class="btn btn-later" onclick="App.onboarding.skipLocationPermission()">나중에 하기</button>
    </div>
</div>
