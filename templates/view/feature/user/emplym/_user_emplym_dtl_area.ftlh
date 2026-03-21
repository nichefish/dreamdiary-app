<#--
 * 사용자 인사정보(user_emplym) > 조회 영역
 * @included_in : user_dtl.ftlh
 -->

<!--begin::Menu separator-->
<div class="separator my-2"></div>
<!--end::Menu separator-->

<#--begin::Card body-->
<div class="card-body">
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">직원명</div>
        </div>
        <div class="col-xl-2 col-8 col-form-label">
            ${(user.emplym.userNm)!"-"}
        </div>
    </div>
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">업무용 이메일</div>
        </div>
        <div class="col-xl-2 col-8 col-form-label">
            ${user.emplym.emplymEmail!}
        </div>
    </div>
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">업무용 연락처</div>
        </div>
        <div class="col-xl-2 col-8 col-form-label">
            ${user.emplym.emplymCttpc!}
        </div>
    </div>
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">소속</div>
        </div>
        <div class="col-xl-2 col-8 col-form-label">
            ${user.emplym.cmpyNm!} / ${user.emplym.teamNm!} / ${user.emplym.emplymNm!}
        </div>
    </div>
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">직급</div>
        </div>
        <div class="col-xl-2 col-8 col-form-label">
            ${user.emplym.cmpyNm!} / ${user.emplym.teamNm!} / ${user.emplym.emplymNm!}
        </div>
    </div>
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">입사일</div>
        </div>
        <div class="col-xl-2 col-8 col-form-label">
            ${user.emplym.ecnyDt!} / ${user.emplym.retireYn!} / ${user.emplym.retireDt!}
        </div>
    </div>
    <#--begin::Row-->
    <div class="row mb-4">
        <div class="col-xl-2 col-4">
            <div class="col-form-label text-center fw-bold">급여 계좌 정보</div>
        </div>
        <div class="col-xl-9 col-8 col-form-label">
            <div class="div-textarea h-auto min-h-125px">${user.emplym.acntBank!} / ${user.emplym.acntNo!}</div>
        </div>
    </div>
</div>