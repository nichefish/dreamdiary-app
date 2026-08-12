package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 엔트리 태그별 사용 건수를 집계하기 위한 기간·사용자·콘텐츠 타입 조건이다.
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class JournalEntryTagContentParam
        extends BaseSearchParam {

    private Integer refId;
    private String refContentType;
    private Integer tagId;
    private Integer yy;
    private Integer mnth;
    private String weekStartDt;
    /** 기준일자 하루 집계 조건. */
    private String stdrdDt;
    private String createdBy;
    private String contentType;
    /** 일기 축 등 복수 contentType IN 집계용. */
    private List<String> contentTypes;
}
