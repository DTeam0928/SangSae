package com.dteam0928.sangsae.web.listener;

import com.dteam0928.sangsae.AppContext;
import com.dteam0928.sangsae.common.config.AppConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** 앱 시작 시 AppContext 생성, 종료 시 커넥션 풀 정리 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppContext appContext = new AppContext(new AppConfig());
        sce.getServletContext().setAttribute(AppContext.ATTRIBUTE, appContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AppContext appContext = AppContext.from(sce.getServletContext());
        if (appContext != null) appContext.close();

    }
}
