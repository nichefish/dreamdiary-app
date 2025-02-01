package io.nicheblog.dreamdiary.global._common.file.controller;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.global._common.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.global._common.file.service.AtchFileDtlService;
import io.nicheblog.dreamdiary.global._common.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

/**
 * AtchFileRestController
 * <pre>
 *  파일 처리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class AtchFileRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.FILE;      // 작업 카테고리 (로그 적재용)

    private final AtchFileDtlService atchFileDtlService;

    /**
     * 파일 유무 여부 체크 (Ajax) - 첨부파일 상세 ID(atchFileDtlNo) 이용.
     * (로그인 사용자만 접근 가능.)
     *
     * @param fileId 파일 ID. 체크할 파일의 고유 식별자
     * @param logParam 로그 관련 파라미터. 처리 과정에서 필요한 로그 정보를 포함
     * @return ResponseEntity -- 응답 객체
     */
    @GetMapping(Url.FILE_DOWNLOAD_CHK_AJAX)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<AjaxResponse> fileChckAjax(
            final @RequestParam("fileId") @Nullable String fileId,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = FileUtils.fileChck(fileId);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // TODO: 실패시에만 로그 적용하도록

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 파일 목록 정보 조회 (Ajax) - 첨부파일 묶음 ID 이용. (atchFileNo)
     * 비로그인 사용자도 외부에서 접근 가능. (인증 없음)
     *
     * @param atchFileNo - 파일 번호. 조회할 파일의 고유 식별자
     * @param logParam 로그 관련 파라미터. 처리 과정에서 필요한 로그 정보를 포함
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 다운로드 중 발생할 수 있는 예외
     */
    @GetMapping(Url.FILE_INFO_LIST_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getFileList(
            final @RequestParam("atchFileNo") @Nullable Integer atchFileNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final List<AtchFileDtlDto> fileList = atchFileDtlService.getPageDto(atchFileNo);
        final boolean isSuccess = (fileList != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltList(fileList);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 파일 다운로드 : 첨부파일 상세 ID 이용 (atchFileDtlNo)
     * Ajax로 유무 체크 후 다운로드하므로 항상 파일이 존재한다 가정하고 진행
     * (로그인 사용자만 접근 가능.)
     *
     * @param atchFileDtlNo 파일 상세 번호. 다운로드할 파일의 고유 식별자
     * @param logParam 로그 관련 파라미터. 처리 과정에서 필요한 로그 정보를 포함
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 다운로드 중 발생할 수 있는 예외
     */
    @GetMapping(Url.FILE_DOWNLOAD)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<AjaxResponse> fileDownload(
            final @RequestParam("atchFileDtlNo") @Nullable Integer atchFileDtlNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 파일 정보 조회
        final AtchFileDtlEntity atchFileDtl = atchFileDtlService.getDtlEntity(atchFileDtlNo);
        final String orgnFileNm = atchFileDtl.getOrgnFileNm();
        // 파일 다운로드 처리
        final File file = new File(atchFileDtl.getFileStrePath(), atchFileDtl.getStreFileNm());
        FileUtils.downloadFile(file, orgnFileNm);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 파일 업로드 : 업로드 후 AtchDtlFileDto 반환 (filepath 정보 포함)
     * (로그인 사용자만 접근 가능.)
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param request Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 파일 업로드 중 발생할 수 있는 예외
     */
    @PostMapping(Url.FILE_UPLOAD_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> uploadFileAjax(
            final MultipartHttpServletRequest request,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 파일 영역 처리 후 업로드 정보 받아서 반환
        final AtchFileDtlDto atchfileDtl = FileUtils.uploadDtlFile(request);
        assert atchfileDtl != null;
        final boolean isSuccess = (atchfileDtl.getAtchFileDtlNo() != null);
        final String rsltMsg =  MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(atchfileDtl);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }
}
