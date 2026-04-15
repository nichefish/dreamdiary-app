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
     * 二쇱뼱吏?{@link JournalDayDto} 媛앹껜??怨듯쑕??諛?二쇰쭚 ?щ? ?뺣낫瑜??ㅼ젙?쒕떎.
     *
     * @param journalDayList 怨듯쑕??諛?二쇰쭚 ?뺣낫瑜??ㅼ젙?????DTO
     * @param holydayMap ?좎쭨(String: yyyy-MM-dd) ??怨듯쑕???대쫫 紐⑸줉 留ㅽ븨 ?뺣낫
     */
    public static void setHolydayInfo(final List<JournalDayDto> journalDayList, final Map<String, List<String>> holydayMap) throws Exception {
        if (CollectionUtils.isEmpty(journalDayList) || holydayMap == null) return;

        for (final JournalDayDto journalDay : journalDayList) {
            setHolydayInfo(journalDay, holydayMap);
        }
    }

    /**
     * 二쇱뼱吏?{@link JournalDayDto} 媛앹껜??怨듯쑕??諛?二쇰쭚 ?щ? ?뺣낫瑜??ㅼ젙?쒕떎.
     *
     * @param journalDay 怨듯쑕??諛?二쇰쭚 ?뺣낫瑜??ㅼ젙?????DTO
     * @param holydayMap ?좎쭨(String: yyyy-MM-dd) ??怨듯쑕???대쫫 紐⑸줉 留ㅽ븨 ?뺣낫
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

