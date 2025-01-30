package io.nicheblog.dreamdiary.global.util.cmm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Date;
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
class ParamModule {

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
     * @throws Exception 페이징 요청 정보 생성 중 발생할 수 있는 예외
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
     * @throws Exception 페이징 요청 정보 생성 중 발생할 수 있는 예외
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
     * @throws Exception 페이징 요청 정보 생성 중 발생할 수 있는 예외
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
     * @throws Exception 페이징 요청 정보 생성 중 발생할 수 있는 예외
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
        final boolean isMyPapr = !searchParam.isBackToList() && searchParam.isAction(Constant.ACTION_TY_MY_PAPR);
        final boolean isBackToMyPapr = searchParam.isBackToList() && (Constant.ACTION_TY_MY_PAPR.equals(searchParam.getActionTyCd()));
        if (isMyPapr || isBackToMyPapr) model.addAttribute(Constant.ACTION_TY_MY_PAPR, true);

        // 세션?에 목록 검색 인자 저장
        session.setAttribute("prevSearchParam", searchParam);
        session.setAttribute("prevListUrl", listUrl);
    }

    /**
     * 공통 > 목록 검색 parameter 빈 값 걸러내고 정돈
     *
     * @param searchParam 필터링할 BaseSearchParam 객체
     * @return {@link BaseSearchParam} -- 필터링된 BaseSearchParam 객체
     * @throws Exception 파라미터 처리 중 발생할 수 있는 예외
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
        if (CollectionUtils.isEmpty(searchParamMap)) return new BaseSearchParam();
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(searchParamMap, BaseSearchParam.class);
    }

    /**
     * 공통 > 목록 검색 parameterMap 빈 값 걸러내고 정돈.
     *
     * @param searchParamMap 필터링할 파라미터 맵
     * @return {@link Map} -- 필터링된 파라미터 맵
     * @throws Exception 파라미터 처리 중 발생할 수 있는 예외
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
                if (key.startsWith("searchKeyword")) continue;
                if (key.endsWith("Dt")) continue;
                filteredSearchKey.put(key, value);
            }
        }
        return filteredSearchKey;
    }
}