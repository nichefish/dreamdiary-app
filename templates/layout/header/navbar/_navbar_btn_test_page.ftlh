<!--
 * 레이아웃 > 탑바 > 테스트 페이지 버튼
 * @included_in : layout/topbar
 -->

<div class="app-navbar-item mx-1">
    <div class="d-flex align-items-center">
        <div class="btn btn-icon btn-active-light-primary blink position-relative w-30px h-30px w-md-40px h-md-40px"
             data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click"
             title="(개발자) 테스트&#10;화면으로 이동합니다."
             onclick="location.replace('${Url.ADMIN_TEST!}');">
            <#--begin::Svg Icon | path: icons/duotune/general/gen022.svg-->
            <span class="svg-primary svg-icon svg-icon-1">
                <#--begin::Svg Icon | path: /var/www/preview.keenthemes.com/keenthemes/metronic/docs/core/html/src/media/icons/duotune/abstract/abs026.svg-->
                <span class="svg-icon svg-icon-muted svg-icon-1hx">
                    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="24px" height="24px" viewBox="0 0 24 24" version="1.1">
                        <title>(개발자) 테스트&#10;화면으로 이동합니다.</title>
                        <g stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                            <rect x="0" y="0" width="24" height="24"/>
                            <path d="M5,8.6862915 L5,5 L8.6862915,5 L11.5857864,2.10050506 L14.4852814,5 L19,5 L19,9.51471863 L21.4852814,12 L19,14.4852814 L19,19 L14.4852814,19 L11.5857864,21.8994949 L8.6862915,19 L5,19 L5,15.3137085 L1.6862915,12 L5,8.6862915 Z M12,15 C13.6568542,15 15,13.6568542 15,12 C15,10.3431458 13.6568542,9 12,9 C10.3431458,9 9,10.3431458 9,12 C9,13.6568542 10.3431458,15 12,15 Z" fill="#000000"/>
                        </g>
                    </svg>
                </span>
                <#--end::Svg Icon-->
            </span>
            <#--end::Svg Icon-->
        </div>
    </div>
</div>