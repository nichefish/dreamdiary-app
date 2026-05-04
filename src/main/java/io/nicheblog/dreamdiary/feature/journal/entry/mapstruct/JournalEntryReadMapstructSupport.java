package io.nicheblog.dreamdiary.feature.journal.entry.mapstruct;

import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

public interface JournalEntryReadMapstructSupport {

    /**
     * 엔티티의 뷰 전용 필드를 DTO에 후처리 매핑한다.
     *
     * @param entity 원본 엔티티
     * @param dto 대상 DTO
     * @throws Exception 변환 중 예외
     */
    @AfterMapping
    default void mapJournalEntryViewFields(final JournalEntryEntity entity, final @MappingTarget JournalEntryDto dto) throws Exception {
        if (entity == null || dto == null) return;

        dto.setJournalChapterId(entity.getJournalChapterId());
        dto.setMarkdownContent(StringUtils.isEmpty(entity.getContent()) ? "-" : MarkdownUtils.markdown(entity.getContent()));

        if (entity.getJournalChapter() == null) {
            dto.setJournalDatePrecision(JournalDatePrecision.EXACT);
            return;
        }

        dto.setJournalDayId(entity.getJournalChapter().getJournalDayId());
        if (entity.getJournalChapter().getJournalDay() == null) {
            dto.setJournalDatePrecision(JournalDatePrecision.EXACT);
            return;
        }

        dto.setStdrdDt(DateUtils.asStr(entity.getJournalChapter().getJournalDay().getJournalDate(), DatePtn.DATE));
        dto.setJournalDatePrecision(entity.getJournalChapter().getJournalDay().getJournalDatePrecision());
        dto.setYy(entity.getJournalChapter().getJournalDay().getYy());
        dto.setMnth(entity.getJournalChapter().getJournalDay().getMnth());
        if (entity.getJournalChapter().getJournalDay().getJournalDate() != null) {
            dto.setJournalDateWeekDay(DateUtils.getDayOfWeekChinese(entity.getJournalChapter().getJournalDay().getJournalDate()));
        }
    }
}
