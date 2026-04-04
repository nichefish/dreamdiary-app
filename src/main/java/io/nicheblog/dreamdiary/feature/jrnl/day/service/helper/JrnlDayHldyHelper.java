package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlDayHldyHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JrnlDayHldyHelper {

    /**
     * 二쇱뼱吏?{@link JrnlDayDto} 媛앹껜??怨듯쑕??諛?二쇰쭚 ?щ? ?뺣낫瑜??ㅼ젙?쒕떎.
     *
     * @param jrnlDayList 怨듯쑕??諛?二쇰쭚 ?뺣낫瑜??ㅼ젙?????DTO
     * @param hldyMap ?좎쭨(String: yyyy-MM-dd) ??怨듯쑕???대쫫 紐⑸줉 留ㅽ븨 ?뺣낫
     */
    public static void setHldyInfo(final List<JrnlDayDto> jrnlDayList, final Map<String, List<String>> hldyMap) throws Exception {
        if (CollectionUtils.isEmpty(jrnlDayList) || hldyMap == null) return;

        for (final JrnlDayDto jrnlDay : jrnlDayList) {
            setHldyInfo(jrnlDay, hldyMap);
        }
    }

    /**
     * 二쇱뼱吏?{@link JrnlDayDto} 媛앹껜??怨듯쑕??諛?二쇰쭚 ?щ? ?뺣낫瑜??ㅼ젙?쒕떎.
     *
     * @param jrnlDay 怨듯쑕??諛?二쇰쭚 ?뺣낫瑜??ㅼ젙?????DTO
     * @param hldyMap ?좎쭨(String: yyyy-MM-dd) ??怨듯쑕???대쫫 紐⑸줉 留ㅽ븨 ?뺣낫
     */
    public static void setHldyInfo(final JrnlDayDto jrnlDay, final Map<String, List<String>> hldyMap) throws Exception {
        if (jrnlDay == null || hldyMap == null) return;

        final String stdrdDt = jrnlDay.getStdrdDt();
        final boolean isHldy = hldyMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        jrnlDay.setIsHldy(isHldy || isWeekend);
        if (isHldy) {
            final String concatHldyNm = String.join(", ", hldyMap.get(stdrdDt));
            jrnlDay.setHldyNm(concatHldyNm);
        }
    }
}
