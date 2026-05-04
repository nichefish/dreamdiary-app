package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.attachable.meta.model.MetaContentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JournalDayMetaContentDto
 * 저널 일자에 연결된 메타 콘텐츠 DTO({@link MetaContentDto} 확장).
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class JournalDayMetaContentDto extends MetaContentDto {

    /**
     * 저널 일자
     */
    private JournalDayDto journalDay;
}

