/**
 * journalAnnualReviewCrudService.ts
 * 저널 결산 리뷰 CRUD/모달 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-α):
 *   - journal_annual_review_module.ts 의 CRUD/Ajax 메서드(submit/regModal/mdfModal/regAjax/delAjax)
 *     를 본 service 로 추출한다. 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - initForm 은 Vue 브리지 진입(JournalAnnualReviewRegVueApp) 으로 모듈 표면에 그대로 남긴다(I-3 패턴 동일).
 *   - 인스턴스 필드 `tagify` 는 `JournalAnnualReviewRegModalApp.attachRegFormControls` 가 모듈 표면에
 *     `module.tagify = cF.tagify.init(...)` 로 직접 set 한다. 외부 read 사이트는 0 이지만 행위 보존.
 *
 * 변경(D):
 *   - `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — ESM 스코프 식별자 결의 race 차단.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

function getReviewNs(): Record<string, any> | undefined {
    return (window as any).dF?.JournalAnnualReview as Record<string, any> | undefined;
}

/**
 * 폼 제출
 * 변경 전: journal_annual_review_module.submit.
 */
export function submit(): void {
    tinymce.get("tinymce_journalAnnualReviewCn").save();
    $("#journalAnnualReviewRegForm").submit();
}

/**
 * 등록 모달 호출 — initForm 위임(Vue 브리지 진입).
 * 변경 전: journal_annual_review_module.regModal.
 */
export function regModal({ journalAnnualId }: { journalAnnualId: string|number }): void {
    if (isNaN(Number(journalAnnualId))) return;

    const obj: Record<string, any> = { journalAnnualId };
    /* initialize form. */
    getReviewNs()?.initForm?.(obj);
}

/**
 * 수정 모달 호출
 * 변경 전: journal_annual_review_module.mdfModal.
 */
export function mdfModal(id: string|number): void {
    if (isNaN(Number(id))) return;

    const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_REVIEW, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const { rsltObj } = res;
        /* initialize form. */
        getReviewNs()?.initForm?.(rsltObj);
    });
}

/**
 * 등록 (Ajax)
 * 변경 전: journal_annual_review_module.regAjax.
 */
export function regAjax(): void {
    const id: string = cF.util.getInputValue("#journalAnnualReviewRegForm [name='id']");
    const isMdf: boolean = cF.util.isNotEmpty(id);
    Swal.fire({
        text: resolveMessage(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_ANNUAL_REVIEW, { id }) : Url.JOURNAL_ANNUAL_REVIEWS;
        const ajaxData: FormData = new FormData(document.getElementById("journalAnnualReviewRegForm") as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    cF.ui.blockUIReload();
                });
        }, "block");
    });
}

/**
 * 삭제 (Ajax)
 * 변경 전: journal_annual_review_module.delAjax.
 */
export function delAjax(id: string|number): void {
    if (isNaN(Number(id))) return;

    Swal.fire({
        text: resolveMessage("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_REVIEW, { id });
        cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    cF.ui.blockUIReload();
                });
        }, "block");
    });
}

/**
 * 글로벌 노출. classic `journal_annual_review_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalAnnualReviewCrudService.<method>(...)` 로 호출한다.
 */
const journalAnnualReviewCrudService = {
    submit,
    regModal,
    mdfModal,
    regAjax,
    delAjax,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalAnnualReviewCrudService = journalAnnualReviewCrudService;
})();

export default journalAnnualReviewCrudService;
