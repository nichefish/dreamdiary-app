<#--
 * 게시물 요소 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<#--
 * 게시물 상세 :: 타이틀
 -->
<#macro post_header_title post>
    <div class="d-flex align-items-center mb-9">
        <#--begin::title-->
        <div class="col-12">
            <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                <#if (((post.imprtcYn)!"N") == "Y")!false>
                    <span class="badge badge-danger me-2">중요</span>
                </#if>
                <span class="vertical-middle<#if (((post.imprtcYn)!"N") == "Y")!false> fw-bold</#if>">
                    <#if (post.ctgrNm!"") != "">[${post.ctgrNm!}]</#if>
                    ${post.title!}
                 </span>
            </h1>
        </div>
    </div>
</#macro>

<#--
 * 게시물 상세 :: 타이틀 메타
 -->
<#macro post_header_meta post>
    <div class="d-flex flex-stack flex-wrap mb-3">
        <!--begin::Author-->
        <div class="d-flex align-items-center">
            <!--begin::Name-->
            <div class="d-flex flex-column align-items-start justify-content-center">
                <span class="text-gray-800 fs-6 fw-bold lh-1 mb-2">
                    <#if post.managt??>
                        <#if (post.managt.managtrInfo.proflImgUrl)??>
                            <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                <img src="${(post.managt.managtrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                            </div>
                        </#if>
                        ${post.managt.managtrNm!"-"}
                    <#elseif post.mdfusrInfo??>
                        <#if (post.mdfusrInfo.proflImgUrl)??>
                            <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                <img src="${(post.mdfusrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                            </div>
                        </#if>
                        ${post.mdfusrNm!}
                    <#else>
                        <#if (post.regstrInfo.proflImgUrl)??>
                            <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                <img src="${(post.regstrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                            </div>
                        </#if>
                        ${post.regstrNm!}
                    </#if>
                </span>
                <span class="text-muted fs-7 lh-1">
                    <#if post.managtDt??>
                        ${post.managtDt!}
                    <#elseif post.mdfDt??>
                        ${post.mdfDt!}
                    <#else>
                        ${post.regDt!}
                    </#if>
                </span>
            </div>
        </div>
        <!--begin::Info-->
        <div class="d-flex align-items-center py-1">
            <a href="#answers" data-kt-scroll-toggle="true"
               class="btn btn-sm btn-outline btn-outline-dashed btn-outline-default px-4 me-2"><@spring.message 'comment'/> ${(post.comment.cnt)!0}</a>
            <button class="btn btn-sm btn-light px-4 me-2 disabled"><@spring.message 'hit-cnt'/> ${post.hitCnt!}</button>
        </div>
    </div>
</#macro>

<#--
 * 게시물 상세 :: 타이틀 메타 (미리보기 버전)
 -->
<#macro post_header_meta_preview post>
    <!--begin::Foot-->
    <div class="d-flex flex-stack flex-wrap mb-3">
        <!--begin::Author-->
        <div class="d-flex align-items-center">
            <!--begin::Name-->
            <div class="d-flex flex-column align-items-start justify-content-center">
                <span class="text-gray-800 fs-6 fw-bold lh-1 mb-2">
                    <#if (authInfo.proflImgUrl)??>
                        <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                            <img src="${(authInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                        </div>
                    </#if>
                    ${authInfo.nickNm!"-"}
                </span>
                <span class="text-muted fs-7 lh-1">
                    ${DateUtils.getCurrDtStr()!}
                </span>
            </div>
        </div>
        <!--begin::Info-->
        <div class="d-flex align-items-center py-1">
            <a href="#answers" data-kt-scroll-toggle="true"
               class="btn btn-sm btn-outline btn-outline-dashed btn-outline-default px-4 me-2"><@spring.message 'comment'/> ${(post.comment.cnt)!0}</a>
            <button class="btn btn-sm btn-light px-4 me-2 disabled"><@spring.message 'hit-cnt'/> ${(post.hitCnt)!0}</button>
        </div>
    </div>
</#macro>

<#--
 * 게시물 상세 :: 타이틀 메타 (프린트 버전)
 -->
<#macro post_header_meta_with_print post>
    <!--begin::Foot-->
    <div class="d-flex flex-stack flex-wrap mb-3">
        <!--begin::Author-->
        <div class="d-flex align-items-center">
            <!--begin::Name-->
            <div class="d-flex flex-column align-items-start justify-content-center">
                    <span class="text-gray-800 fs-6 fw-bold lh-1 mb-2">
                        <#if (post.regstrInfo.proflImgUrl)??>
                            <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                <img src="${(post.regstrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                            </div>
                        </#if>
                        ${post.regstrNm!"-"}
                    </span>
                <span class="text-muted fs-7 lh-1">
                        ${post.regDt!}
                    </span>
            </div>
        </div>
        <!--begin::Info-->
        <div class="d-flex align-items-center py-1">
            <button type="button" class="btn btn-sm btn-primary px-4 me-2"
                    onclick="Page.pdfPop();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="출력용 팝업을 호출합니다." >
                <i class="bi bi-printer"></i>출력
            </button>
            <a href="#answers" data-kt-scroll-toggle="true"
               class="btn btn-sm btn-outline btn-outline-dashed btn-outline-default px-4 me-2">
                <@spring.message 'comment'/> ${(post.comment.cnt)!0}
            </a>
            <button class="btn btn-sm btn-light px-4 me-2 disabled">
                <@spring.message 'hit-cnt'/> ${post.hitCnt!}
            </button>
        </div>
    </div>
</#macro>

