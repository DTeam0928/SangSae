<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.io.*,java.nio.charset.StandardCharsets,java.util.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%!
    /*
     * 별도 JSON 라이브러리 없이 로컬 더미 JSON을 Map/List로 변환합니다.
     * 데이터 로딩만 Java로 처리하고 HTML 출력은 JSTL로 처리합니다.
     */
    private static final class HomeJsonReader {
        private final String source;
        private int position;

        HomeJsonReader(String source) {
            this.source = source;
        }

        Object parse() {
            Object result = readValue();
            skipWhitespace();

            if (position != source.length()) {
                throw error("Unexpected trailing content");
            }

            return result;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(
                    message + " at position " + position
            );
        }

        private void skipWhitespace() {
            while (position < source.length()) {
                char ch = source.charAt(position);

                if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
                    break;
                }

                position++;
            }
        }

        private boolean consume(char expected) {
            skipWhitespace();

            if (position < source.length()
                    && source.charAt(position) == expected) {
                position++;
                return true;
            }

            return false;
        }

        private void expect(char expected) {
            if (!consume(expected)) {
                throw error("Expected " + expected);
            }
        }

        private Object readValue() {
            skipWhitespace();

            if (position >= source.length()) {
                throw error("Unexpected end");
            }

            char ch = source.charAt(position);

            if (ch == '{') return readObject();
            if (ch == '[') return readArray();
            if (ch == '"') return readString();

            if (source.startsWith("true", position)) {
                position += 4;
                return Boolean.TRUE;
            }

            if (source.startsWith("false", position)) {
                position += 5;
                return Boolean.FALSE;
            }

            if (source.startsWith("null", position)) {
                position += 4;
                return null;
            }

            return readNumber();
        }

        private Map<String, Object> readObject() {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();

            if (consume('}')) return result;

            do {
                String key = readString();
                expect(':');

                if (result.containsKey(key)) {
                    throw error("Duplicate object key");
                }

                result.put(key, readValue());
            } while (consume(','));

            expect('}');
            return result;
        }

        private List<Object> readArray() {
            expect('[');
            List<Object> result = new ArrayList<>();

            if (consume(']')) return result;

            do {
                result.add(readValue());
            } while (consume(','));

            expect(']');
            return result;
        }

        private String readString() {
            expect('"');
            StringBuilder result = new StringBuilder();

            while (position < source.length()) {
                char ch = source.charAt(position++);

                if (ch == '"') return result.toString();

                if (ch < 0x20) {
                    throw error("Invalid control character");
                }

                if (ch != '\\') {
                    result.append(ch);
                    continue;
                }

                if (position >= source.length()) {
                    throw error("Incomplete escape");
                }

                char escaped = source.charAt(position++);

                switch (escaped) {
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'b': result.append('\b'); break;
                    case 'f': result.append('\f'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case 'u':
                        if (position + 4 > source.length()) {
                            throw error("Incomplete unicode escape");
                        }

                        String hex = source.substring(position, position + 4);

                        if (!hex.matches("[0-9a-fA-F]{4}")) {
                            throw error("Invalid unicode escape");
                        }

                        result.append((char) Integer.parseInt(hex, 16));
                        position += 4;
                        break;
                    default:
                        throw error("Invalid escape");
                }
            }

            throw error("Unterminated string");
        }

        private Number readNumber() {
            int start = position;

            while (position < source.length()) {
                char ch = source.charAt(position);

                if (ch == ',' || ch == ']' || ch == '}'
                        || Character.isWhitespace(ch)) {
                    break;
                }

                position++;
            }

            String token = source.substring(start, position);

            if (!token.matches(
                    "-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")) {
                throw error("Invalid number");
            }

            return new java.math.BigDecimal(token);
        }
    }
%>

<%
    /*
     * 외부에서 완성된 homeModel을 전달하면 그 모델을 우선 사용합니다.
     * 모델이 없을 때만 /resources/js/dummy.js를 읽습니다.
     */
    if (request.getAttribute("homeModel") == null) {
        String resourcePath = "/resources/js/dummy.js";
        String startMarker = "/* HOME_MODEL_START */";
        String endMarker = "/* HOME_MODEL_END */";
        StringBuilder source = new StringBuilder();

        InputStream input = application.getResourceAsStream(resourcePath);

        if (input == null) {
            throw new IllegalStateException(
                    "Dummy resource not found: " + resourcePath
            );
        }

        try (Reader reader = new InputStreamReader(
                input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int count;

            while ((count = reader.read(buffer)) != -1) {
                source.append(buffer, 0, count);
            }
        }

        String text = source.toString();
        int start = text.indexOf(startMarker);

        if (start < 0) {
            throw new IllegalStateException("Dummy JSON start marker missing");
        }

        start += startMarker.length();
        int end = text.indexOf(endMarker, start);

        if (end < 0) {
            throw new IllegalStateException("Dummy JSON end marker missing");
        }

        Object parsed = new HomeJsonReader(
                text.substring(start, end).trim()
        ).parse();

        if (!(parsed instanceof Map)) {
            throw new IllegalStateException("homeModel must be an object");
        }

        request.setAttribute("homeModel", parsed);
    }
%>

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

<c:url var="commonCssUrl" value="/resources/css/style-jsp.css" />
<c:url var="homeCssUrl" value="/resources/css/home.css" />
<c:url var="dummyJsUrl" value="/resources/js/dummy.js" />
<c:url var="homeJsUrl" value="/resources/js/home.js" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title><c:out value="${ui.pageTitle}" /></title>

    <link rel="stylesheet" href="<c:out value='${commonCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${homeCssUrl}' />">

    <script src="<c:out value='${dummyJsUrl}' />" defer></script>
    <script src="<c:out value='${homeJsUrl}' />" defer></script>
</head>
<body>

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
                             <c:if test="${not empty items}">hidden</c:if>>
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

<div class="modal-backdrop"
     id="deleteModal"
     aria-hidden="true"
     data-title-template="<c:out value='${ui.modalTitle}' />"
     data-description-template="<c:out value='${ui.modalDescription}' />">
    <div class="modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="modalTitle"
         aria-describedby="modalDesc">
        <div class="modal-title" id="modalTitle"></div>
        <div class="modal-desc" id="modalDesc"></div>

        <div class="modal-btns">
            <button type="button"
                    class="modal-btn btn-cancel"
                    data-action="cancel-delete">
                <c:out value="${ui.cancelLabel}" />
            </button>

            <button type="button"
                    class="modal-btn btn-confirm"
                    data-action="confirm-delete">
                <c:out value="${ui.confirmLabel}" />
            </button>
        </div>
    </div>
</div>

</body>
</html>