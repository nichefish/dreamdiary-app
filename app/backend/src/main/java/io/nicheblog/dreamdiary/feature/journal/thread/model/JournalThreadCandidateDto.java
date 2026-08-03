package io.nicheblog.dreamdiary.feature.journal.thread.model;

import lombok.Builder;
import lombok.Getter;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;

import java.time.LocalDateTime;

/**
 * JournalThreadCandidateDto
 * <pre>
 *  엔트리 소속 메뉴에 제공하는 경량 저널 스레드 후보 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class JournalThreadCandidateDto {

    private Integer id;

    private String title;

    private PrefixDto prefix;

    /**
     * 스레드 라이프사이클 키 ({@code OPEN}/{@code PENDING}/{@code RESOLVED}).
     * <p>
     * 미설정 행은 서버가 {@code OPEN} 으로 내려준다.
     * </p>
     */
    private String lifecycleKey;

    /** 현재 살아있는 소속 수. 후보 우선순위의 사용 빈도 근거다. */
    private Long membershipCount;

    /** 가장 최근에 살아있는 소속이 추가된 시각. 후보 우선순위의 최근 사용 근거다. */
    private LocalDateTime lastMembershipAt;

    /** 후보를 요청한 엔트리가 현재 이 스레드에 소속돼 있는지 여부. */
    private boolean member;
}
