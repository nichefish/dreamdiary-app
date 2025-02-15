<#--
 * 버튼 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<!--
 * 버튼 : 권한/수정권한(mdfable)에 따른 수정 버튼
 -->
<#macro post_btn_modify func post isMngr>
    <#if post.isRegstr>
        <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                onclick="${func!}"
                data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.page.mdf'/>">
            <i class="bi bi-pencil-square"></i>(내 글)수정
        </button>
    <#elseif post.mdfable??>
        <!-- 1. mdfable 항목 사용시 -->
        <!-- 수정권한에 따라 수정 권한 부여 -->
        <#if post.mdfable! == (Constant.MDFABLE_MNGR)>
            <#if isMngr!false>
                <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                        onclick="${func!}"
                        data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.page.mdf'/>">
                    <i class="bi bi-pencil-square"></i>(관리자+)수정
                </button>
            </#if>
        <#elseif post.mdfable! == (Constant.MDFABLE_USER)>
            <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                    onclick="${func!}"
                    data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.page.mdf'/>">
                <i class="bi bi-pencil-square"></i>(사용자+)수정
            </button>
        <#elseif post.mdfable! == (Constant.MDFABLE_ALL)>
            <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                    onclick="${func!}"
                    data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.page.mdf'/>">
                <i class="bi bi-pencil-square"></i>(전체+)수정
            </button>
        </#if>
    <#else>
        <!-- 1. mdfable 항목 미사용시 : 등록자/관리자만 수정 가능 -->
        <#if isMngr!false>
            <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                    onclick="${func!}"
                    data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.page.mdf'/>">
                <i class="bi bi-pencil-square"></i>(관리자+)수정
            </button>
        </#if>
    </#if>
</#macro>

<!--
 * 버튼 : 권한에 따른 삭제 버튼
 -->
<#macro post_btn_delete func post isMngr>
    <#if post.isRegstr>
        <button type="button" class="btn btn-sm btn-light btn-active-danger me-2"
                onclick="${func!}"
                data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.del'/>">
            <i class="bi bi-trash"></i>(내 글)삭제
        </button>
    <#elseif isMngr!false>
        <button type="button" class="btn btn-sm btn-light btn-active-danger me-2 btn-sm"
                onclick="${func!}"
                data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.del'/>">
            <i class="bi bi-trash"></i>(관리자+)삭제
        </button>
    </#if>
</#macro>

<!--
 * 버튼 : 목록으로 가기 버튼
 -->
<#macro post_btn_toList func>
    <button type="button" class="btn btn-sm btn-light"
            onclick="${func!}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.page.list'/>">
        <span class="indicator-label">
            <i class="bi bi-list"></i>목록
        </span>
    </button>
</#macro>


<!--
 * 버튼 : 글 작성 전 미리보기 팝업 호출
 -->
<#macro post_btn_preview_pop func>
    <button type="button" class="btn btn-sm btn-primary mx-2"
            onclick="${func!}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
            title="작성한 내용에 대하여&#10;미리보기 팝업을 호출합니다.">
        <i class="bi bi-pencil-square"></i>
        <span class="indicator-label">미리보기</span>
        <span class="indicator-progress">Please wait...
            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
        </span>
    </button>
</#macro>

<!--
 * 버튼 : 글 저장 / 취소
 -->
<#macro post_btn_submit func>
    <button type="button" class="btn btn-sm btn-primary mx-2"
            onclick="${func!}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.save'/>">
        <i class="bi bi-pencil-square"></i>
        <span class="indicator-label">저장</span>
        <span class="indicator-progress">Please wait...
            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
        </span>
    </button>
</#macro>

<!--
 * 버튼 : 미리보기 팝업 닫기
 -->
<#macro popup_btn_preview_close closerNm>
    <button type="button" class="btn btn-sm btn-light"
            onclick="${closerNm!}.close();"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="팝업 창을 닫습니다.">
        <i class="bi bi-list"></i>닫기
    </button>
</#macro>
