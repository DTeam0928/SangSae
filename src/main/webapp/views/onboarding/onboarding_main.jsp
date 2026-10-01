<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>일상을 새롭게, 상새</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/style-jsp.css">

    <style>
        body {
            background-color: var(--color-bg);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        #onboarding-viewport {
            width: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .step-section {
            display: none;
            width: 100%;
            animation: fadeIn 0.3s ease-in-out;
        }

        .step-section.active {
            display: block;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(6px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>

    <main id="onboarding-viewport">
        <!-- Step 1: 소셜 로그인 -->
        <section id="step-1" class="step-section active">
            <jsp:include page="/views/onboarding/step/step_social.jsp" />
        </section>

        <!-- Step 2: 이메일 로그인 / 회원가입 -->
        <section id="step-2" class="step-section">
            <jsp:include page="/views/onboarding/step/step_auth.jsp" />
        </section>

        <!-- Step 3: 프로필 & 관심사 -->
        <section id="step-3" class="step-section">
            <jsp:include page="/views/onboarding/step/step_profile.jsp" />
        </section>

        <!-- Step 4: 위치 권한 안내 -->
        <section id="step-4" class="step-section">
            <jsp:include page="/views/onboarding/step/step_location.jsp" />
        </section>
    </main>

    <script>
        function navigateToStep(stepNumber) {
            const sections = document.querySelectorAll('.step-section');
            sections.forEach(sec => sec.classList.remove('active'));

            const target = document.getElementById('step-' + stepNumber);
            if (target) {
                target.classList.add('active');
                window.scrollTo(0, 0);
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            navigateToStep(1);
        });
    </script>
</body>
</html>