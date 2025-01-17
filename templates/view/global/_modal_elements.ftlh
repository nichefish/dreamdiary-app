<#--
 * 모달 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<!--
 * 모달:: 타이틀 영역
 -->
<#macro modal_header title>
    <div class="modal-header">
        <h5 id="modal_title" class="modal-title">[${title!}]</h5>
        <div class="btn btn-sm btn-icon btn-active-light-primary ms-2" aria-label="Close"
             onClick="ModalHistory.pop();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.close"/>">
            <i class="fas fa-times"></i>
        </div>
    </div>
</#macro>

<!--
 * 모달:: 타이틀 영역 (with 이전 모달 돌아가기 버튼)
 -->
<#macro modal_header_with_back title>
    <div class="modal-header">
        <h5 id="modal_title" class="modal-title">[${title!}]</h5>
        <div>
            <div class="btn btn-sm btn-icon btn-active-light-primary" aria-label="Close"
                 onclick="ModalHistory.prev();"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.prev"/>">
                <i class="bi bi-box-arrow-in-left"></i>
            </div>
            <div class="btn btn-sm btn-icon btn-active-light-primary" aria-label="Close"
                 onClick="ModalHistory.pop();"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.close"/>">
                <i class="fas fa-times"></i>
            </div>
        </div>
    </div>
</#macro>

<!--
 * 모달:: 타이틀 영역 (다크)
 -->
<#macro modal_header_dark title>
    <div class="modal-header" style="background-color:#41416e;">
        <h5 id="modal_title" class="modal-title">[${title!}]</h5>
        <div class="btn btn-sm btn-icon btn-active-light-primary ms-2" aria-label="Close"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.close"/>">
            <i class="fas fa-times"></i>
        </div>
    </div>
</#macro>

<!--
 * 모달:: 버튼::등록/수정
 -->
<#macro modal_btn_save func>
    <button type="button" class="btn btn-sm btn-primary me-2"
            onclick="${func!}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.save"/>">
        <i class="bi bi-pencil-square"></i>
        <span class="indicator-label">저장</span>
        <span class="indicator-progress">Please wait...
            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
        </span>
    </button>
</#macro>

<!--
 * 모달:: 버튼::수정 모달 호출
 -->
<#macro modal_btn_modify func>
    <button type="button" class="btn btn-sm btn-active-primary"
            onclick="${func!}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.modal.mdf"/>">
        <i class="bi bi-pencil-square"></i>수정
    </button>
</#macro>

<!--
 * 모달:: 버튼::삭제
 -->
<#macro modal_btn_delete func>
    <button type="button" class="btn btn-sm btn-light btn-active-danger"
            onclick="${func!}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.del"/>">
        <i class="bi bi-trash"></i>삭제
    </button>
</#macro>

<!--
 * 모달:: 버튼::모달 닫기
 -->
<#macro modal_btn_close>
    <button type="button" class="btn btn-sm btn-light"
            onClick="ModalHistory.pop();"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.close"/>">
        <i class="bi bi-x"></i>닫기
    </button>
</#macro>

<!--
* 모달:: 버튼::모달 닫기 (닫기 안전장치 기능 추가)
-->
<#macro modal_btn_close_safe func>
    <button type="button" class="btn btn-sm btn-light modal-btn-close-safe"
            data-func="ModalHistory.pop(); <#if func?? && func != "">${func!}</#if>"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.close.safe"/>">
        <i class="bi bi-x"></i>닫기
    </button>
</#macro>