package io.nicheblog.dreamdiary.global._common._clsf.viewer.service;

import io.nicheblog.dreamdiary.global._common._clsf.viewer.entity.ViewerEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;

/**
 * ViewerService
 * <pre>
 *  컨텐츠 열람자 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface ViewerService {

    /**
     * 열람자 존재 여부(기 방문 여부)를 체크합니다.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @return 이미 방문한 경우 true, 그렇지 않은 경우 false
     */
    ViewerEntity getViewerByHasVisitedChk(final BaseClsfKey refKey);

    /**
     * 열람자를 등록합니다.
     * 
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    void addViewer(final BaseClsfKey refKey);
}
