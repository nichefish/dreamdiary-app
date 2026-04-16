package io.nicheblog.dreamdiary.feature.journal.dream.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * JournalDreamPostDto
 * <pre>
 *  저널 꿈 등록/수정용 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalDreamPostDto
        extends BaseClsfDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_DREAM.key;

    /** 제목 */
    private String title;
    /** 내용 */
    private String content;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer journalDayId;
    /** 저널 기준일자 */
    private Integer yy;
    /** 저널 기준일자 */
    private Integer mnth;

    /** 순번 */
    private Integer sortOrder;

    /** 저널 일기 목록 */
    private List<JournalIntrptDto> journalIntrptList;

    /** 꿈꾼이(타인) 이름 */
    private String elseDreamerNm;

    /**
     * 인덱스 변경 여부
     */
    @Builder.Default
    private Boolean isSortOrderChanged = false;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 첨부파일 모듈 */
    public FileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}

