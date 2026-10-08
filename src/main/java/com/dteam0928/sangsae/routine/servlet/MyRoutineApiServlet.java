package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.routine.dto.RoutineForm;
import com.dteam0928.sangsae.routine.dto.TaskForm;
import com.dteam0928.sangsae.routine.service.MyRoutineService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * R-00 루틴 관리 탭 API. 등록·수정은 form(POST), 여러 값은 같은 이름으로 반복해서 보낸다.
 * <pre>
 * GET    /api/my-routines                          첫 로딩 (루틴 셀렉트, 카테고리, 내 장소)
 * POST   /api/my-routines                          새 루틴
 * GET    /api/my-routines/{id}                     기본 정보
 * POST   /api/my-routines/{id}                     기본 정보 수정
 * DELETE /api/my-routines/{id}                     루틴 삭제(숨김)
 * GET    /api/my-routines/{id}/timeline?date=      타임테이블
 * GET    /api/my-routines/{id}/calendar?from=&amp;to=  캘린더 + 요약 카드
 * POST   /api/my-routines/{id}/tasks?date=         Task 등록   → 응답: date 의 타임테이블
 * POST   /api/my-routines/{id}/tasks/{taskId}?date= Task 수정
 * DELETE /api/my-routines/{id}/tasks/{taskId}?date= Task 삭제(숨김)
 * </pre>
 * 완료 체크는 팀원 API: POST /user-routines/tasks/complete
 */
@WebServlet(Routes.API_MY_ROUTINES + "/*")
public class MyRoutineApiServlet extends JsonApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        MyRoutineService service = app().myRoutineService();
        if (seg.isEmpty()) {
            respond(resp, () -> service.home(userId(req)));
            return;
        }
        if (!isId(seg.get(0)) || seg.size() > 2) {
            notFound(resp);
            return;
        }
        long id = Long.parseLong(seg.get(0));
        if (seg.size() == 1) {
            respond(resp, () -> service.detail(userId(req), id));
        } else if (seg.get(1).equals("timeline")) {
            respond(resp, () -> service.timeline(userId(req), id, param(req, "date")));
        } else if (seg.get(1).equals("calendar")) {
            respond(resp, () -> service.calendar(userId(req), id, param(req, "from"), param(req, "to")));
        } else {
            notFound(resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        MyRoutineService service = app().myRoutineService();
        if (seg.isEmpty()) {
            respond(resp, () -> service.create(userId(req), routineForm(req)));
            return;
        }
        if (!isId(seg.get(0))) {
            notFound(resp);
            return;
        }
        long id = Long.parseLong(seg.get(0));
        if (seg.size() == 1) {
            respond(resp, () -> service.update(userId(req), id, routineForm(req)));
        } else if (seg.size() == 2 && seg.get(1).equals("tasks")) {
            respond(resp, () -> service.addTask(userId(req), id, taskForm(req), param(req, "date")));
        } else if (seg.size() == 3 && seg.get(1).equals("tasks") && isId(seg.get(2))) {
            long taskId = Long.parseLong(seg.get(2));
            respond(resp, () -> service.updateTask(userId(req), id, taskId, taskForm(req), param(req, "date")));
        } else {
            notFound(resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        MyRoutineService service = app().myRoutineService();
        if (seg.isEmpty() || !isId(seg.get(0))) {
            notFound(resp);
            return;
        }
        long id = Long.parseLong(seg.get(0));
        if (seg.size() == 1) {
            respond(resp, () -> service.delete(userId(req), id));
        } else if (seg.size() == 3 && seg.get(1).equals("tasks") && isId(seg.get(2))) {
            long taskId = Long.parseLong(seg.get(2));
            respond(resp, () -> service.deleteTask(userId(req), id, taskId, param(req, "date")));
        } else {
            notFound(resp);
        }
    }

    private static RoutineForm routineForm(HttpServletRequest req) {
        return new RoutineForm(param(req, "title"), param(req, "startDate"), param(req, "endDate"),
                param(req, "visibility"), param(req, "description"), values(req, "categories"), values(req, "tags"),
                longParam(req, "representativePlaceId"));
    }

    private static TaskForm taskForm(HttpServletRequest req) {
        return new TaskForm(param(req, "content"), param(req, "time"), param(req, "category"),
                longParam(req, "placeId"), param(req, "repeatType"), values(req, "daysOfWeek"),
                param(req, "onceDate"), param(req, "alertTime"), param(req, "memo"));
    }

    private static List<String> values(HttpServletRequest req, String name) {
        String[] v = req.getParameterValues(name);
        return v == null ? List.of() : List.of(v);
    }
}
