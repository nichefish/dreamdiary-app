<#--
 * 목록 테이블 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<!--
 * 목록 수정 권한 표시
 -->
<#macro mdfable post >
    <#if ((post.mdfable!"") == (Constant.MDFABLE_MNGR)) && isMngr>
        <i class="bi bi-person-lines-fill text-info ms-1 opacity-75"
           data-bs-toggle="tooltip" data-bs-placement="top" title="관리자 수정 가능"></i>
    <#elseif ((post.mdfable!"") == (Constant.MDFABLE_USER)) || ((post.mdfable!"") == (Constant.MDFABLE_ALL))>
        <i class="bi bi-people-fill ms-1 blink-slow"
           data-bs-toggle="tooltip" data-bs-placement="top" title="모든 사용자가 수정 가능"></i>
    </#if>
</#macro>

<!--
 * 목록 댓글 표시
 -->
<#macro list_comment post >
    <#if post.comment?? && post.comment.cnt gt 0>
        <span class="mx-1 text-noti btn-active-warning fs-x-small cursor-pointer opacity-hover"
              onclick="CommentList.modal(${post.postNo!}, '${post.contentType}');"
              data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message "comment"/> 모달 호출">
        [${post.comment.cnt!0}]
        </span>
    </#if>
</#macro>

<!--
 * 목록 모달 팝업 열기 버튼 표시
 -->
<#macro list_dtl_modal post func>
    <#assign fullFunc = (func + "(" + post.postNo?c + ")") />
    <a class="badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow float-end"
       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="모달로 보기">
        <i class="bi bi-stickies fs-5 text-noti opacity-hover" onclick="${fullFunc!}"></i>
    </a>
</#macro>

<!--
 * 목록 등록자(조치자) 표시
 -->
<#macro list_managtr post >
    <#if (post.managtrInfo)?? && (post.managtrInfo.proflImgUrl)??>
        <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
            <img src="${(post.managtrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
        </div>
    </#if>
    <span class="cursor-help"
          data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="최초 작성자:&#10;${post.regstrNm!"-"}">
        ${post.managt.managtrNm!"-"}
        <#if post.managt.isManagtr!false>
            <span class="badge badge-secondary opacity-75 mx-1 fs-9">나</span>
        </#if>
    </span>
</#macro>

<!--
* 목록 등록일시(조치일시) 표시
-->
<#macro list_managtDt post >
    <span class="cursor-help"
          data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="최초 작성일:&#10;${post.regDt!"-"}">
        ${post.managt.managtDt!}
    </span>
</#macro>

<!--
 * 목록 첨부파일 영역 표시
 -->
<#macro list_atch_file post >
    <#if post.atchFileInfo?? && (post.atchFileInfo.atchFileList)?has_content>
        <a class="badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow"
           data-bs-toggle="tooltip" data-bs-placement="top" title="첨부파일">
            <i class="bi bi-file-earmark-arrow-down fs-5 text-info opacity-hover" onclick="AtchFileList.modal('${post.atchFileNo!}');"></i>
        </a>
    <#else>
        -
    </#if>
</#macro>

<!--
 * 목록 태그 영역 표시
 -->
<#macro list_tag post >
    <#if post.tag?? && post.tag.tagStrList??>
        <span class="me-6 fs-7">
            <span class="pe-1 text-muted"><i class="bi bi-tags"></i></span>
            <#list post.tag.list as tag>
                <span class="text-muted pe-1 cursor-pointer"
                      onclick="dF.Tag.dtlModal('${tag.refTagNo!}')"
                      data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                      title="해당 태그가 등록된 게시물 목록 팝업을 호출합니다.">
                    <#if tag.ctgr != ""><span class="fs-7 text-noti">[${tag.ctgr!}]</span></#if>
                    #<span class="border-bottom text-primary fw-lighter opacity-hover">${tag.tagNm!"-"}</span>
                </span>
            </#list>
        </span>
    </#if>
</#macro>