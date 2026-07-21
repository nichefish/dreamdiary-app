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

    String getCategoryCode();

    Number getMembershipCount();

    LocalDateTime getLastMembershipAt();

    Number getCurrentEntryMembershipCount();
}
