/**
 * journalDayCrudService.ts
 * 저널 일자 CRUD/모달 액션 서비스 (Vue 소유).
 *
 * 변경 전: JournalDayContextMenu 등 Vue 컴포넌트가 dF.JournalDayCrudService(레거시)를 직접 호출했다.
 * 변경 후: Vue 컴포넌트는 본 서비스를 통해 CRUD 액션을 수행한다.
 *          dF.JournalDayCrudService 는 레거시 data-journal-day-action DOM 및 regAjax 호환을 위해 유지된다(Phase 3 제거 예정).
 */

import { getJournalDayListBridge } from "../journalDayListBridge.js";

type RegVueBridge = {
    mounted?: boolean;
    open?: (model: Record<string, any>) => void;
    pendingPayload?: Record<string, any> | null;
};

type DetailVueBridge = {
    mounted?: boolean;
    open?: (model: Record<string, any>) => void;
    pendingPayload?: Record<string, any> | null;
};

type ListVueBridge = {
    mounted?: boolean;
    refresh?: () => void;
};

function hideOpenModals(): void {
    document.querySelectorAll(".modal.show").forEach(function(modal: Element): void {
        ($ as any)(modal).modal("hide");
    });
}

function openRegBridge(obj: Record<string, any>): void {
    const bridge = window.JournalDayRegVueApp as RegVueBridge | undefined;
    if (!bridge || typeof bridge !== "object") {
        console.error("[journalDayCrudService] JournalDayRegVueApp bridge is not available.");
        return;
    }
    if (bridge.mounted === true && typeof bridge.open === "function") {
        bridge.open(obj);
    } else {
        bridge.pendingPayload = obj;
    }
}

function resolveValidDate(): string {
    const calDt: Date | null | undefined = window.JournalDayCalVueApp?.getCalendarDate?.();
    if (calDt instanceof Date) return cF.date.dateToStr(calDt, cF.date.ptnDate) ?? cF.date.getCurrDateStr(cF.date.ptnDate);

    const searchParams: Record<string, any> = getJournalDayListBridge()?.getSearchParams?.() ?? {};
    const yy: string = cF.util.getUrlParam("yy") ?? searchParams.yy ?? cF.date.getCurrYyStr();
    const mnth: string = cF.util.getUrlParam("mnth") ?? searchParams.mnth ?? cF.date.getCurrMnthStr();

    const year: number = parseInt(yy, 10);
    let month: number = parseInt(mnth, 10);
    if (month === 99) month = 1;
    let day: number = parseInt(cF.date.getCurrDayStr(2), 10);
    const lastDay: number = new Date(year, month, 0).getDate();
    if (day > lastDay) day = lastDay;

    return `${String(year)}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

export function openRegModal(): void {
    openRegBridge({
        journalDate: resolveValidDate(),
        journalDatePrecision: "EXACT",
    });
    ModalHistory.push(journalDayCrudService, "openRegModal", []);
}

/**
 * 수정 모달 호출.
 * 변경 전: dF.JournalDayCrudService.mdfModal() → dF.JournalDayFormService.initForm() → bridge
 * 변경 후: Ajax 후 JournalDayRegVueApp bridge를 직접 호출한다.
 * @param {string|number} id
 */
export function openMdfModal(id: string | number): void {
    if (isNaN(Number(id))) return;
    hideOpenModals();

    const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        openRegBridge(res.rsltObj);
        ModalHistory.push(journalDayCrudService, "openMdfModal", [id]);
    });
}

/**
 * 상세 모달 호출.
 * 변경 전: dF.JournalDayCrudService.dtlModal() → bridge
 * 변경 후: Ajax 후 JournalDayDetailVueApp bridge를 직접 호출한다.
 * @param {string|number} id
 */
export function openDtlModal(id: string | number): void {
    if (isNaN(Number(id))) return;
    hideOpenModals();

    const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const rsltObj: Record<string, any> = res.rsltObj;
        const bridge = window.JournalDayDetailVueApp as DetailVueBridge | undefined;
        if (!bridge || typeof bridge !== "object") {
            console.error("[journalDayCrudService] JournalDayDetailVueApp bridge is not available.");
            return;
        }
        if (bridge.mounted === true && typeof bridge.open === "function") {
            bridge.open(rsltObj);
        } else {
            bridge.pendingPayload = rsltObj;
        }
        ModalHistory.push(journalDayCrudService, "openDtlModal", [id]);
    });
}

/**
 * 삭제 (Ajax).
 * 변경 전: dF.JournalDayCrudService.delAjax() → dF.JournalDayRuntimeService.refresh() → bridge
 * 변경 후: 삭제 후 <code>getJournalDayListBridge()?.refresh()</code>를 직접 호출한다.
 * @param {string|number} id
 */
export function delAjax(id: string | number): void {
    if (isNaN(Number(id))) return;

    Swal.fire({
        text: Message.get("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id });
        cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;
                    const vueApp = getJournalDayListBridge() as ListVueBridge | undefined;
                    if (vueApp?.mounted === true && typeof vueApp.refresh === "function") {
                        vueApp.refresh();
                        return;
                    }
                    window.JournalDayCalVueApp?.refresh?.();
                });
        }, "block");
    });
}

const journalDayCrudService = {
    openRegModal,
    openMdfModal,
    openDtlModal,
    delAjax,
};

export default journalDayCrudService;
