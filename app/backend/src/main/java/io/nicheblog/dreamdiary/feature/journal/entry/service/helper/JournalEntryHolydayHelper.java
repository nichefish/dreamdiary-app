package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalEntryHolydayHelper
 * <pre>
 *  저널 엔트리 DTO에 공휴일명·주말 여부 등 표시용 정보를 채운다.
 *  일자({@code JournalDayHolydayHelper})와 동일 계약: {@code isHolyday} = 공휴일 또는 주말,
 *  {@code holydayNm} 은 공휴일명만(주말 단독이면 비움).
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalEntryHolydayHelper {

    /**
     * 주어진 엔트리 목록에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param entryList 대상 엔트리 DTO 목록
     * @param holydayMap 날짜(yyyy-MM-dd)별 공휴일 이름 목록 맵
     */
    public static void setHolydayInfo(final List<JournalEntryDto> entryList, final Map<String, List<String>> holydayMap) throws Exception {
        if (CollectionUtils.isEmpty(entryList) || holydayMap == null) return;

        for (final JournalEntryDto entry : entryList) {
            setHolydayInfo(entry, holydayMap);
        }
    }

    /**
     * 주어진 엔트리에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param entry 대상 엔트리 DTO
     * @param holydayMap 날짜(yyyy-MM-dd)별 공휴일 이름 목록 맵
     */
    public static void setHolydayInfo(final JournalEntryDto entry, final Map<String, List<String>> holydayMap) throws Exception {
        if (entry == null || holydayMap == null) return;

        final String stdrdDt = entry.getStdrdDt();
        final boolean isHolyday = holydayMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        entry.setIsHolyday(isHolyday || isWeekend);
        if (isHolyday) {
            entry.setHolydayNm(String.join(", ", holydayMap.get(stdrdDt)));
        }
    }
}
