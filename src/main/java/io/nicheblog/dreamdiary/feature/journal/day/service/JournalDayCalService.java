package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.mapstruct.JournalDayCalMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayCalDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.mapstruct.JournalDiaryCalMapstruct;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryCalDto;
import io.nicheblog.dreamdiary.feature.journal.dream.mapstruct.JournalDreamCalMapstruct;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamCalDto;
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
 *  저널 일자 달력 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("journalDayCalService")
@RequiredArgsConstructor
@Log4j2
public class JournalDayCalService {

    private final JournalDayQueryService journalDayQueryService;

    private final JournalDayCalMapstruct dayCalMapstruct = JournalDayCalMapstruct.INSTANCE;
    private final JournalDiaryCalMapstruct diaryCalMapstruct = JournalDiaryCalMapstruct.INSTANCE;
    private final JournalDreamCalMapstruct dreamCalMapstruct = JournalDreamCalMapstruct.INSTANCE;

    /**
     * 달력 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<BaseCalDto> getCalListDtoByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        final List<JournalDayDto> myJournalDayList = journalDayQueryService.getYyMnthListDtoEnrichedByUser(username, searchParam);

        final List<BaseCalDto> journalCalEventList = new ArrayList<>();
        for (final JournalDayDto journalDay: myJournalDayList) {
            // JournalDayDto를 CalDto로 변환
            final JournalDayCalDto journalDayCalDto = dayCalMapstruct.toCalDto(journalDay);
            journalCalEventList.add(journalDayCalDto);

            final List<JournalChapterDto> myEntryList = journalDay.getJournalChapterList();
            if (CollectionUtils.isNotEmpty(myEntryList)) {
                for (final JournalChapterDto journalChapter : myEntryList) {
                    final List<JournalDiaryDto> myDiaryList = journalChapter.getJournalDiaryList();
                    if (CollectionUtils.isNotEmpty(myDiaryList)) {
                        for (final JournalDiaryDto journalDiaryDto : myDiaryList) {
                            final JournalDiaryCalDto diaryCalDto = diaryCalMapstruct.toCalDto(journalDiaryDto);
                            journalCalEventList.add(diaryCalDto);
                        }
                    }
                }
            }

            final List<JournalDreamDto> myDreamList = journalDay.getJournalDreamList();
            if (CollectionUtils.isNotEmpty(myDreamList)) {
                for (final JournalDreamDto journalDreamDto : myDreamList) {
                    final JournalDreamCalDto dreamCalDto = dreamCalMapstruct.toCalDto(journalDreamDto);
                    journalCalEventList.add(dreamCalDto);
                }
            }
        }

        // 날짜와 타입(JournalDay, JournalDiary, JournalDream) 기준으로 정렬
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
     * 이벤트 타입 비교 메서드(JournalDay, JournalDiary, JournalDream)
     * @param event1 BaseCalDto
     * @param event2 BaseCalDto
     */
    private int compareEventType(final BaseCalDto event1, final BaseCalDto event2) {
        // 우선순위 정의: JournalDay -> JournalDiary -> JournalDream
        final int eventType1 = getEventTypePriority(event1);
        final int eventType2 = getEventTypePriority(event2);
        return Integer.compare(eventType1, eventType2);
    }

    /**
     * 각 이벤트의 우선순위를 반환하는 메서드
     * @param event BaseCalDto
     */
    private int getEventTypePriority(final BaseCalDto event) {
        if (event instanceof JournalDayCalDto) return 1;
        if (event instanceof JournalDiaryCalDto) return 3;
        if (event instanceof JournalDreamCalDto) return 4;
        return 2;
    }
}

