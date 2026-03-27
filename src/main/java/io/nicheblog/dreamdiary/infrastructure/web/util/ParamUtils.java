package io.nicheblog.dreamdiary.infrastructure.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ParamModule
 * <pre>
 *  공통, 기본 기능 처리 유틸리티 모듈 :: CmmUtils에서 사용
 *  (!package-private class)
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@Log4j2
public class ParamUtils {


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
    @SuppressWarnings({"deprecation", "unchecked"})
    public String createQueryStringFromObject(final Object object, final String strategy) throws Exception {
        // object -> hashMap
        final ObjectMapper mapper = new ObjectMapper();
        if ("SNAKE".equals(strategy)) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        }
        final Map<String, Object> paramMap = mapper.convertValue(object, HashMap.class);
        // map에서 value가 비어있는 key들을 걸러냄
        final Map<String, Object> filteredParamMap = CmmUtils.filterParamMap(paramMap);
        // queryString으로 변환;
        return createParamStringFromMap(filteredParamMap);
    }

    /**
     * 공통 > map의 key-value값으로 paramString 생성
     */
    public String createParamStringFromMap(final Map<String, Object> paramMap) throws Exception {
        final StringBuilder paramData = new StringBuilder();
        for (final Map.Entry<String, Object> param : paramMap.entrySet()) {
            if (!paramData.isEmpty()) paramData.append("&");
            paramData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            paramData.append("=");
            paramData.append(URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
        }
        log.info("creating paramString... paramData: {}, paramString: {}", paramData, paramData.toString());
        return paramData.toString();
    }

    /**
     * 쿼리스트링을 Map에 담아서 반환
     */
    public Map<String, String> queryStringToMap(final String queryString) {
        log.info("queryString: {}", queryString);
        if (StringUtils.isEmpty(queryString)) return null;

        final Map<String, String> resultMap = new HashMap<>();
        for (final String param : queryString.split("&")) {
            final String[] pair = param.split("=");
            resultMap.put(pair[0], (pair.length > 1) ? pair[1] : "");
        }
        return resultMap;
    }

    /**
     * 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
     *
     * @param listUrl 목록 화면의 URL
     * @param searchParam 현재 검색 인자 정보를 담고 있는 BaseSearchParam 객체
     * @return {@link BaseSearchParam} -- 이전 검색 인자가 존재할 경우 해당 검색 인자, 그렇지 않을 경우 원래 searchParam
     */
    public static BaseSearchParam checkPrevSearchParam(final String listUrl, BaseSearchParam searchParam) {

        // 목록 화면으로 돌아온 경우에만 체크
        if (StringUtils.isEmpty(listUrl) || !searchParam.isBackToList()) return searchParam;

        // 세션에서 이전 정보 조회, 이전 정보가 있을 경우 이전 정보 반환
        final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final HttpSession session = servletRequestAttribute.getRequest().getSession();
        // prevListUrl과 prevSearchParam은 한 세트. prevListUrl이 있으면 prevSearchParam 반환
        final String prevListUrl = (String) session.getAttribute("prevListUrl");
        if (StringUtils.isEmpty(prevListUrl) || !listUrl.equals(prevListUrl)) return searchParam;

        return (BaseSearchParam) session.getAttribute("prevSearchParam");
    }

    /**
     * 공통 > pageSize, pageNo로 페이징 요청 정보 생성
     * 
     * @param searchParam 페이징 정보를 포함한 파라미터
     * @param sortParam 정렬 기준
     * @return {@link PageRequest} -- 생성된 PageRequest 객체
     */
    public static PageRequest getPageRequest(final BaseSearchParam searchParam, final String sortParam) throws Exception {
        return getPageRequest(searchParam, sortParam, null);
    }

    /**
     * 공통 > pageSize, pageNo로 페이징 요청 정보 생성
     *
     * @param searchParam 페이징 정보를 포함한 파라미터
     * @param sortParam 정렬 기준
     * @param model ModelMap 객체
     * @return {@link PageRequest} -- 생성된 PageRequest 객체
     */
    public static PageRequest getPageRequest(final BaseSearchParam searchParam, final String sortParam, final ModelMap model) throws Exception {
        final Sort sort = Sort.by(Sort.Direction.DESC, sortParam);
        return getPageRequest(searchParam, sort, model);
    }

    /**
     * 공통 > pageSize, pageNo로 페이징 요청 정보 생성
     *
     * @param searchParam 페이징 정보를 포함한 파라미터
     * @param sort 정렬 기준
     * @param model ModelMap 객체
     * @return {@link PageRequest} -- 생성된 PageRequest 객체
     */
    public static PageRequest getPageRequest(final BaseSearchParam searchParam, final Sort sort, final ModelMap model) throws Exception {
        final Integer pageSize = searchParam.getPageSize();
        final Integer pageNo = searchParam.getPageNo();
        if (model != null) {
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("pageNo", pageNo);
        }
        final int pageIdx = pageNo - 1;
        return PageRequest.of(pageIdx, pageSize, sort);
    }

    /**
     * 공통 > pageSize, pageNo로 페이징 요청 정보 생성
     *
     * @param searchParam 페이징 정보를 포함한 파라미터
     * @param sort 정렬 기준
     * @return {@link PageRequest} -- 생성된 PageRequest 객체
     */
    public static PageRequest getPageRequest(final BaseSearchParam searchParam, final Sort sort) throws Exception {
        return getPageRequest(searchParam, sort, null);
    }

    /**
     * 공통 > 처리를 마친 parameterMap 값을 공백 제거하여 entrySet으로 화면에 추가
     *
     * @param searchParam 페이징 정보를 포함한 파라미터
     * @param listUrl 목록 화면의 URL
     * @param model 모델 맵에 추가할 ModelMap 객체
     */
    public static void setModelAttrMap(final BaseSearchParam searchParam, final String listUrl, final ModelMap model) {
        final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final HttpSession session = servletRequestAttribute.getRequest().getSession();

        // 내 글 보기 체크시 목록 돌아가기 버튼 보여지기 위해 값 저장
        final boolean isMyPapr = !searchParam.isBackToList() && searchParam.isAction(Code.ACTION_TY_MY_PAPR);
        final boolean isBackToMyPapr = searchParam.isBackToList() && (Code.ACTION_TY_MY_PAPR.equals(searchParam.getActionTyCd()));
        if (isMyPapr || isBackToMyPapr) model.addAttribute(Code.ACTION_TY_MY_PAPR, true);

        // 세션?에 목록 검색 인자 저장
        session.setAttribute("prevSearchParam", searchParam);
        session.setAttribute("prevListUrl", listUrl);
    }
}
