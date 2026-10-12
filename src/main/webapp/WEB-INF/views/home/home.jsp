<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="home" value="${requestScope.homeModel}" />
<c:set var="ui" value="${home.ui}" />
<fmt:setLocale value="ko_KR" />

<c:choose>
    <c:when test="${not empty sessionScope.nickname}">
        <c:set var="nickname" value="${sessionScope.nickname}" />
    </c:when>
    <c:otherwise>
        <c:set var="nickname" value="${home.nickname}" />
    </c:otherwise>
</c:choose>

<!-- 1. 루틴 성공 비율로 퍼센트 계산 -->
<c:choose>
    <c:when test="${empty home.dailyAggregate or empty home.dailyAggregate.routineTotalCount or home.dailyAggregate.routineTotalCount eq 0}">
        <c:set var="xpPercent" value="0" />
    </c:when>
    <c:otherwise>
        <c:set var="xpPercent" value="${(home.dailyAggregate.routineSuccessCount / home.dailyAggregate.routineTotalCount) * 100}" />
    </c:otherwise>
</c:choose>

<c:url var="homeCssUrl" value="/assets/css/home.css" />
<c:url var="homeJsUrl" value="/assets/js/home.js" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/common/head.jsp" %>

    <title><c:out value="${ui.pageTitle}" /></title>

    <link rel="stylesheet" href="<c:out value='${homeCssUrl}' />">

    <script src="<c:out value='${homeJsUrl}' />" defer></script>
