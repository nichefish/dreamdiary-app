package io.nicheblog.dreamdiary.feature.journal.thread.model;

/**
 * JournalThreadMembershipTagProjection
 * <pre>
 *  스레드 목록 enrich 용 소속 엔트리 태그 Projection.
 *  스레드별 태그 합집합을 엔트리 DTO 풀 로드 없이 조회한다.
 * </pre>
 *
 * @author nichefish
 */
public interface JournalThreadMembershipTagProjection {

    /** 스레드 ID */
    Integer getThreadId();

    /** 태그 ID */
    Integer getTagId();

    /** 태그 이름 */
    String getName();

    /** 태그 카테고리 */
    String getCtgr();
}