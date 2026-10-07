package com.dteam0928.sangsae.routine.dto;

import com.dteam0928.sangsae.routine.entity.RoutineTemplate;
import com.dteam0928.sangsae.routine.entity.RoutineTemplateTask;
import java.util.List;

public record RoutineTemplateDetailDto(
    RoutineTemplate template,
    List<RoutineTemplateTask> tasks
) {
}
