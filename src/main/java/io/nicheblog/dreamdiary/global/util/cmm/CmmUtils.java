package io.nicheblog.dreamdiary.global.util.cmm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
@Component
@Log4j2
public class CmmUtils {

    /** 파라피터 관련 메소드 분리 및 합성 */
    public static class Param extends ParamModule {}

    /**
     * 공통 > Object -> Map으로 변환
     */
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
     * 공통 > dto의 property 값으로 paramString 생성
     * 기본 :: 프로퍼티 그대로 변환
     */
    public String createQueryStringFromObject(final Object object) throws Exception {
        // object -> hashMap
        return createQueryStringFromObject(object, null);
    }

    /**
     * 공통 > dto의 property 값으로 paramString 생성
     * (SNAKE CASE 별도 처리 가능)
     */
    @SuppressWarnings("deprecation")
    public String createQueryStringFromObject(
            final Object object,
            final String strategy
    ) throws Exception {
        // object -> hashMap
        final ObjectMapper mapper = new ObjectMapper();
        if ("SNAKE".equals(strategy)) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        }
        final Map<String, Object> paramMap = mapper.convertValue(object, HashMap.class);
        // map에서 value가 비어있는 key들을 걸러냄
        final Map<String, Object> filteredParamMap = CmmUtils.Param.filterParamMap(paramMap);
        // queryString으로 변환;
        return createParamStringFromMap(filteredParamMap);
    }


    /**
     * 공통 > map의 key-value값으로 paramString 생성
     */
    public String createParamStringFromMap(final Map<String, Object> paramMap) throws Exception {
        final StringBuilder paramData = new StringBuilder();
        for (Map.Entry<String, Object> param : paramMap.entrySet()) {
            if (!paramData.isEmpty()) paramData.append("&");
            paramData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            paramData.append("=");
            paramData.append(URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
        }
        log.info("creating paramString... paramData: {}, paramString: {}", paramData, paramData.toString());
        return paramData.toString();
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
     * 쿼리스트링을 Map에 담아서 반환
     */
    public Map<String, String> queryStringToMap(final String queryString) {
        log.info("queryString: {}", queryString);
        if (StringUtils.isEmpty(queryString)) return null;

        final Map<String, String> resultMap = new HashMap<>();
        for (String param : queryString.split("&")) {
            final String[] pair = param.split("=");
            resultMap.put(pair[0], (pair.length > 1) ? pair[1] : "");
        }
        return resultMap;
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
}