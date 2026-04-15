package io.nicheblog.dreamdiary.feature.journal.sumry.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * JournalSumryDto
 * <pre>
 *  저널 결산 Dto.
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
public class JournalSumryDto
        extends BaseClsfDto
        implements Identifiable<Integer>, TagCmpstnModule, SectnCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_SUMRY.key;

    /** 제목 */
    private String title;
    /** 내용 */
    private String content;
    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /* ----- */

    /** 결산 년도 */
    private Integer yy;

    /** 꿈 일수 */
    private Integer dreamDayCnt;
    /** 꿈 갯수 */
    private Integer dreamCnt;

    /** 꿈 기록 완료 여부 (Y/N) */
    @Builder.Default
    private String dreamComptYn = "N";

    /** 저널 결산 리뷰 목록 */
    private List<JournalSumryReviewDto> journalSumryReviewList;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 단락 정보 모듈 */
    public SectnCmpstn sectn;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}

