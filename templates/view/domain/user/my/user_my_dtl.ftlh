<#--
 * 내 정보(user_my) > 상세 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script>
    /** Model 값을 객체에 담아 typescript로 전달 */
    const Model = (function() {
        return {
            errorMsg: "${errorMsg!}",
        }
    })();
</script>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4 d-flex-center">
                <label class="fs-6 fw-bold">아이디</label>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                <div class="btn btn-icon btn-light-primary position-relative w-30px h-30px w-md-40px h-md-40px me-3 cursor-default"
<#--                         onclick="dF.UserMy.viewOrgnlProflImg();"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="원본 이미지를 조회합니다."-->
                >
                    <label class="position-absolute top-0 start-100 translate-middle badge badge-sm badge-circle badge-light-primary opacity-hover cursor-pointer"
                           onclick="dF.UserMy.uploadProflImg();"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="프로필 이미지를 변경합니다.">
                        <i class="bi bi-pen icon-xs text-primary"></i>
                    </label>
                    <span class="position-absolute top-100 start-100 translate-middle badge badge-sm badge-circle badge-light-danger opacity-hover cursor-pointer"
                          onclick="dF.UserMy.removeProflImg();"
                          data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="프로필 이미지를 삭제합니다.">
                        <i class="bi bi-x icon-xs text-danger"></i>
                    </span>
                    <#if user.proflImgUrl??>
                        <img src="${user.proflImgUrl!}" class="img-thumbnail p-0 w-100" />
                    <#else>
                        <span class="svg-icon svg-icon-1">
                            <i class="fas fa-user-circle fs-2 blink"></i>
                        </span>
                    </#if>
                    <#--end::Svg Icon-->
                </div>
                ${user.userId!}
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fs-6 fw-bold">비밀번호</div>
            </div>
            <div class="col-xl-2 col-5">
                <button type="button" class="btn btn-sm btn-secondary"
                        onclick="dF.UserMyPwChg.myPwChgModal();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="비밀번호 변경 팝업을&#10;호출합니다.">
                    비밀번호 변경
                </button>
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fs-6 fw-bold"><label for="authNm">권한</label></div>
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
                <div class="col-form-label text-center fs-6 fw-bold"><label for="nickNm">표시이름</label></div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                ${user.nickNm!}
            </div>
        </div>
        <#--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fs-6 fw-bold">
                    <label for="acsIpListStr">접속IP</label>
                </div>
            </div>
            <div class="col-xl-9 col-8 col-form-label">
                <div class="form-check form-switch form-check-custom form-check-solid">
                    <span class="me-8">
                        <#if user.isAcsIpY!false>사용<#else>미사용</#if>
                    </span>
                    <#if user.isAcsIpY!false && user.acsIpList??>
                        <#list user.acsIpList![] as item>
                            <span class="div-textarea div-height-1 me-4">${item.acsIp!}</span>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <#if user.userInfo??>
        <div class="separator my-2"></div>

        <#--begin::Card body-->
        <div class="card-body">
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold"><label for="cttpc">부서</label></div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    ${user.userInfo.cmpyNm!"-"} / ${user.userInfo.teamNm!"-"} / ${user.userInfo.emplymNm!"-"}
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">직급</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    ${(user.userInfo.rankNm)!"-"}
                    <#if ((user.userInfo.rankCd)! == "STAFF") && ((user.userInfo.apntcYn)! == "Y")>
                        <span class="text-muted">(수습)</span>
                    </#if>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">입사일</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    ${(user.userInfo.ecnyDt)!"-"}
                </div>
                <#if (((user.userInfo.retireYn)!"N") == "Y")!false>
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">퇴사일</div>
                    </div>
                    <div class="col-xl-2 col-4 col-form-label">
                        ${user.userInfo.retireDt!"-"}
                    </div>
                </#if>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">전화번호</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    ${user.cttpc!"-"}
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">E-mail</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    ${user.email!"-"}
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">생년월일</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    ${user.userInfo.brthdy!"-"}
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">계좌번호</div>
                </div>
                <div class="col-xl-8 col-8 col-form-label">
                    ${user.userInfo.acntBank!"-"} <span class="px-3">|</span> ${user.userInfo.acntNo!"-"}
                </div>
            </div>
            <#if user.userInfo.itemList?has_content>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">추가정보</div>
                    </div>
                    <div class="col-xl-9 col-8 col-form-label" id="userItemListDiv">
                        <#if user.userInfo?? && user.userInfo.itemList?has_content>
                            <#list user.userInfo.itemList![] as item>
                                <div class="row mb-2 ps-2 border-bottom border-1 text-muted">
                                    <div class="col-xl-2 col-3 text-start">${item.itemNm!}</div>
                                    <div class="col-xl-4 col-4 text-start">${item.itemCn!}</div>
                                    <div class="col-xl-4 col-4 text-start">${item.itemDc!}</div>
                                </div>
                            </#list>
                        </#if>
                    </div>
                </div>
            </#if>
        </div>
    </#if>

    <div class="separator my-2"></div>

    <!-- 내 휴가 정보 -->
    <#if (authInfo.hasEcnyDt!false)>
        <#--begin::Card body-->
        <div class="card-body">
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-3 col-12">
                    <div class="fs-6 fw-bold text-center">
                        ${vcatnYy.statsYy!}년 연차 사용 현황<br />
                        (${vcatnYy.bgnDt!} ~ ${vcatnYy.endDt!})
                    </div>
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="기본연차: ${vcatnStats.bsYryc!0}<#if vcatnStats.isNewbie!false>(+${vcatnStats.newbieYryc!0})</#if>일<#if (vcatnStats.cnwkYryc!0) gt 0>, &#10;근속 추가연차: ${vcatnStats.cnwkYryc!0}일</#if><#if (vcatnStats.prjctYryc!0) gt 0>, &#10;프로젝트 추가연차: ${vcatnStats.prjctYryc!0}일</#if>">
                    <span class="cursor-help" >총연차 : ${vcatnStats.totYryc!0}</span>
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label">
                    소진연차 : ${vcatnStats.exhsYryc!0}
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label">
                    잔여연차 : ${vcatnStats.remndrYryc!0}
                </div>
                <div class="col-xl-2 col-sm-12">
                    <button type="button" class="btn btn-sm btn-primary mx-1"
                            onclick="dF.UserMy.myVcatnList();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="내 휴가 사용현황 조회&#10;팝업을 호출합니다.">
                        <i class="fas fa-umbrella-beach"></i>휴가 상세 현황 확인
                    </button>
                </div>
            </div>
        </div>
    </#if>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="userNo" value="${user.userNo!}">
    <#if user.userInfo??>
        <input type="hidden" name="userProflNo" value="${user.userInfo.userProflNo!}">
    </#if>
</form>
<div id="proflImageTemplate" hidden>
    <#--begin::Form-->
    <form name="profllImgForm" id="profllImgForm" class="form" enctype="multipart/form-data">
        <input type="file" name="atchFile0" id="atchFile0" class="file"
               onchange="return false;"
               accept=".png, .jpg, .jpeg" />
    </form>
</div>

<#--include::modals-->
<#include "/view/domain/user/my/_modal/_user_my_pw_chg_modal.ftlh">
<#if (authInfo.hasEcnyDt!false)>
    <#include "/view/domain/user/my/_modal/_user_my_vcatn_list_modal.ftlh">
</#if>
<script type="text/javascript" src="/js/view/domain/user/my/user_my_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/user/my/user_my_pw_chg_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/user/my/user_my_dtl.js?${releaseDate!}"></script>

</@layout.layout>