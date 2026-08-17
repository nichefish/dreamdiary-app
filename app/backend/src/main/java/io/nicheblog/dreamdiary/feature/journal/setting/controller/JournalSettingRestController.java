package io.nicheblog.dreamdiary.feature.journal.setting.controller;

import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalUserSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.service.JournalSettingService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * JournalSettingRestController
 * <pre>
 *  저널 도메인 설정 REST 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalSettingRestController {

    private final JournalSettingService journalSettingService;

    /**
     * 저널 전역 설정을 조회한다.
     *
     * @return Ajax 응답
     */
    @GetMapping(value = Url.JOURNAL_SETTINGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> getSettings() {
        final JournalSettingDto setting = journalSettingService.getAdminSetting();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(setting));
    }

    /**
     * 저널 전역 설정을 갱신한다.
     *
     * @param dto 갱신할 설정
     * @return Ajax 응답
     */
    @PutMapping(value = Url.JOURNAL_SETTINGS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> updateSettings(
            final @RequestBody JournalSettingDto dto
    ) {
        final JournalSettingDto updated = journalSettingService.updateAdminSetting(dto);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(updated));
    }

    /**
     * 로그인 사용자의 저널 설정을 조회한다.
     *
     * @return Ajax 응답
     */
    @GetMapping(value = Url.JOURNAL_MY_SETTINGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> getMySettings() {
        final JournalUserSettingDto setting = journalSettingService.getMySetting();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(setting));
    }

    /**
     * 로그인 사용자의 저널 설정을 갱신한다.
     *
     * @param dto 갱신할 사용자 저널 설정
     * @return Ajax 응답
     */
    @PutMapping(value = Url.JOURNAL_MY_SETTINGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> updateMySettings(
            final @Valid @RequestBody JournalUserSettingDto dto
    ) {
        final JournalUserSettingDto updated = journalSettingService.updateMySetting(dto);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(updated));
    }
}
