package io.nicheblog.dreamdiary.domain.jrnl.diary.model;

import io.nicheblog.dreamdiary.global._common._clsf.ContentType;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * JrnlDiaryCalDto
 * <pre>
 *  저널 일기 달력 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JrnlDiaryCalDto
        extends BaseCalDto {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DAY;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;

    /** 중요 여부 (Y/N) */
    @Builder.Default
    private String imprtcYn = "N";

    /** 내용 */
    private String cn;

    /** 마크다운 처리된 내용 */
    private String markdownCn;

    /* ----- */

    public String getTitle() {
        String hashTagStr = this.tag.getHashTagStr();
        return StringUtils.defaultString(hashTagStr, "(diary)");
    }

    public String getTextColor() {
        return "#8e8e8e";
    }

    public String getIcon() {
        return "<i class=\"bi bi-book\"></i>";
    }

    public String getClassName() {
        return "bg-transparent border-transparent";
    }

    public String getGroupId() {
        return this.contentType;
    }

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
