package io.nicheblog.dreamdiary.feature.file.controller;

import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.feature.file.service.FileRecordService;
import io.nicheblog.dreamdiary.feature.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
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
 * FileRestController
 * <pre>
 *  파일 처리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class FileRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.FILE;      // 작업 카테고리 (로그 적재용)

    private final FileRecordService fileRecordService;

    /**
     * 파일 유무 여부 체크 (Ajax) - 첨부파일 상세 ID(fileRecordId) 이용.
     * (로그인 사용자만 접근 가능.)
     *
     * @param fileId 파일 ID. 체크할 파일의 고유 식별자
     * @return ResponseEntity -- 응답 객체
     */
    @GetMapping(Url.FILE_DOWNLOAD_CHK_AJAX)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<AjaxResponse> fileChckAjax(
            final @RequestParam("fileId") @Nullable String fileId
    ) {

        final boolean isSuccess = FileUtils.fileChck(fileId);
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        // TODO: 실패시에만 로그 적용하도록

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 파일 목록 정보 조회 (Ajax) - 첨부파일 묶음 ID 이용. (fileGroupId)
     * 비로그인 사용자도 외부에서 접근 가능. (인증 없음)
     *
     * @param fileGroupId - 파일 번호. 조회할 파일의 고유 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.FILE_INFO_LIST_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getFileList(
            final @RequestParam("fileGroupId") @Nullable Integer fileGroupId
    ) throws Exception {

        final List<FileRecordDto> fileList = fileRecordService.getPageDto(fileGroupId);
        final boolean isSuccess = (fileList != null);
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(fileList));
    }

    /**
     * 파일 다운로드 : 첨부파일 상세 ID 이용 (fileRecordId)
     * Ajax로 유무 체크 후 다운로드하므로 항상 파일이 존재한다 가정하고 진행
     * (로그인 사용자만 접근 가능.)
     *
     * @param fileRecordId 파일 상세 번호. 다운로드할 파일의 고유 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.FILE_DOWNLOAD)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<AjaxResponse> fileDownload(
            final @RequestParam("fileRecordId") @Nullable Integer fileRecordId
    ) throws Exception {

        // 파일 정보 조회
        final FileRecordEntity fileRecord = fileRecordService.getDtlEntity(fileRecordId);
        final String orgnFileNm = fileRecord.getOrgnFileNm();
        // 파일 다운로드 처리
        final File file = new File(fileRecord.getFileStrePath(), fileRecord.getStreFileNm());
        FileUtils.downloadFile(file, orgnFileNm);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 파일 업로드 : 업로드 후 AtchDtlFileDto 반환 (filepath 정보 포함)
     * (로그인 사용자만 접근 가능.)
     *
     * @param request Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.FILE_UPLOAD_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> uploadFileAjax(
            final MultipartHttpServletRequest request
    ) throws Exception {

        // 파일 영역 처리 후 업로드 정보 받아서 반환
        final FileRecordDto atchfileDtl = FileUtils.uploadDtlFile(request);
        assert atchfileDtl != null;
        final boolean isSuccess = (atchfileDtl.getId() != null);
        final String rsltMsg =  isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(atchfileDtl));
    }
}
