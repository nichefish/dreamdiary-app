<!--
 * 레이아웃 > 탑바 > 파일시스템 메뉴 버튼
 * @included_in : layout/topbar
 -->

<div class="app-navbar-item align-items-stretch mx-1">
    <div class="d-flex align-items-center">
        <div class="btn btn-icon btn-active-light-primary blink position-relative w-30px h-30px w-md-40px h-md-40px"
             data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click"
             title="파일시스템 연동&#10;화면으로 이동합니다."
             onclick="location.replace('${Url.FLSYS_HOME!}');">
            <#--begin::Svg Icon | path: icons/duotune/general/gen022.svg-->
            <span class="svg-primary svg-icon svg-icon-1">
                <#--begin::Svg Icon | path: /var/www/preview.keenthemes.com/keenthemes/metronic/docs/core/html/src/media/icons/duotune/abstract/abs026.svg-->
                <span class="svg-icon svg-icon-muted svg-icon-1hx">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <title>파일시스템 연동&#10;화면으로 이동합니다.</title>
                        <path opacity="0.8" d="M7 20.5L2 17.6V11.8L7 8.90002L12 11.8V17.6L7 20.5ZM21 20.8V18.5L19 17.3L17 18.5V20.8L19 22L21 20.8Z" fill="currentColor"/>
                        <path d="M22 14.1V6L15 2L8 6V14.1L15 18.2L22 14.1Z" fill="currentColor"/>
                    </svg>
                </span>
                <#--end::Svg Icon-->
            </span>
            <#--end::Svg Icon-->
        </div>
    </div>
</div>
