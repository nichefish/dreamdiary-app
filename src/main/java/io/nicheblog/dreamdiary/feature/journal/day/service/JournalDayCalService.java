package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.mapstruct.JournalDayCalMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayCalDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryCalMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryCalDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * JournalDayCalService
 * <pre>
 *  ??????깆쁽 ??????뺥돩??筌뤴뫀諭? * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDayCalService {

    private final JournalDayQueryService journalDayQueryService;

    private final JournalDayCalMapstruct dayCalMapstruct = JournalDayCalMapstruct.INSTANCE;
    private final JournalEntryCalMapstruct entryCalMapstruct = JournalEntryCalMapstruct.INSTANCE;

    /**
     * ????筌뤴뫖以?鈺곌퀬??(dto level)
     *
     * @param searchParam 野꺜??鈺곌퀗援????용┸ ???뵬沃섎챸苑?揶쏆빘猿?     * @return {@link List} -- 鈺곌퀬???筌뤴뫖以?     */
    public List<BaseCalDto> getCalListDtoByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        final List<JournalDayDto> myJournalDayList = journalDayQueryService.getYyMnthListDtoEnrichedByUser(username, searchParam);

        final List<BaseCalDto> journalCalEventList = new ArrayList<>();
        for (final JournalDayDto journalDay: myJournalDayList) {
            final JournalDayCalDto journalDayCalDto = dayCalMapstruct.toCalDto(journalDay);
            journalCalEventList.add(journalDayCalDto);

            final List<JournalChapterDto> myEntryList = journalDay.getJournalChapterList();
            if (CollectionUtils.isNotEmpty(myEntryList)) {
                for (final JournalChapterDto journalChapter : myEntryList) {
                    final List<JournalEntryDto> myDiaryList = JournalEntryViewProjectionHelper.getDiaryEntries(journalChapter);
                    if (CollectionUtils.isNotEmpty(myDiaryList)) {
                        for (final JournalEntryDto journalDiaryDto : myDiaryList) {
                            final JournalEntryCalDto diaryCalDto = entryCalMapstruct.toCalDto(journalDiaryDto);
                            journalCalEventList.add(diaryCalDto);
                        }
                    }
                }
            }

            final List<JournalEntryDto> myDreamList = journalDay.getJournalDreamList();
            if (CollectionUtils.isNotEmpty(myDreamList)) {
                for (final JournalEntryDto journalDreamDto : myDreamList) {
                    final JournalEntryCalDto dreamCalDto = entryCalMapstruct.toCalDto(journalDreamDto);
                    journalCalEventList.add(dreamCalDto);
                }
            }
        }

        // ?醫롮??? ????JournalDay, JournalDiary, JournalDream) 疫꿸퀣???곗쨮 ?類ｌ졊
        journalCalEventList.sort((event1, event2) -> {
            final int dateComparison = event1.getStart().compareTo(event2.getStart());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return compareEventType(event1, event2);
        });

        return journalCalEventList;
    }

    /**
     * ??源????????쑨??筌롫뗄苑??JournalDay, JournalDiary, JournalDream)
     * @param event1 BaseCalDto
     * @param event2 BaseCalDto
     */
    private int compareEventType(final BaseCalDto event1, final BaseCalDto event2) {
        // ?怨쀪퐨??뽰맄 ?類ㅼ벥: JournalDay -> JournalDiary -> JournalDream
        final int eventType1 = getEventTypePriority(event1);
        final int eventType2 = getEventTypePriority(event2);
        return Integer.compare(eventType1, eventType2);
    }

    /**
     * 揶???源?紐꾩벥 ?怨쀪퐨??뽰맄??獄쏆꼹???롫뮉 筌롫뗄苑??     * @param event BaseCalDto
     */
    private int getEventTypePriority(final BaseCalDto event) {
        if (event instanceof JournalDayCalDto) return 1;
        if (event instanceof JournalEntryCalDto entryCalDto) return entryCalDto.getTypePriority();
        return 2;
    }
}


