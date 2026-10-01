package com.dteam0928.sangsae.common.web;

import com.dteam0928.sangsae.common.exception.BusinessException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSP 인라인 에러 표시용.
 * JSP 에서 ${errors.email}, ${errors.password}, ${errors.global} 처럼 꺼내 쓴다.
 */
public final class FormErrors {

    public static final String ATTR = "errors";

    private FormErrors() {
    }

    public static Map<String, String> of(BusinessException e) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(e.getField(), e.getMessage());
        return map;
    }
}
