package io.nicheblog.dreamdiary.feature.journal.dream.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * JournalDreamCalDto
 * <pre>
 *  저널 꿈 달력 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalDreamCalDto
        extends BaseCalDto {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_DAY.key;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer journalDayId;

    /** 중요 여부 (Y/N) */
    @Builder.Default
    private String imprtcYn = "N";

    /** 내용 */
    private String content;

    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /* ----- */

    public String getTitle() {
        String hashTagStr = this.tag.getHashTagStr();
        return StringUtils.defaultString(hashTagStr, "(dream)");
    }

    public String getTextColor() {
        return "rgba(95,0,130,0.8)";
    }

    public String getIcon() {
        return "<i class=\"bi bi-moon-stars text-dream\"></i>";
    }

    public String getClassName() {
        return "bg-transparent border-transparent text-dream text-truncate";
    }

    public String getGroupId() {
        return this.contentType;
    }
    
    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}

