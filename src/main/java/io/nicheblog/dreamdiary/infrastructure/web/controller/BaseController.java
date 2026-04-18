package io.nicheblog.dreamdiary.infrastructure.web.controller;

import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;

/**
 * BaseController
 * <pre>
 *  (공통/상속) 기본 컨트롤러 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseController {

    /** actvtyCtgrCd */
    ActvtyCtgr getActvtyCtgr();

    /** baseUrl */
    default String getBaseUrl() {
        return null;
    };
}
