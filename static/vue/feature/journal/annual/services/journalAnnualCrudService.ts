/**
 * journalAnnualCrudService.ts
 * 저널 결산 CRUD/모달/Ajax 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-α):
 *   - journal_annual_module.ts 의 CRUD/Ajax 계열 메서드(listAjax/detailView/detailViewWithSection/detailAjax/
 *     list/makeYyAnnualAjax/makeTotalAnnualAjax/comptAjax/submit/modifyModal/registAjax) 를 본 service 로 추출한다.
 *   - 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - initForm 은 Vue 브리지 진입(JournalAnnualRegistVueApp) 으로 모듈 표면에 그대로 남긴다(I-3 패턴 동일).
 *   - 외부 호출 시그니처는 변경 없음. 내부 동작도 변경 없음.
 *
 * 변경(D):
 *   - `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — ESM 스코프 식별자 결의 race 차단.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

function getAnnualNs(): Record<string, any> | undefined {
    return (window as any).dF?.JournalAnnual as Record<string, any> | undefined;
}

/**
 * 목록 갱신 (Ajax)
 * 변경 전: journal_annual_module.listAjax — `cF.handlebars.template(rsltList, "journal_annual_list")` 로
 *   `<div id="journal_annual_list_div">` 에 직접 HBS 카드 마크업 주입 후 `KTMenu.createInstances()` 호출.
 * 변경 후(A-5-α): `cF.handlebars.template` 진입을 제거하고 `window.JournalAnnualListVueApp.setList(...)` 브리지로
 *   Vue 가 카드를 렌더한다(KTMenu/tooltip 재부착은 Vue 측 nextTick 에서 동일 수행). `cF.ui.closeModal()` 동작은 보존.
 */
export function listAjax(): void {
    const url: string = Url.JOURNAL_ANNUALS;
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const { rsltList } = res;
        cF.ui.closeModal();

        const bridge = (window as any).JournalAnnualListVueApp as {
            mounted?: boolean;
            setList?: (list: Record<string, any>[]) => void;
            pendingList?: Record<string, any>[] | null;
        } | undefined;
        const list: Record<string, any>[] = Array.isArray(rsltList) ? rsltList : [];
        if (bridge?.mounted === true && typeof bridge.setList === "function") {
            bridge.setList(list);
            return;
        }
        if (bridge) {
            bridge.pendingList = list;
            console.log("[journalAnnualCrudService.listAjax] JournalAnnualListVueApp pending list queued.");
            return;
        }
        console.error("[journalAnnualCrudService.listAjax] JournalAnnualListVueApp unavailable (목록 페이지 외 호출 또는 로드 순서 확인).");
    }, "block");
}

/**
 * 상세 화면으로 이동 (년도로 조회)
 * 변경 전: journal_annual_module.detailView.
 */
export function detailView(yy: string|number): void {
    if (isNaN(Number(yy))) return;

    location.href = cF.util.bindUrl(Url.JOURNAL_ANNUAL_VIEW, {yy}) + "?section=DIARY";
}

/**
 * 섹션 전환 이동 (년도로 조회)
 * 변경 전: journal_annual_module.detailViewWithSection.
 */
export function detailViewWithSection(section: "DIARY"|"DREAM"): void {
    const yy: string = cF.util.getPathVariableFromUrl(/\/annual\/(\d{4})(?:\.do)?$/);
    if (!yy) return console.warn("invalid yy.");

    location.href = cF.util.bindUrl(Url.JOURNAL_ANNUAL_VIEW, {yy}) + `?section=${section}`;
}

/**
 * 상세 조회 (Ajax) (년도로 조회)
 * 변경 전: journal_annual_module.detailAjax.
 */
export function detailAjax(yy: string|number): void {
    const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL, { yy });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const rsltObj: Record<string, any> = res.rsltObj;
        /* 변경(A-7-β): 변경 전 `cF.handlebars.template(rsltObj, "journal_annual_detail")` 가 `#journal_annual_detail_div` 에 HBS 카드 주입.
         * 변경 후: Vue `JournalAnnualDetailCardApp` 브리지 `setModel` 로 동일 페이로드를 반영한다(UI 마크업은 Vue 템플릿이 1:1 재현). */
        const bridge = (window as any).JournalAnnualDetailVueApp as {
            mounted?: boolean;
            setModel?: (obj: Record<string, any>) => void;
            pendingModel?: Record<string, any> | null;
        } | undefined;
        if (bridge?.mounted === true && typeof bridge.setModel === "function") {
            bridge.setModel(rsltObj);
            return;
        }
        if (bridge) {
            bridge.pendingModel = rsltObj;
            console.log("[journalAnnualCrudService.detailAjax] JournalAnnualDetailVueApp pending model queued.");
            return;
        }
        console.error("[journalAnnualCrudService.detailAjax] JournalAnnualDetailVueApp unavailable (#journal_annual_detail_div 또는 적재 순서 확인).");
    });
}

