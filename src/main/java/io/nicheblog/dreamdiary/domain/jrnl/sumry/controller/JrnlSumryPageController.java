package io.nicheblog.dreamdiary.domain.jrnl.sumry.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayTagService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryTagService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamTagService;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumrySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.JrnlSumryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global._common.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import java.util.List;

/**
 * JrnlSumryPageController
 * <pre>
 *  저널 결산 페이지 Controller.
 * </pre>
 *
 * @see LogActvtyPageControllerAspect
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class JrnlSumryPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_SUMRY_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlSumryService jrnlSumryService;
    private final JrnlDiaryService jrnlDiaryService;
    private final JrnlDreamService jrnlDreamService;
    private final JrnlDayTagService jrnlDayTagService;
    private final JrnlDiaryTagService jrnlDiaryTagService;
    private final JrnlDreamTagService jrnlDreamTagService;
    private final DtlCdService dtlCdService;

    /**
     * 저널 결산 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.JRNL_SUMRY_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSumryPage(
            @ModelAttribute("searchParam") JrnlSumrySearchParam searchParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SUMRY);
        model.addAttribute("pageNm", PageNm.LIST);

        // 전체 통계 조회
        final JrnlSumryDto totalSumry = jrnlSumryService.getTotalSumry();
        model.addAttribute("totalSumry", totalSumry);
        // 목록 조회 및 모델에 추가
        final List<JrnlSumryDto.LIST> jrnlSumryList = jrnlSumryService.getMyListDto(searchParam);
        model.addAttribute("jrnlSumryList", jrnlSumryList);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/jrnl/sumry/jrnl_sumry_list";
    }

    /**
     * 저널 결산 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(value = Url.JRNL_SUMRY_DTL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSumryDtl(
            final @RequestParam("postNo") @Nullable Integer key,
            final @RequestParam("yy") @Nullable Integer yyParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SUMRY);
        model.addAttribute("pageNm", PageNm.DTL);

        // 객체 조회 및 모델에 추가
        final JrnlSumryDto retrievedDto = key != null ? jrnlSumryService.getSumryDtl(key) : yyParam != null ? jrnlSumryService.getDtlDtoByYy(yyParam) : null;
        model.addAttribute("post", retrievedDto);
        assert retrievedDto != null;
        final Integer yy = retrievedDto.getYy();
        // 중요 일기 목록 조회
        model.addAttribute("imprtcDiaryList", jrnlDiaryService.getImprtcDiaryList(yy));
        // 중요 꿈 목록 조회
        model.addAttribute("imprtcDreamList", jrnlDreamService.getImprtcDreamList(yy));

        // 일자 태그 목록 조회
        final List<TagDto> jrnlDayTagList = jrnlDayTagService.getDaySizedListDto(yy, 99);
        model.addAttribute("dayTagList", jrnlDayTagList);
        // 일기 태그 목록 조회
        final List<TagDto> jrnlDiaryTagList = jrnlDiaryTagService.getDiarySizedListDto(yy, 99);
        model.addAttribute("diaryTagList", jrnlDiaryTagList);
        // 꿈 태그 목록 조회
        final List<TagDto> jrnlDreamTagList = jrnlDreamTagService.getDreamSizedListDto(yy, 99);
        model.addAttribute("dreamTagList", jrnlDreamTagList);
        // 코드 데이터 모델에 추가
        dtlCdService.setCdListToModel(Constant.JRNL_SUMRY_TY_CD, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/jrnl/sumry/jrnl_sumry_dtl";
    }
}
