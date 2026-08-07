package io.nicheblog.dreamdiary.feature.journal.thread.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn.LifecycleCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn.LifecycleCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JournalThreadDto
 * 저널 스레드(JOURNAL_THREAD) 첨부 콘텐츠의 뷰·등록용 DTO.
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalThreadDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule,
        LifecycleCmpstnModule {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_THREAD;

    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    private String title;

    private String content;

    private String markdownContent;

    /** 콘텐츠당 없거나 하나만 선택하는 말머리. */
    private PrefixDto prefix;

    /** 등록·수정 payload에서 선택한 말머리 ID. */
    private Integer prefixId;

    /**
     * 활성 소속 엔트리 수.
     * <p>
     * 목록 enrich({@code applyEntryTagSummaries}) 가 소속 집계 쿼리로 채운다.
     * 스레드 자체 필드가 아니라 표시용 파생 값이다.
     * </p>
     */
    private Long membershipCount;

    /**
     * 활성 소속 엔트리 기준일({@code stdrdDt})의 최소값 ({@code YYYY-MM-DD}).
     * <p>
     * 목록 enrich 가 채운다. 소속이 없거나 유효 일자가 없으면 {@code null}.
     * </p>
     */
    private String firstEntryDate;

    /**
     * 활성 소속 엔트리 기준일({@code stdrdDt})의 최대값 ({@code YYYY-MM-DD}).
     * <p>
     * 목록 enrich 가 채운다. 소속이 없거나 유효 일자가 없으면 {@code null}.
     * 하루짜리면 {@link #firstEntryDate} 와 같다.
     * </p>
     */
    private String lastEntryDate;

    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL extends JournalThreadDto {
        //
    }

    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST extends JournalThreadDto {
        //
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    public FileCmpstn file;
    public CommentCmpstn comment;
    /** 스레드 라이프사이클. 목록·상세 enrich 가 부착 테이블에서 채운다. 없으면 OPEN. */
    public LifecycleCmpstn lifecycle;
    /** 태그 컴포지션. 스레드는 자체 태그를 소유하지 않는다(엔티티 TagEmbed 제거).
     * 화면 표시용 집계 컨테이너로, 목록·상세의 applyEntryTagSummar(y/ies) 가
     * 소속 엔트리 태그를 여기에 집계해 넣는다. 매핑 skip 대비 non-null 초기화. */
    @Builder.Default
    public TagCmpstn tag = new TagCmpstn();
}
