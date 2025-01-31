package io.nicheblog.dreamdiary.domain.flsys.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.file.utils.FileUtils;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysDto;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysSearchParam;
import io.nicheblog.dreamdiary.domain.flsys.service.FlsysService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nullable;

/**
 * FlsysController
 * <pre>
 *  파일시스템 컨트롤러.
 * </pre>
 * TODO: 보완 예정
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class FlsysController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.FLSYS_HOME;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.FLSYS;        // 작업 카테고리 (로그 적재용)

    private final FlsysService flsysService;

    /**
     * 파일시스템 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.FLSYS_HOME)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String flsysList(
            final @ModelAttribute("searchParam") FlsysSearchParam searchParam,
            final @RequestParam("filePath") @Nullable String filePathParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.FLSYS);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 활동 로그 목록 조회
        final String filePath = !StringUtils.isEmpty(filePathParam) ? filePathParam : Constant.HOME_FLSYS;
        final FlsysDto flsys = flsysService.getFlsysByPath(filePath);
        model.addAttribute("file", flsys);

        boolean isSuccess = true;
        String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return "/view/global/_common//flsys/flsys_home";
    }

    /**
     * 파일시스템 정보 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param filePath 조회할 파일 경로
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return 파일 시스템 정보를 담은 AjaxResponse 객체를 ResponseEntity로 반환
     * @see LogActvtyEventListener
     */
    @GetMapping(value = Url.FLSYS_LIST_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysListAjax(
            final @RequestParam("filePath") String filePath,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            // 해당하는 경로의 파일시스템 정보 조회
            if (!filePath.startsWith(Constant.HOME_FLSYS)) throw new IllegalArgumentException("허용되지 않은 경로입니다.");
            final FlsysDto file = flsysService.getFlsysByPath(filePath);
            ajaxResponse.setRsltObj(file);

            isSuccess = true;
            rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);
        } catch (final Exception e) {
            isSuccess = false;
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
            // 로그 관련 세팅
            logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
            publisher.publishEvent(new LogActvtyEvent(this, logParam));
        }

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 파일 시스템 파일 다운로드
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param filePath 조회할 파일 경로
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @see LogActvtyEventListener
     */
    @GetMapping(Url.FLSYS_FILE_DOWNLOAD)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public void flsysFileDownload(
            final @RequestParam("filePath") String filePath,
            final LogActvtyParam logParam
    ) throws Exception {

        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            if (!filePath.startsWith(Constant.HOME_FLSYS)) throw new IllegalArgumentException("허용되지 않은 경로입니다.");

            final FlsysDto file = flsysService.getFlsysByPath(filePath);
            // 응답 헤더 설정 및 한글 파일명 처리 (메소드 분리)
            FileUtils.downloadFile(file.getFile());

            isSuccess = true;
            rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);
        } catch (final Exception e) {
            isSuccess = false;
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
            MessageUtils.alertMessage(rsltMsg, baseUrl);
        } finally {
            // 로그 관련 세팅
            logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
            publisher.publishEvent(new LogActvtyEvent(this, logParam));
        }
    }
}
