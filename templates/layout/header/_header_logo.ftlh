<!--
 * 레이아웃 > 헤더 > 모바일
 * @included_in : layout/header/_header_base.ftlh
 -->

<!--begin::Web logo-->
<div class="d-flex align-items-center flex-grow-1 flex-lg-grow-0" style="width:235px;">


	<div class="d-none d-lg-flex align-items-center w-100 border-end h-50 border-gray-400">
		<a href="${Url.MAIN!}"
		   data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click" title="${Url.DOMAIN!}">
			<img alt="Logo" src="/media/img/logo/logo_racoon.jpg" class="h-30px"/>
			<span class="ps-3 fs-6 text-underline-dotted" style="letter-spacing: 0.2px">
				${Url.DOMAIN!}
			</span>
		</a>
	</div>
</div>
<!--end::Web logo-->
<!--begin::Mobile logo-->
<div class="d-flex d-lg-none align-items-center flex-grow-1 flex-lg-grow-0">
	<div class="btn btn-icon btn-active-color-primary w-35px h-35px" id="kt_app_sidebar_mobile_toggle">
		<i class="ki-duotone ki-abstract-14 fs-2 fs-md-1">
			<span class="path1"></span>
			<span class="path2"></span>
		</i>
	</div>
	<a href="${Url.MAIN!}" class="d-lg-none">
		<img alt="Logo" src="/media/img/logo/logo_racoon.jpg" class="h-30px"/>
	</a>
</div>
<!--end::Mobile logo-->