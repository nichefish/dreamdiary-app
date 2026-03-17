package io.nicheblog.dreamdiary.domain.jrnl.day.model;

import io.nicheblog.dreamdiary.domain.clsf.ContentType;
import io.nicheblog.dreamdiary.domain.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * JrnlDayCalDto
 * <pre>
 *  저널 일자 달력 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JrnlDayCalDto
        extends BaseCalDto {

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JRNL_DAY.key;

    /* ----- */

    /** 기준일자 */
    private String stdrdDt;

    /* ----- */

    public String getDisplay() {
        return "background";
    }

    public String getColor() {
        return "#c1e4f6";
    }

    public String getTitle() {
        String hashTagStr = this.tag.getHashTagStr();
        return StringUtils.defaultString(hashTagStr, this.stdrdDt);
    }

    public String getIcon() {
        return "<i class=\"bi bi-calendar3\"></i>";
    }

    public String getGroupId() {
        return this.contentType;
    }

    public String getClassName() {
        return "text-truncate";
    }

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
