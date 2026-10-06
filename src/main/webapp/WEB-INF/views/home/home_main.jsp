<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.io.*,java.nio.charset.StandardCharsets,java.util.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/views/common/home_model.jspf" %>
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

<c:choose>
    <c:when test="${empty home.userXpInfo.progressPercent}">
        <c:set var="xpPercent" value="${0}" />
    </c:when>
    <c:when test="${home.userXpInfo.progressPercent lt 0}">
        <c:set var="xpPercent" value="${0}" />
    </c:when>
    <c:when test="${home.userXpInfo.progressPercent gt 100}">
        <c:set var="xpPercent" value="${100}" />
    </c:when>
    <c:otherwise>
        <c:set var="xpPercent" value="${home.userXpInfo.progressPercent}" />
    </c:otherwise>
</c:choose>

<c:url var="homeCssUrl" value="/resources/css/home.css" />
<c:url var="dummyJsUrl" value="/resources/js/dummy.js" />
<c:url var="homeJsUrl" value="/resources/js/home.js" />

<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/views/common/head.jsp" %>

    <title><c:out value="${ui.pageTitle}" /></title>

    <link rel="stylesheet" href="<c:out value='${homeCssUrl}' />">

    <script src="<c:out value='${dummyJsUrl}' />" defer></script>
    <script src="<c:out value='${homeJsUrl}' />" defer></script>
</head>
<body data-page="home" data-context-path="${pageContext.request.contextPath}">

<%@ include file="/views/common/header.jsp" %>

<main class="container" id="home-dashboard">
    <div class="section-title">
        <span class="title" id="dashboard-title">
            <c:out value="${nickname}" /><c:out value="${ui.dashboardSuffix}" />
        </span>
    </div>

    <div class="dashboard-grid">
        <section class="dash-card xp-card">
            <div class="dash-card-header">
                <div class="dash-title">
                    <c:out value="${ui.xpTitle}" />
                </div>

                <span class="badge xp-badge" id="user-title-badge">
                    <c:out value="${home.userXpInfo.title}" />
                </span>
            </div>

            <div class="xp-content-group">
                <div class="xp-info-row">
                    <span class="stat-label">
                        <c:out value="${ui.totalXpLabel}" />
                    </span>

                    <div class="stat-num stat-num-accent">
                        <fmt:formatNumber
                                value="${home.userXpInfo.totalXp}"
                                type="number"
                                maxFractionDigits="0" />
                        <span class="xp-unit">
                            <c:out value="${ui.xpUnit}" />
                        </span>
                    </div>
                </div>

                <div class="xp-progress-bar-container"
                     role="progressbar"
                     aria-label="<c:out value='${ui.xpTitle}' />"
                     aria-valuemin="0"
                     aria-valuemax="100"
                     aria-valuenow="<c:out value='${xpPercent}' />">
                    <div class="xp-progress-bar"
                         id="xp-progress"
                         data-progress="<c:out value='${xpPercent}' />"
                         style="width: <c:out value='${xpPercent}' />%;"></div>
                </div>

                <div class="xp-next-target">
                    <span><c:out value="${ui.nextTitleLabel}" /></span>

                    <span class="text-accent-bold">
                        <fmt:formatNumber
                                value="${home.userXpInfo.remainingXp}"
                                type="number"
                                maxFractionDigits="0" />
                        <c:out value="${ui.remainingXpSuffix}" />
                    </span>
                </div>
            </div>
        </section>

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
                                           varStatus="status">
                                    <c:url var="editUrl" value="${card.editPath}">
                                        <c:param name="id" value="${item.id}" />
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

                                            <button type="button"
                                                    class="small-btn danger"
                                                    data-action="open-delete"
                                                    data-type="<c:out value='${card.typeLabel}' />"
                                                    data-name="<c:out value='${item.title}' />">
                                                <c:out value="${ui.deleteLabel}" />
                                            </button>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>

                            <c:otherwise>
                                <%-- 빈 상태 문구는 아래 공통 요소에서 출력합니다. --%>
                            </c:otherwise>
                        </c:choose>

                        <div class="empty-state"
                             data-empty-state
                             data-empty-text="<c:out value='${card.emptyText}' />"
                             data-filtered-empty-text="<c:out value='${card.filteredEmptyText}' />"
                             <c:if test="${not empty items}">is-hidden</c:if>>
                            <c:out value="${card.emptyText}" />
                        </div>
                    </div>
                </div>

                <a href="<c:out value='${addUrl}' />" class="detail-btn">
                    <c:out value="${card.addLabel}" />
                </a>
            </section>
        </c:forEach>

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

<%@ include file="/views/common/modal_delete.jsp" %>
</body>
</html>
