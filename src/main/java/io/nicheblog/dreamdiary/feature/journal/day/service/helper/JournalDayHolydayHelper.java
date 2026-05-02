package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalDayHolydayHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalDayHolydayHelper {

    /**
     * 주어진 일자 목록에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param journalDayList 공휴일 및 주말 정보를 설정할 일자 DTO 목록
     * @param holydayMap 날짜(String: yyyy-MM-dd)별 공휴일 이름 목록 맵
     */
    public static void setHolydayInfo(final List<JournalDayDto> journalDayList, final Map<String, List<String>> holydayMap) throws Exception {
        if (CollectionUtils.isEmpty(journalDayList) || holydayMap == null) return;

        for (final JournalDayDto journalDay : journalDayList) {
            setHolydayInfo(journalDay, holydayMap);
        }
    }

    /**
     * 주어진 일자에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param journalDay 공휴일 및 주말 정보를 설정할 일자 DTO
     * @param holydayMap 날짜(String: yyyy-MM-dd)별 공휴일 이름 목록 맵
     */
    public static void setHolydayInfo(final JournalDayDto journalDay, final Map<String, List<String>> holydayMap) throws Exception {
        if (journalDay == null || holydayMap == null) return;

        final String stdrdDt = journalDay.getStdrdDt();
        final boolean isHolyday = holydayMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        journalDay.setIsHolyday(isHolyday || isWeekend);
        if (isHolyday) {
            final String concatHolydayNm = String.join(", ", holydayMap.get(stdrdDt));
            journalDay.setHolydayNm(concatHolydayNm);
        }
    }
}
