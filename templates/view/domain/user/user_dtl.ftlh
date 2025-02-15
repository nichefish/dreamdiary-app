<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 상세 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Row-->
        <div class="row mb-1">
            <div class="col-xl-2 col-4 d-flex-center">
                <label class="fw-bold">승인여부</label>
            </div>
            <div class="col-xl-1 col-2 d-flex align-items-center">
                <#if (user.isCf)!true>
                    <span class="text-success">
                        승인<i class="bi bi-check text-success"></i>
                    </span>
                <#else>
                    <span class="text-muted">
                        미승인<i class="bi bi-dash fs-9 text-muted blink"></i>
                    </span>
                </#if>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                <#if (user.isCf)!true>
                    <div class="btn btn-sm btn-danger" onclick="dF.User.uncfAjax();">
                        승인 취소
                    </div>
                <#else>
                    <div class="btn btn-sm btn-secondary" onclick="dF.User.cfAjax();">
                        승인 처리
                    </div>
                </#if>
            </div>
        </div>

        <div class="separator my-8"></div>

        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4 d-flex-center">
                <label class="fw-bold">아이디</label>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                <div class="btn btn-icon btn-secondary position-relative w-30px h-30px w-md-40px h-md-40px me-3">
                    <#if user.proflImgUrl??>
                        <img src="${user.proflImgUrl!}" class="img-thumbnail p-0 w-100" />
                    <#else>
                        <span class="svg-icon svg-icon-1">
                            <i class="fas fa-user-circle fs-2"></i>
                        </span>
                    </#if>
                </div>
                ${user.userId!}
            </div>
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">등록자</div>
            </div>
            <div class="col-xl-6 col-8 pc-d-flex col-form-label">
                <div class="col-xl-2 col-12">${user.regstrId!}</div>
                <div class="col-xl-6 col-12"> (${user.regDt!})</div>
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 d-flex-center">
                <label class="fw-bold">비밀번호</label>
            </div>
            <div class="col-xl-2">
                <button type="button" class="btn btn-sm btn-secondary"
                        onclick="dF.User.pwResetAjax();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="비밀번호를 초기화합니다.&#10;로그인 정책 관리 메뉴에서&#10;초기화 값을 설정할 수&#10;있습니다.">
                    비밀번호 초기화
                </button>
            </div>
            <div class="col-xl-8">
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="authNm">권한</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                <#list user.authList![] as auth>
                    <#if (auth.authCd! == Constant.AUTH_MNGR)>
                        <i class="bi bi-person-lines-fill text-info ms-1 opacity-75"></i>   <!-- 관리자 -->
                    <#elseif (auth.authCd! == Constant.AUTH_USER)>
                        <i class="bi bi-people-fill ms-1"></i>        <!-- 사용자 -->
                    <#elseif (auth.authCd! == Constant.AUTH_DEV)>
                        <i class="bi bi-person-fill-gear ms-1"></i>        <!-- 개발자 -->
                    </#if>
                    ${auth.authNm!}
                </#list>
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="nickNm">표시이름</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                ${user.nickNm!}
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="email">이메일</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                ${user.email!}
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="cttpc">연락처</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                ${user.cttpc!}
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">잠금여부</div>
            </div>
            <div class="col-xl-9 col-8 col-form-label">
                <#if user.isLocked!false>
                    <div class="text-danger">
                        잠김<i class="bi bi-lock fs-9 text-danger"></i>
                    </div>
                <#else>
                    <div class="text-success">
                        <@spring.message 'use'/><i class="bi bi-check text-success"></i>
                    </div>
                </#if>
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="acsIpListStr">접속IP</label>
                </div>
            </div>
            <div class="col-xl-2 col-4">
                <div class="col-form-label">
                    <#if user.useAcsIp!false>
                        <div class="text-success">
                            <@spring.message 'use'/><i class="bi bi-check text-success"></i>
                        </div>
                    <#else>
                        <div class="text-muted">
                            <@spring.message 'unuse'/><i class="bi bi-dash fs-9 text-muted"></i>
                        </div>
                    </#if>
                </div>
                <div class="form-check form-switch form-check-custom form-check-solid gap-4">
                    <#if user.useAcsIp! && user.acsIpList??>
                        <#list user.acsIpList![] as item>
                            <span class="div-textarea div-height-1 px-5">${item.acsIp}</span>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
        <#--begin::Row-->
        <div class="row">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">계정 설명</div>
            </div>
            <div class="col-xl-9 col-8 col-form-label">
                <div class="div-textarea h-auto min-h-125px">${user.cn!}</div>
            </div>
        </div>
    </div>
    <#--페이지:: 사용자 프로필 정보 영역-->
    <#if user.profl??>
        <#include "/view/domain/user/profl/_user_profl_dtl_area.ftlh">
    </#if>
    <#--페이지:: 사용자 인사정보 영역-->
    <#if user.emplym??>
        <#include "/view/domain/user/emplym/_user_emplym_dtl_area.ftlh">
    </#if>
    <#--begin::Card footer-->
    <div class="card-footer d-flex justify-content-end py-6 px-9">
        <#--페이지:: 버튼: 수정하기-->
        <@component.post_btn_modify "dF.User.mdfForm();" user isMngr />
        <#--페이지:: 버튼: 삭제하기-->
        <@component.post_btn_delete "dF.User.delAjax(${post.postNo});" user isMngr />
        <#--페이지:: 버튼: 목록으로 가기-->
        <@component.post_btn_toList "dF.User.list();" />
    </div>
    <!--end::Card footer-->
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="userNo" value="${user.userNo!}">
    <#if user.profl??>
        <input type="hidden" name="userProflNo" value="${user.profl.userProflNo!}">
    </#if>
</form>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/user/user_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/user/user_dtl.js?${releaseDate!}"></script>

</@layout.layout>