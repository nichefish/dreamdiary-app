package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.clsf.meta.model.MetaContentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JournalDayMetaContentDto
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

