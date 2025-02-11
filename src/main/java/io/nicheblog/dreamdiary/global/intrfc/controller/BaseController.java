package io.nicheblog.dreamdiary.global.intrfc.controller;

import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;

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