/**
 * 목록 화면으로 이동
 * 변경 전: journal_annual_module.list.
 */
export function list(): void {
    cF.ui.blockUIReplace(Url.JOURNAL_ANNUAL_LIST);
}

/**
 * 특정 년도 결산 생성 (Ajax)
 * 변경 전: journal_annual_module.makeYyAnnualAjax.
 */
export function makeYyAnnualAjax(yy: string|number): void {
    const yYElmt: HTMLSelectElement = document.querySelector("#listForm #yy");
    if (yy == null) yy = yYElmt.value;
    if (cF.util.isEmpty(yy)) {
        cF.ui.swalOrAlert("yy는 필수 항목입니다.");
        return;
    }
    const url: string = Url.JOURNAL_ANNUAL_MAKE_AJAX;
    const ajaxData: Record<string, any> = { "yy": yy };
    cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
        Swal.fire({ text: res.message })
            .then(function(): void {
                if (res.rslt) cF.ui.blockUIReload();
            });
    }, "block");
}

/**
 * 전체 년도 결산 갱신 (Ajax)
 * 변경 전: journal_annual_module.makeTotalAnnualAjax.
 */
export function makeTotalAnnualAjax(): void {
    const url: string = Url.JOURNAL_ANNUAL_MAKE_TOTAL_AJAX;
    cF.$ajax.post(url, null, function(res: AjaxResponse): void {
        Swal.fire({ text: res.message })
            .then(function(): void {
                if (res.rslt) cF.ui.blockUIReload();
            });
    }, "block");
}

/**
 * 꿈 기록 완료 처리 (Ajax)
 * 변경 전: journal_annual_module.comptAjax.
 */
export function comptAjax(id: string|number): void {
    if (isNaN(Number(id))) return;

    const url: string = Url.JOURNAL_ANNUAL_DREAM_COMPT_AJAX;
    const ajaxData: Record<string, any> = { id };
    cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
        Swal.fire({ text: res.message })
            .then(function(): void {
                if (res.rslt) cF.ui.blockUIReload();
            });
    }, "block");
}

/**
 * 폼 제출
 * 변경 전: journal_annual_module.submit.
 */
export function submit(): void {
    tinymce.get("tinymce_journalAnnualCn").save();
    $("#journalAnnualRegistForm").submit();
}

/**
 * 등록(수정) 모달 호출
 * 변경 전: journal_annual_module.modifyModal.
 *   - initForm 은 모듈 표면(`dF.JournalAnnual.initForm`) 을 호출한다(Vue 브리지 진입).
 */
export function modifyModal(yy: string|number): void {
    if (isNaN(Number(yy))) return;

    const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL, { yy });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const { rsltObj } = res;
        /* initialize form. */
        getAnnualNs()?.initForm?.(rsltObj);
    });
}

/**
 * 등록 (Ajax)
 * 변경 전: journal_annual_module.registAjax.
 */
export function registAjax(): void {
    Swal.fire({
        text: resolveMessage("view.cnfm.save"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const yy: string = cF.util.getInputValue("#journalAnnualRegistForm [name='yy']");
        if (cF.util.isEmpty(yy)) return;

        const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL, { yy });
        const ajaxData: FormData = new FormData(document.getElementById("journalAnnualRegistForm") as HTMLFormElement);
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
 * 글로벌 노출. classic `journal_annual_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalAnnualCrudService.<method>(...)` 로 호출한다.
 */
const journalAnnualCrudService = {
    listAjax,
    detailView,
    detailViewWithSection,
    detailAjax,
    list,
    makeYyAnnualAjax,
    makeTotalAnnualAjax,
    comptAjax,
    submit,
    modifyModal,
    registAjax,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalAnnualCrudService = journalAnnualCrudService;
})();

export default journalAnnualCrudService;
