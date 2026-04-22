package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
    private String createdBy;
    private String contentType;
}
