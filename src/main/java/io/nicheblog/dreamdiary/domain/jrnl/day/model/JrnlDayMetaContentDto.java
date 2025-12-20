package io.nicheblog.dreamdiary.domain.jrnl.day.model;

import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaContentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDayMetaContentDto
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class JrnlDayMetaContentDto extends MetaContentDto {

    /**
     * 저널 일자
     */
    private JrnlDayDto jrnlDay;
}
