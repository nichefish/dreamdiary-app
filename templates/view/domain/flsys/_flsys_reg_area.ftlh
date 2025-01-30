<#--
 * 레이아웃 > 파일시스템 참조 추가 영역
 -->

<#--begin::Row-->
<div class="row">
    <div>
        <label class="mb-2 d-block">
            <span class="text-gray-700 fs-6 fw-bolder cursor-help"
                  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="파일 서버에 있는 파일에 대한&#10;참조 정보를 추가합니다.">
                파일시스템 참조
            </span>
            <span class="text-gray-500 fs-9 mx-2">※ 파일 서버에 있는 파일에 대한 참조 정보를 추가합니다.</span>
        </label>
        <div class="btn btn-sm btn-light-primary"
             onclick="Flsys.flsysModal('${HOME_FLSYS!}');"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="파일 시스템 모달 팝업을&#10;호출합니다.">
            <span class="svg-icon svg-icon-muted svg-icon-1hx">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path opacity="0.3" d="M7 20.5L2 17.6V11.8L7 8.90002L12 11.8V17.6L7 20.5ZM21 20.8V18.5L19 17.3L17 18.5V20.8L19 22L21 20.8Z" fill="currentColor"/>
                    <path d="M22 14.1V6L15 2L8 6V14.1L15 18.2L22 14.1Z" fill="currentColor"/>
                </svg>
            </span>
            파일시스템 참조
        </div>
    </div>
    <div id="flsys_ref_div" class="mb-8">
    </div>
</div>

<#--include:: Handlebars template-->
<#include "/view/global/_common/flsys/_flsys_reg_template.hbs">