<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 등록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

<style>
    @media (max-width: 768px) {
        #idDupChckBtn{
            font-size:12px;
        }
    }
</style>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Form-->
        <form name="userRegForm" id="userRegForm" class="form mt-10" data-mode="${formMode!}" method="post" enctype="multipart/form-data">
            <input type="hidden" name="userNo" value="${user.userNo!}">
            <input type="hidden" name="atchFileNo" value="${user.atchFileNo!}">
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-lg-center text-sm-end <#if isReg!false>required</#if>">
                        <label for="userId">아이디</label>
                    </div>
                </div>
                <#if isReg!false>
                    <div class="col-xl-2 col-8">
                        <input type="text" name="userId" id="userId" class="form-control form-control-solid required no-space"
                               value="${user.userId!}" maxlength="16" />
                        <div class="form-text">
                            <@spring.message "noti.req.userid"/>
                        </div>
                        <div id="userId_validate_span" class="text-danger"></div>
                        <div id="ipDupChckPassed_validate_span" class="text-danger"></div>
                    </div>
                    <div class="col-xl-2 col-4">
                        <button type="button" class="btn btn-sm btn-secondary required blink" id="idDupChckBtn"
                                onclick="dF.User.idDupChckAjax();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="입력한 아이디에 대한&#10;중복 여부를 확인합니다.">
                            <span class="hidden-table">아이디 </span>중복 확인
                        </button>
                        <input type="hidden" name="ipDupChckPassed" id="ipDupChckPassed" value="N"/>
                    </div>
                </#if>
                <#if isMdf!false>
                    <div class="col-xl-2">
                        <input type="hidden" name="userId" id="userId" value="${user.userId!}" />
                        <div class="fw-bold col-form-label text-start">${user.userId!}</div>
                    </div>
                </#if>
            </div>
            <#if isReg!false>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-end  required">
                            <label for="password">비밀번호</label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="password" name="password" id="password" class="form-control form-control-solid required no-space"
                               maxlength="20" autocomplete="off" />
                        <div class="form-text text-noti">
                            <@spring.message "noti.req.password"/>
                        </div>
                        <div id="password_validate_span"></div>
                    </div>
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-end required">
                            <label for="passwordCf">비밀번호 확인</label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="password" name="passwordCf" id="passwordCf"  class="form-control form-control-solid no-space"
                               maxlength="20" autocomplete="off" />
                        <div id="passwordCf_validate_span"></div>
                    </div>
                </div>
            </#if>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-lg-center text-sm-end required">
                        <label for="authCd" class="cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="계정 권한을 설정합니다.">
                            권한
                            <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                </div>
                <div class="col-xl-2">
                    <select name="authListStr" id="authCd" class="form-select form-select-solid required"
                            data-control="select2" data-allow-clear="true" data-hide-search="true" data-close-on-select="false" multiple="multiple">
                        <#list authRoleList![] as auth>
                            <#assign is_selected = user.authStrList?? && user.authStrList?seq_contains(auth.authCd) />
                            <option value="${auth.authCd!}" <#if is_selected>selected</#if>>${auth.authNm!}</option>
                        </#list>
                    </select>
                    <div id="authCd_validate_span"></div>
                </div>
            </div>
            <#--begin::Row-->
            <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
            <div class="row mb-4" id="nickNmShowDiv" <#if user.userInfo??>style="display:none;"</#if>>
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-lg-center text-sm-start required"
                         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="직원정보 미입력시&#10;사용할 이름입니다.&#10;(외부사용자, 임시계정 등)">
                        <label for="nickNm" class="cursor-help">
                            표시이름
                            <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                </div>
                <div class="col-xl-2">
                    <input type="text" name="nickNm" id="nickNm" class="form-control form-control-solid required"
                           value="${user.nickNm!}" placeholder="표시이름" maxlength="20" />
                    <div class="form-text text-noti">
                        <@spring.message "noti.req.nicknm"/>
                    </div>
                    <span id="nickNm_validate_span"></span>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                        <label for="emailId" class="cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="이메일 주소입니다. 알림 및 계정 복구 등에 사용됩니다.">
                            E-mail
                            <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                </div>
                <div class="col-lg-2 col-4">
                    <input type="text" name="emailId" id="emailId" class="form-control form-control-solid no-space"
                           value="${user.emailId!}" placeholder="이메일 ID 입력" maxlength="20" />
                    <div id="emailId_validate_span"></div>
                </div>
                <div class="col-xl-2 col-1 d-flex-center fw-bold text-gray-700" style="width:2%;">
                    <label for="emailDomain"><label for="emailDomainSelect">@</label></label>
                </div>
                <div class="col-lg-2 col-5 vertical-center">
                    <input type="text" name="emailDomain" id="emailDomain" class="form-control form-control-solid no-space"
                           value="${user.emailDomain!"gmail.com"}" placeholder="직접입력" maxlength="20" />
                    <div id="emailDomain_validate_span"></div>
                </div>
                <div class="col-xl-2 col-1">
                    <select name="emailDomainSelect" id="emailDomainSelect" class="form-select form-select-solid">
                        <option value="">직접입력</option>
                        <option value="gmail.com" <#if isReg!false>selected</#if>>gmail.com</option>
                        <option value="naver.com">naver.com</option>
                        <option value="kakao.com">kakao.com</option>
                    </select>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-sm-start text-lg-center">
                        <label for="cttpc">
                            전화번호
                        </label>
                    </div>
                </div>
                <div class="col-xl-2">
                    <input type="text" name="cttpc" id="cttpc" class="form-control form-control-solid"
                           value="${user.cttpc!}" maxlength="20" />
                    <div class="form-text text-noti"><@spring.message "noti.req.cttpc"/></div>
                    <span id="cttpc_valid_span"></span>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-lg-center text-sm-start ">
                        <label for="acsIpListStr" class="cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="특정 IP에서만&#10;접속 가능하도록 설정합니다.">
                            접속 IP
                            <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                </div>
                <div class="col-xl-2">
                    <div class="form-check form-switch form-check-custom form-check-solid mt-2">
                        <input type="checkbox" name="useAcsIpYn" id="useAcsIpYn" class="form-check-input" value="Y"
                               <#if user.useAcsIp!false>checked="checked"</#if> />
                        <label class="form-check-label fw-bold ms-3" for="useAcsIpYn" id="useAcsIpYnLabel" style="color:<#if user.useAcsIp!false>blue<#else>gray</#if>;">
                            <#if !user.useAcsIp!false>미</#if>사용
                        </label>
                    </div>
                    <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                    <div id="acsIpListSpan" class="mt-2 mb-0" <#if !(user.useAcsIp!false)>style=display:none;"</#if>>
                        <input type="text" name="acsIpListStr" id="acsIpListStr" class="form-control form-control-solid no-space"
                               value="${user.acsIpListStr!}" maxlength="500" autocomplete="off" />
                        <div class="form-text text-noti"><@spring.message "noti.req.acsip"/></div>
                        <div id="acsIpListStr_valid_span"></div>
                    </div>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2">
                    <div class="fw-bold col-form-label text-sm-start text-lg-center">
                        <label for="cn" class="cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="계정에 대한 관리용 정보를&#10;입력합니다.&#10;(입력한 내용은 사용자에게&#10;노출되지 않습니다.)">
                            계정 설명
                            <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                </div>
                <div class="col-xl-9">
                    <textarea name="cn" id="cn" class="form-control form-control-solid h-100px" wrap="hard" maxlength="4000" placeholder="비고 사항을 입력합니다.">${user.cn!}</textarea>
                </div>
            </div>

            <#--사용자 프로필 정보 영역-->
            <div id="user_profl_div">
                <#--handlebars template goes in here-->
            </div>

            <#--사용자 인사정보 영역-->
            <div id="user_emplym_div">
                <#--handlebars template goes in here-->
            </div>
        </form>
        <!--end:Form-->
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-between">
            <div>
                <div class="d-flex gap-2">
                    <div id="user_profl_reg_btn_div">
                        <button type="button" id="userProflBtn" class="btn btn-sm btn-primary blink"
                                onclick="dF.UserProfl.enableUserProfl();"
                                data-profl="${user.profl??}"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="사용자 프로필 정보를&#10;추가합니다.">
                            프로필 정보 추가+
                        </button>
                    </div>
                    <div>
                        <button type="button" id="userEmplymBtn" class="btn btn-sm btn-primary blink"
                                onclick="dF.UserEmplym.enableUserEmplym();"
                                data-emplym="${user.emplym??}"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="입사인원에 대하여&#10;직원 인사정보를 추가합니다.">
                            직원 인사정보 추가+
                        </button>
                        <div class="text-noti">
                            <@spring.message "noti.req.user.emplym"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="gap-2">
                <#--페이지:: 버튼: 저장하기-->
                <@component.post_btn_submit "cF.util.submit('#userRegForm');" />
                <#--페이지:: 버튼: 목록으로 가기-->
                <@component.post_btn_toList "dF.User.list();" />
            </div>
        </div>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="userNo" value="${user.userNo!}">
</form>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/user/user_module.js?${releaseDate!}"></script>

<#--include::user_emplym_modules-->
<#include "/view/domain/user/emplym/_user_emplym_reg_template.hbs">
<script type="text/javascript" src="/js/view/domain/user/emplym/user_emplym_module.js?${releaseDate!}"></script>

<#--include::user_profl_modules-->
<#include "/view/domain/user/profl/_user_profl_reg_template.hbs">
<script type="text/javascript" src="/js/view/domain/user/profl/user_profl_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/user/user_reg_form.js?${releaseDate!}"></script>

</@layout.layout>