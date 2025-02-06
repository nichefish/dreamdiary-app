package io.nicheblog.dreamdiary.domain.jrnl.day.service.impl;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct.JrnlDayCalMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayCalDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayCalService;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryCalMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryCalDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct.JrnlDreamCalMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamCalDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.schdul.service.SchdulCalService;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
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
public class JrnlDayCalServiceImpl
        implements JrnlDayCalService {

    private final JrnlDayService JrnlDayService;
    private final SchdulCalService schdulCalService;

    private final JrnlDayCalMapstruct dayCalMapstruct = JrnlDayCalMapstruct.INSTANCE;
    private final JrnlDiaryCalMapstruct diaryCalMapstruct = JrnlDiaryCalMapstruct.INSTANCE;
    private final JrnlDreamCalMapstruct dreamCalMapstruct = JrnlDreamCalMapstruct.INSTANCE;

    private final ApplicationContext context;
    private JrnlDayCalServiceImpl getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 전체 목록 (저널 일자 및 휴가) 데이터 조회
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 휴가 목록
     * @throws Exception 조회 및 처리 중 발생할 수 있는 예외
     */
    @Override
    public List<BaseCalDto> getSchdulTotalCalList(final JrnlDaySearchParam searchParam) throws Exception {
        final List<BaseCalDto> totalSchdulCalList = new ArrayList<>();

        // 저널 일자 복록 조회
        final List<BaseCalDto> jrnlDayCalList = this.getSelf().getMyCalListDto(searchParam);
        totalSchdulCalList.addAll(jrnlDayCalList);

        // 일정(공휴일, 행사) 달력 목록 검색
        final List<BaseCalDto> hldyCalList = schdulCalService.getHldyCalList(searchParam);
        totalSchdulCalList.addAll(hldyCalList);

        return totalSchdulCalList;
    }

    /**
     * 내 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlDayCalList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getYy() + \"_\" + #searchParam.getMnth()")
    public List<BaseCalDto> getMyCalListDto(final JrnlDaySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());
        final List<JrnlDayDto> myJrnlDayList = JrnlDayService.getMyListDto(searchParam);

        final List<BaseCalDto> jrnlCalEventList = new ArrayList<>();
        for (final JrnlDayDto jrnlDay: myJrnlDayList) {
            // JrnlDayDto를 CalDto로 변환
            final JrnlDayCalDto jrnlDayCalDto = dayCalMapstruct.toCalDto(jrnlDay);
            jrnlCalEventList.add(jrnlDayCalDto);

            final List<JrnlDiaryDto> myDiaryList = jrnlDay.getJrnlDiaryList();
            if (CollectionUtils.isNotEmpty(myDiaryList)) {
                for (final JrnlDiaryDto jrnlDiaryDto : myDiaryList) {
                    final JrnlDiaryCalDto diaryCalDto = diaryCalMapstruct.toCalDto(jrnlDiaryDto);
                    jrnlCalEventList.add(diaryCalDto);
                }
            }

            final List<JrnlDreamDto> myDreamList = jrnlDay.getJrnlDreamList();
            if (CollectionUtils.isNotEmpty(myDreamList)) {
                for (final JrnlDreamDto jrnlDreamDto : myDreamList) {
                    final JrnlDreamCalDto dreamCalDto = dreamCalMapstruct.toCalDto(jrnlDreamDto);  // 필요시 mapstruct에서 변환
                    jrnlCalEventList.add(dreamCalDto);
                }
            }
        }

        // 날짜와 타입(JrnlDay, JrnlDiary, JrnlDream) 기준으로 정렬
        jrnlCalEventList.sort((event1, event2) -> {
            // 날짜 비교
            final int dateComparison = event1.getStart().compareTo(event2.getStart());
            if (dateComparison != 0) {
                return dateComparison;  // 날짜가 다르면 날짜 기준으로 정렬
            }
            // 날짜가 같으면 타입에 따라 정렬 (JrnlDay, JrnlDiary, JrnlDream 순)
            return compareEventType(event1, event2);
        });

        return jrnlCalEventList;
    }

    // 이벤트 타입 비교 메서드 (JrnlDay, JrnlDiary, JrnlDream 순)
    private int compareEventType(final BaseCalDto event1, final BaseCalDto event2) {
        // 우선순위 정의: JrnlDay -> JrnlDiary -> JrnlDream
        final int eventType1 = getEventTypePriority(event1);
        final int eventType2 = getEventTypePriority(event2);
        return Integer.compare(eventType1, eventType2);
    }

    // 각 이벤트의 우선순위를 반환하는 메서드
    private int getEventTypePriority(final BaseCalDto event) {
        if (event instanceof JrnlDayCalDto) return 1;
        if (event instanceof JrnlDiaryCalDto) return 3;  // JrnlDiary
        if (event instanceof JrnlDreamCalDto) return 4;  // JrnlDream
        return 2;  // 기타 경우
    }
}