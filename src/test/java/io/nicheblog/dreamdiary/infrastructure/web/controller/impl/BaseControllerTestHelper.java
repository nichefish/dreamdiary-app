package io.nicheblog.dreamdiary.infrastructure.web.controller.impl;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

/**
 * BaseControllerTestHelper
 * <pre>
 *  테스트 컨트롤러 기본속성 세팅 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
@Log4j2
public class BaseControllerTestHelper {

    /**
     * 뷰 템플릿 파일 존재여부 체크
     * 템플릿 파일 경로는 "templates/{viewName}.ftlh"로 구성됩니다.
     *
     * @param viewName 확인할 뷰 템플릿 파일의 이름
     * @return {@link Boolean} -- 템플릿 파일이 존재하면 true, 존재하지 않으면 false
     */
    public boolean viewFileExists(final String viewName) {
        final String viewPath = "templates" + viewName + ".ftlh";
        log.info("viewPath: {}", viewPath);
        final File file = new File(viewPath);
        return file.exists();
    }
}
