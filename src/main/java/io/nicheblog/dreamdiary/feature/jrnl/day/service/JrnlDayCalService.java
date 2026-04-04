package io.nicheblog.dreamdiary.feature.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.mapstruct.JrnlDayCalMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayCalDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.mapstruct.JrnlDiaryCalMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryCalDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.mapstruct.JrnlDreamCalMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamCalDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * JrnlDayCalService
 * <pre>
 *  저널 일자 달력 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDayCalService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDayCalService {

    private final JrnlDayQueryService jrnlDayQueryService;

    private final JrnlDayCalMapstruct dayCalMapstruct = JrnlDayCalMapstruct.INSTANCE;
    private final JrnlDiaryCalMapstruct diaryCalMapstruct = JrnlDiaryCalMapstruct.INSTANCE;
    private final JrnlDreamCalMapstruct dreamCalMapstruct = JrnlDreamCalMapstruct.INSTANCE;

    /**
     * 달력 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<BaseCalDto> getCalListDtoByUser(final String userId, final JrnlDaySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        final List<JrnlDayDto> myJrnlDayList = jrnlDayQueryService.getYyMnthListDtoEnrichedByUser(userId, searchParam);

        final List<BaseCalDto> jrnlCalEventList = new ArrayList<>();
        for (final JrnlDayDto jrnlDay: myJrnlDayList) {
            // JrnlDayDto를 CalDto로 변환
            final JrnlDayCalDto jrnlDayCalDto = dayCalMapstruct.toCalDto(jrnlDay);
            jrnlCalEventList.add(jrnlDayCalDto);

            final List<JrnlEntryDto> myEntryList = jrnlDay.getJrnlEntryList();
            if (CollectionUtils.isNotEmpty(myEntryList)) {
                for (final JrnlEntryDto jrnlEntry : myEntryList) {
                    final List<JrnlDiaryDto> myDiaryList = jrnlEntry.getJrnlDiaryList();
                    if (CollectionUtils.isNotEmpty(myDiaryList)) {
                        for (final JrnlDiaryDto jrnlDiaryDto : myDiaryList) {
                            final JrnlDiaryCalDto diaryCalDto = diaryCalMapstruct.toCalDto(jrnlDiaryDto);
                            jrnlCalEventList.add(diaryCalDto);
                        }
                    }
                }
            }

            final List<JrnlDreamDto> myDreamList = jrnlDay.getJrnlDreamList();
            if (CollectionUtils.isNotEmpty(myDreamList)) {
                for (final JrnlDreamDto jrnlDreamDto : myDreamList) {
                    final JrnlDreamCalDto dreamCalDto = dreamCalMapstruct.toCalDto(jrnlDreamDto);
                    jrnlCalEventList.add(dreamCalDto);
                }
            }
        }

        // 날짜와 타입(JrnlDay, JrnlDiary, JrnlDream) 기준으로 정렬
        jrnlCalEventList.sort((event1, event2) -> {
            final int dateComparison = event1.getStart().compareTo(event2.getStart());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return compareEventType(event1, event2);
        });

        return jrnlCalEventList;
    }

    /**
     * 이벤트 타입 비교 메서드(JrnlDay, JrnlDiary, JrnlDream)
     * @param event1 BaseCalDto
     * @param event2 BaseCalDto
     */
    private int compareEventType(final BaseCalDto event1, final BaseCalDto event2) {
        // 우선순위 정의: JrnlDay -> JrnlDiary -> JrnlDream
        final int eventType1 = getEventTypePriority(event1);
        final int eventType2 = getEventTypePriority(event2);
        return Integer.compare(eventType1, eventType2);
    }

    /**
     * 각 이벤트의 우선순위를 반환하는 메서드
     * @param event BaseCalDto
     */
    private int getEventTypePriority(final BaseCalDto event) {
        if (event instanceof JrnlDayCalDto) return 1;
        if (event instanceof JrnlDiaryCalDto) return 3;
        if (event instanceof JrnlDreamCalDto) return 4;
        return 2;
    }
}
