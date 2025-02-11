package io.nicheblog.dreamdiary.extension.clsf.managt.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;

/**
 * ManagtrService
 * <pre>
 *  조치자 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface ManagtrService {

    /**
     * 게시물 조치자 존재여부 (기 방문여부) 체크.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @return {@link Boolean} -- 사용자가 이미 방문했으면 true, 그렇지 않으면 false
     */
    Boolean hasAlreadyVisited(final BaseClsfKey refKey);

    /**
     * 게시물 조치자 등록.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    void addManagtr(final BaseClsfKey refKey);
}
