package io.nicheblog.dreamdiary.feature.journal.thread.model;

import java.time.LocalDateTime;

/**
 * JournalThreadCandidateProjection
 * <pre>
 *  엔트리 소속 메뉴용 저널 스레드 후보 집계 Projection.
 * </pre>
 *
 * @author nichefish
 */
public interface JournalThreadCandidateProjection {

    Integer getId();

    String getTitle();

    Integer getPrefixId();

    String getPrefixName();

    String getPrefixColor();

    String getPrefixActiveYn();

    /** 스레드 라이프사이클 키. 행이 없으면 쿼리가 {@code OPEN} 으로 보정한다. */
    String getLifecycleKey();

    Number getMembershipCount();

    LocalDateTime getLastMembershipAt();

    Number getCurrentEntryMembershipCount();
}
