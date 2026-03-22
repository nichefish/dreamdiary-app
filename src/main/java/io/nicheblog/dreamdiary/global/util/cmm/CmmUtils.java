package io.nicheblog.dreamdiary.global.util.cmm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CmmUtils
 * <pre>
 *  공통, 기본 기능 처리 유틸리티 모듈
 * </pre>
 * TODO:: 필요별로 유틸 분리하고 필요하면 새로 만들기
 *
 * @author nichefish
 */
@UtilityClass
@Log4j2
public class CmmUtils {

    /**
     * 공통 > Object -> Map으로 변환
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertToMap(final Object searchParam) throws Exception {
        if (searchParam == null) return new HashMap<>();

        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(searchParam, HashMap.class);
    }

    /**
     * 공통 > Page 및 listIndex 정보 받아서 *역순* rownum 반환
     */
    public static Long getPageRnum(final Page<?> pageList, int i) {
        final Pageable pageable = pageList.getPageable();
        long totalCnt = pageList.getTotalElements();
        long offset = pageable.isPaged() ? pageable.getOffset() : 0L;
        return (totalCnt - offset - i);
    }

    /**
     * 공통 > map의 key값에 prefix/suffix를 붙여서 복사한다. (objectMapper 사전작업)
     */
    public static Map<?, ?> copyMap(final Map<?, ?> source, final String keyModifier, final String mode) {
        if (source == null) return null;
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<?, ?> stringObjectEntry : source.entrySet()) {
            final Object key = stringObjectEntry.getKey();
            final Object value = stringObjectEntry.getValue();
            if (mode == null) {
                result.put(key.toString(), value);
            } else if (Constant.PREFIX.equals(mode)) {
                result.put(keyModifier + key.toString(), value);
            } else if (Constant.SUFFIX.equals(mode)) {
                result.put(key.toString() + keyModifier, value);
            }
        }
        return result;
    }

    /**
     * 공통 > html 태그 제거 (정규식)
     */
    public String removeHtmlTag(final String html) {
        return html.replaceAll(Regex.HTML_TAG_REGEX, "");
    }

    /**
     * 공통 > 년도, 월 담긴 Map 반환
     */
    public static Map<String, Object> getYyMhtnMap(final String yyStr, final String mnthStr) throws Exception {
        final Integer[] prevMnth = DateUtils.getPrevYyMnth();
        final int yy = (StringUtils.isNotEmpty(yyStr)) ? Integer.parseInt(yyStr) : prevMnth[0];
        final int mnth = (StringUtils.isNotEmpty(mnthStr)) ? Integer.parseInt(mnthStr) : prevMnth[1];
        return new HashMap<>() {{
            put("yy", yy);
            put("mnth", mnth);
        }};
    }

    /**
     * 공통 > 숫자 텍스트에 콤마 추가
     */
    public String thousandSeparator(final String value) {
        try {
            return new java.text.DecimalFormat("#,###").format(Integer.parseInt(value));
        } catch (final Exception e) {
            MessageUtils.getExceptionMsg(e);
            return "";
        }
    }

    /**
     * 공통 > 숫자 텍스트에서 콤마 제거
     */
    public String removeComma(final String value) {
        return value.replaceAll("\\,", "");
    }

    /**
     * 공통 > tagify 문자열 파싱
     */
    public static List<String> parseTagify(final String tafigyStr) {
        if (StringUtils.isEmpty(tafigyStr)) return new ArrayList<>();
        final JSONArray jArray = new JSONArray(tafigyStr);
        final List<String> strList = new ArrayList<>();
        for (int i = 0; i < jArray.length(); i++) {
            strList.add(jArray.getJSONObject(i).getString("value"));
        }
        return strList;
    }

    /**
     * 문자열을 Set<String>으로 변환하는 유틸 함수
     *
     * @param valueStr String
     * @param delimiter String
     * @return Set<String>
     */
    public static Set<String> parseToSet(final String valueStr, final String delimiter) {
        if (StringUtils.isEmpty(delimiter)) return parseToSet(valueStr, ",");

        return Arrays.stream(valueStr.split(delimiter))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * HTML 을 텍스트로 변환
     *
     * @param html String
     * @return String
     */
    public static String htmlToText(final String html) {
        if (StringUtils.isEmpty(html)) return "";

        Document doc = Jsoup.parse(html);
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        doc.select("br").append("\\n");
        doc.select("p").prepend("\\n");
        doc.select("div").prepend("\\n");
        doc.select("li").prepend("\\n");

        return doc.text().replace("\\n", "\n").trim();
    }

    /**
     * 공통 > 목록 검색 parameter 빈 값 걸러내고 정돈
     *
     * @param searchParam 필터링할 BaseSearchParam 객체
     * @return {@link BaseSearchParam} -- 필터링된 BaseSearchParam 객체
     */
    public static BaseSearchParam filterParam(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = filterParamMap(searchParamMap);
        return convertToParam(filteredSearchKey);
    }

    /**
     * 공통 > Map -> Param으로 변환
     *
     * @param searchParamMap 변환할 파라미터 맵
     * @return {@link BaseSearchParam} -- 변환된 BaseSearchParam 객체
     */
    public static BaseSearchParam convertToParam(final Map<String, Object> searchParamMap) throws Exception {
        if (MapUtils.isEmpty(searchParamMap)) return new BaseSearchParam();
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(searchParamMap, BaseSearchParam.class);
    }

    /**
     * 공통 > 목록 검색 parameterMap 빈 값 걸러내고 정돈.
     *
     * @param searchParamMap 필터링할 파라미터 맵
     * @return {@link Map} -- 필터링된 파라미터 맵
     */
    public static Map<String, Object> filterParamMap(final Map<String, Object> searchParamMap) throws Exception {
        final Map<String, Object> filteredSearchKey = new HashMap<>();
        // 목록 검색에서 시작일, 종료일이 같은 날짜(문자열)로 넘어올 경우 searchEndDt를 23:59:59로 세팅
        final Object searchStartDt = searchParamMap.get("searchStartDt");
        if (searchStartDt instanceof String searchStartDtStr) {
            if (StringUtils.isNotEmpty(searchStartDtStr)) {
                final String searchEndDtStr = (String) searchParamMap.get("searchEndDt");
                if (searchStartDtStr.equals(searchEndDtStr)) {
                    final Date searchEndDt = DateUtils.asDate(searchEndDtStr);
                    searchParamMap.put("searchEndDt", DateUtils.Parser.eDateParseStr(searchEndDt));
                }
            }
        }
        // Parameter 순차적으로 세팅
        for (final String key : searchParamMap.keySet()) {
            // pageNo, pageSize는 검색인자가 아니므로 여기 들어갈 필요가 없다.
            if ("pageNo".equals(key)) continue;
            if ("pageSize".equals(key)) continue;
            // isBackToList 빼기
            if ("isBackToList".equals(key)) continue;
            final Object value = searchParamMap.get(key);
            final String valueStr = String.valueOf(searchParamMap.get(key));
            if (StringUtils.isNotEmpty(valueStr) && !"null".equals(valueStr)) {
                // 날짜 파라미터 세팅 ("Dt"로 끝나는 입력값은 Date로 변환하여 Dt에 담음)
                if (key.endsWith("Dt")) filteredSearchKey.put(key, DateUtils.asDate(value));
                // searchEndDt 문자열 :: 끝에 강제로 23:59:59 붙여줌 (yyyy-MM-dd까지만 받기때문)
                if (key.equals("searchEndDt")) {
                    filteredSearchKey.put(key, DateUtils.Parser.eDateParse(value));
                    continue;
                }
                // searchType + searchKeyword 매칭 (인덱스마다 자동 설정) (ex.searchType1 <- searchKeyword1)
                if (key.startsWith("searchType")) {
                    final String idx = key.replace("searchType", "");
                    final String searchKeyword = String.valueOf(searchParamMap.get("searchKeyword" + idx));
                    if (StringUtils.isNotEmpty(searchKeyword) && !"null".equals(searchKeyword)) {
                        filteredSearchKey.put(valueStr, searchKeyword);
                    }
                    continue;
                }
                if (!"searchKeywords".equals(key) && key.startsWith("searchKeyword")) continue;
                if (key.endsWith("Dt")) continue;
                filteredSearchKey.put(key, value);
            }
        }
        return filteredSearchKey;
    }
}
