package io.nicheblog.dreamdiary.feature.clsf.history.model;

import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;

/**
 * HistoryActionModule
 * <pre>
 *  쓰기 요청 시 이력 타입 메타데이터 전달용 모듈.
 * </pre>
 */
public interface HistoryActionModule {

    String getHistoryType();

    Integer getFromHistoryNo();

    default HistoryType resolveHistoryType() {
        return HistoryType.from(this.getHistoryType());
    }
}
