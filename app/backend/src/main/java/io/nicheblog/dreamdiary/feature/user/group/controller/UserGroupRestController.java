package io.nicheblog.dreamdiary.feature.user.group.controller;

import io.nicheblog.dreamdiary.auth.permission.model.PermissionDto;
import io.nicheblog.dreamdiary.feature.user.group.model.UserGroupDto;
import io.nicheblog.dreamdiary.feature.user.group.service.UserGroupService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * UserGroupRestController
 * <pre>
 *  사용자 그룹 관리 REST API. 그룹 CRUD, 멤버십, 그룹 permission 부여.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class UserGroupRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_GROUP_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_GROUP;

    private final UserGroupService userGroupService;

    @GetMapping(Url.USER_GROUPS)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public ResponseEntity<AjaxResponse> listAjax(
            @RequestParam(required = false) final String searchKeyword,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) {
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder", "groupKey"));
        final Page<UserGroupDto> pageResult = userGroupService.getPage(searchKeyword, pageRequest);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(pageResult));
    }

    @GetMapping(Url.USER_GROUP)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public ResponseEntity<AjaxResponse> dtlAjax(@PathVariable final Integer id) {
        final UserGroupDto dto = userGroupService.getDtl(id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(dto));
    }

    @PostMapping(Url.USER_GROUPS)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public ResponseEntity<AjaxResponse> registAjax(@Valid @RequestBody final UserGroupDto dto) throws Exception {
        final ServiceResponse result = userGroupService.regist(dto);
        final String rsltMsg = Boolean.TRUE.equals(result.getRslt())
                ? MessageUtils.getMessage("common.result.success")
                : MessageUtils.getMessage("common.result.failure");
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @PutMapping(Url.USER_GROUP)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public ResponseEntity<AjaxResponse> modifyAjax(
            @PathVariable final Integer id,
            @Valid @RequestBody final UserGroupDto dto
    ) throws Exception {
        final ServiceResponse result = userGroupService.modify(id, dto);
        final String rsltMsg = Boolean.TRUE.equals(result.getRslt())
                ? MessageUtils.getMessage("common.result.success")
                : MessageUtils.getMessage("common.result.failure");
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @DeleteMapping(Url.USER_GROUP)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public ResponseEntity<AjaxResponse> deleteAjax(@PathVariable final Integer id) {
        final ServiceResponse result = userGroupService.delete(id);
        final String rsltMsg = Boolean.TRUE.equals(result.getRslt())
                ? MessageUtils.getMessage("common.result.success")
                : MessageUtils.getMessage("common.result.failure");
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @GetMapping(Url.PERMISSIONS)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public ResponseEntity<AjaxResponse> permissionsAjax() {
        final List<PermissionDto> list = userGroupService.listActivePermissions();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withList(list));
    }
}