</head>
<body data-page="home" data-context-path="${pageContext.request.contextPath}">

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="container" id="home-dashboard">
    <c:if test="${empty home}"><p role="status">홈 데이터를 불러오지 못했습니다.</p></c:if>
    <div class="section-title">
        <span class="title" id="dashboard-title">
            <c:out value="${nickname}" /><c:out value="${ui.dashboardSuffix}" />
        </span>
    </div>

    <div class="dashboard-grid">
        <!-- 2. XP 카드 렌더링 -->
        <section class="dash-card xp-card">
            <div class="dash-card-header">
                <div class="dash-title">
                    <c:out value="${not empty homeModel.ui.xpTitle ? homeModel.ui.xpTitle : ui.xpTitle}" default="나의 칭호" />
                </div>

                <span class="badge xp-badge" id="user-title-badge">
                    🔥 연속 <c:out value="${empty home.dailyAggregate.currentStreak ? 0 : home.dailyAggregate.currentStreak}" />일
                </span>
            </div>

            <div class="xp-content-group">
                <div class="xp-info-row">
                    <span class="stat-label">
                        오늘 획득한 XP (퀘스트: <c:out value="${empty home.dailyAggregate.questSuccessCount ? 0 : home.dailyAggregate.questSuccessCount}"/>개)
                    </span>

                    <div class="stat-num stat-num-accent">
                        <fmt:formatNumber
                                value="${empty home.dailyAggregate.earnedXp ? 0 : home.dailyAggregate.earnedXp}"
                                type="number"
                                maxFractionDigits="0" />
                        <span class="xp-unit">
                            <c:out value="${ui.xpUnit}" default="XP" />
                        </span>
                    </div>
                </div>

                <div class="xp-progress-bar-container"
                     role="progressbar"
                     aria-label="오늘의 진행도"
                     aria-valuemin="0"
                     aria-valuemax="100"
                     aria-valuenow="<c:out value='${xpPercent}' />">
                    <div class="xp-progress-bar"
                         id="xp-progress"
                         data-progress="<c:out value='${xpPercent}' />"
                         style="width: <c:out value='${xpPercent}' />%;"></div>
                </div>

                <div class="xp-next-target">
                    <span>루틴 완료 현황</span>

                    <span class="text-accent-bold">
                        <c:out value="${empty home.dailyAggregate.routineSuccessCount ? 0 : home.dailyAggregate.routineSuccessCount}" /> /
                        <c:out value="${empty home.dailyAggregate.routineTotalCount ? 0 : home.dailyAggregate.routineTotalCount}" />개
                    </span>
                </div>
            </div>
        </section>

        <c:forEach var="card" items="${ui.statsCards}">
            <section class="dash-card">
                <div class="dash-title">
                    <c:out value="${card.title}" />
                </div>

                <div class="stats-row">
                    <c:forEach var="stat" items="${card.items}">
                        <div>
                            <div class="stat-label">
                                <c:out value="${stat.label}" />
                            </div>

                            <div class="stat-num${stat.accent ? ' stat-num-accent' : ''}">
                                <fmt:formatNumber
                                        value="${home.stats[stat.key]}"
                                        type="number"
                                        maxFractionDigits="0" /><c:out value="${stat.unit}" />
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </section>
        </c:forEach>

        <!-- 3. 관리 카드 렌더링 -->
        <c:forEach var="card" items="${ui.managementCards}">
            <c:set var="items" value="${home[card.listKey]}" />
            <c:url var="addUrl" value="${card.addPath}" />

            <section class="dash-card"
                     data-management-card="<c:out value='${card.key}' />">
                <div>
                    <div class="dash-card-header">
                        <div class="dash-title">
                            <c:out value="${card.title}" />
                        </div>

                        <select class="badge select-badge"
                                id="<c:out value='${card.key}' />-filter"
                                data-list-filter
                                aria-label="<c:out value='${card.title}' />">
                            <c:forEach var="filter" items="${card.filters}">
                                <option value="<c:out value='${filter.value}' />">
                                    <c:out value="${filter.label}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="list-container"
                         id="<c:out value='${card.key}' />-content">
                        <c:choose>
                            <c:when test="${not empty items}">
                                <c:forEach var="item"
                                           items="${items}"
                                           varStatus="status"
                                           begin="0"
                                           end="4">
                                    <c:url var="editUrl" value="${card.editPath}">
									    <c:if test="${card.key ne 'trip'}">
									        <c:param name="id" value="${item.id}" />
									    </c:if>
									</c:url>

                                    <div class="item-row"
                                         data-item-row
                                         data-id="<c:out value='${item.id}' />"
                                         data-start-date="<c:out value='${item.startDate}' />"
                                         data-end-date="<c:out value='${item.endDate}' />"
                                         data-status="<c:out value='${item.status}' />">
                                        <div class="item-left-group">
                                            <span class="item-number">
                                                <c:out value="${status.count}" />
                                            </span>

                                            <div>
                                                <div class="subtitle">
                                                    <c:out value="${item.title}" />
                                                </div>

                                                <div class="desc item-subtext">
                                                    <c:out value="${item.startDate}" /><c:out value="${ui.dateSeparator}" /><c:out value="${item.endDate}" />
                                                </div>
                                            </div>
                                        </div>

                                        <div class="item-btn-group">
                                            <a class="small-btn"
                                               href="<c:out value='${editUrl}' />#<c:out value='${card.editFragment}' />">
                                                <c:out value="${ui.editLabel}" />
                                            </a>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                        </c:choose>

                        <div class="empty-state"
                             data-empty-state
                             data-empty-text="<c:out value='${card.emptyText}' />"
                             data-filtered-empty-text="<c:out value='${card.filteredEmptyText}' />"
                             <c:if test="${not empty items}">hidden</c:if>>
                            <c:out value="${card.emptyText}" />
                        </div>
                    </div>
                </div>

                <!-- 5개 초과: 전체보기 / 5개 이하: 추가하기 -->
                <c:choose>
                    <c:when test="${not empty items and items.size() > 5}">
                        <a href="<c:out value='${addUrl}' />#<c:out value='${card.editFragment}' />" class="detail-btn">
                            전체보기 (${items.size()}개) →
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="<c:out value='${addUrl}' />" class="detail-btn">
                            <c:out value="${card.addLabel}" />
                        </a>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:forEach>
    </div>

    <section class="extra-section">
        <div class="section-title">
            <span><c:out value="${ui.extraTitle}" /></span>
        </div>

        <div class="dashboard-grid">
            <c:forEach var="card" items="${ui.featureCards}">
                <c:set var="feature" value="${home[card.dataKey]}" />
                <c:url var="featureUrl" value="${card.path}" />

                <section class="full-card card-flex-col">
                    <div>
                        <div class="card-header-flex">
                            <span class="badge${card.key eq 'community' ? ' orange' : ''}">
                                <c:out value="${card.badgeLabel}" />
                            </span>

                            <span class="desc${card.key eq 'quest' ? ' text-accent-bold' : ''}">
                                <c:out value="${feature[card.metaKey]}" />
                            </span>
                        </div>

                        <c:choose>
                            <c:when test="${not empty feature}">
                                <div class="card-title-gap subtitle">
                                    <c:out value="${feature.title}" />
                                </div>

                                <p class="desc">
                                    <c:out value="${feature[card.bodyKey]}" />
                                </p>
                            </c:when>

                            <c:otherwise>
                                <div class="empty-state">
                                    <c:out value="${card.emptyText}" />
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <a href="<c:out value='${featureUrl}' />"
                       class="detail-btn${card.key eq 'community' ? ' btn-community' : ''}">
                        <c:out value="${card.linkLabel}" />
                    </a>
                </section>
            </c:forEach>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/common/modal_delete.jsp" %>
</body>
</html>