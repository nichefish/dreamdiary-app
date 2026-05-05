/**
 * journalInterpretationCrudService.ts
 * 저널 해석 CRUD/모달 액션 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(I-3):
 *   - journal_interpretation_module.ts 의 CRUD 계열 메서드(regModal/regAjax/dtlModal/mdfModal/delAjax/copy/submit)
 *     를 본 서비스로 추출한다. 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - initForm 은 Vue 브리지 전용으로 모듈에 남긴다(JournalInterpretationRegVueApp 경로).
 *   - dtlModal 의 Handlebars 템플릿 호출(`journal_interpretation_dtl`)은 미존재 템플릿 호출 그대로 유지(가시 dead — 별도 정리 전까지 변경 없음).
 *
 * 변경(I-5):
 *   - dtlModal 본체와 service 등록 항목 제거. 호출 그래프상 외부 호출자 없음(자체 thin wrapper 만 호출하던 dead 경로).
 *     · 이전 표기는 "별도 정리 전까지 변경 없음"이었으나 본 phase 에서 정식 제거한다(가시 dead 자체가 사라짐).
 *
 * @author nichefish
 */

/** @keepInSync static/vue/feature/journal/day/journalDayListBridge.ts */
function journalDayResolveListBridge(): JournalDayListAppBridge | undefined {
    return window.JournalDayMonthlyApp ?? window.JournalDayWeeklyApp ?? window.JournalDayDailyApp;
}

function getInterpretationNs(): Record<string, any> | undefined {
    return (window as any).dF?.JournalInterpretation as Record<string, any> | undefined;
}

function closeOpenModals(): void {
    document.querySelectorAll(".modal.show").forEach(function(modal: Element): void {
        $(modal).modal("hide");
    });
}

/**
 * 등록 모달 호출
 * 변경 전: journal_interpretation_module.regModal — 검증 후 initForm.
 * 변경 후(I-3): 동일 흐름. initForm 은 모듈 표면(`dF.JournalInterpretation.initForm`) 을 호출한다.
 */
export function regModal(params: {
    journalDayId: string | number;
    refId: string | number;
    refContentType: string;
    stdrdDt: string;
    journalDateWeekDay: string;
}): void {
    const { journalDayId, refId, refContentType, stdrdDt, journalDateWeekDay } = params;
    if (isNaN(Number(journalDayId))) return;
    if (isNaN(Number(refId))) return;
    if (cF.util.isEmpty(refContentType)) return;

    const obj: Record<string, any> = {
        journalDayId,
        refId,
        refContentType,
        stdrdDt,
        journalDateWeekDay,
    };
    getInterpretationNs()?.initForm?.(obj);
}

/**
 * 폼 제출 (tinymce save + form submit)
 * 변경 전: journal_interpretation_module.submit.
 */
export function submit(): void {
    tinymce.get("tinymce_journalInterpretationCn").save();
    $("#journalInterpretationRegForm").submit();
}

/**
 * 등록 (Ajax)
 * 변경 전: journal_interpretation_module.regAjax.
 */
export function regAjax(): void {
    const dfNs = (window as any).dF;
    const id: string = cF.util.getInputValue("#journalInterpretationRegForm [name='id']");
    const isMdf: boolean = cF.util.isNotEmpty(id);
    Swal.fire({
        text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id }) : Url.JOURNAL_INTERPRETATIONS;
        const ajaxData: FormData = new FormData(document.getElementById("journalInterpretationRegForm") as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    /* 변경(Phase 16): dF.JournalDayRuntimeService.refresh() 제거 → 뷰 감지 인라인.
                     * CAL 화면(JournalDayCalVueApp.mounted)에서도 호출될 수 있어 CAL 감지 포함. */
                    if (window.JournalDayCalVueApp?.mounted === true) {
                        window.JournalDayCalVueApp.refresh?.();
                        /* 변경 후: 해석 CRUD 후 태그 패널 갱신 — dF.JournalDayTagService 단일 진입점 호출. */
                        dfNs.JournalDayTagService?.refreshDayTagList?.();
                    } else {
                        journalDayResolveListBridge()?.refresh?.();
                    }
                    cF.ui.unblockUI();
                    ModalHistory.reset();
                });
        }, "block");
    });
}

/**
 * (변경 I-5) 상세 모달 호출 dtlModal 제거됨.
 *   - 변경 전: journal_interpretation_module.dtlModal — Ajax get 후 cF.handlebars.modal(`journal_interpretation_dtl`).
 *     해당 Handlebars 템플릿이 저장소에 부재하여 dead 호출이었고, I-1~I-4 단계에서 외부 호출자도 0 으로 확인됨.
 *   - 본 phase 에서 본체와 thin wrapper, 타입 선언을 모두 제거(가시 dead 정리).
 */

/**
 * 수정 모달 호출
 * 변경 전: journal_interpretation_module.mdfModal.
 */
export function mdfModal(id: string|number): void {
    if (isNaN(Number(id))) return;

    closeOpenModals();

    const self = getInterpretationNs();
    const funcName = "mdfModal";
    const histArgs: unknown[] = [id];

    const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const { rsltObj } = res;
        /* initialize form. */
        getInterpretationNs()?.initForm?.(rsltObj);

        /* modal history push */
        ModalHistory.push(self, funcName, histArgs);
    });
}

/**
 * 삭제 (Ajax)
 * 변경 전: journal_interpretation_module.delAjax.
 */
export function delAjax(id: string|number): void {
    if (isNaN(Number(id))) return;

    const dfNs = (window as any).dF;
    Swal.fire({
        text: Message.get("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
        cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    /* 변경(Phase 16): dF.JournalDayRuntimeService.refresh() 제거 → 뷰 감지 인라인. */
                    if (window.JournalDayCalVueApp?.mounted === true) {
                        window.JournalDayCalVueApp.refresh?.();
                        /* 변경 후: 해석 CRUD 후 태그 패널 갱신 — dF.JournalDayTagService 단일 진입점 호출. */
                        dfNs.JournalDayTagService?.refreshDayTagList?.();
                    } else {
                        journalDayResolveListBridge()?.refresh?.();
                    }
                    cF.ui.unblockUI();
                    ModalHistory.reset();
                });
        }, "block");
    });
}

/**
 * 복사
 * 변경 전: journal_interpretation_module.copy.
 *
 * @deprecated
 */
export function copy(id: string|number): void {
    if (isNaN(Number(id))) return;

    const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const rsltObj: Record<string, any> = res.rsltObj;
        const { stdrdDt, journalDateWeekDay } = rsltObj;
        const date: string = stdrdDt + " (" + journalDateWeekDay + ")" + "\r\n";
        const resultCn: string = rsltObj.content;
        // 문단/줄바꿈을 먼저 텍스트로 치환
        const replacedCn: string = resultCn
            .replace(/<\s*hr\b[^>]*\/?>/gi, "\n---\n")
            .replace(/<\s*br\s*\/?>/gi, "\n")
            .replace(/<\s*\/?p[^>]*>/gi, "\n");
        const div: HTMLDivElement = document.createElement("div");
        div.innerHTML = date + replacedCn;
        const textToCopy: string = (div.innerText ?? "")
            .replace(/\n+/g, "\n")
            .replace(/\n/g, "\r\n")
            .trim();

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(textToCopy)
                .then((): void => {
                    Swal.fire({ icon: "success", text: "클립보드에 복사되었습니다.", timer: 1500, showConfirmButton: false  });
                })
                .catch((): void => {
                    cF.util.legacyCopy(textToCopy);
                });
        } else {
            cF.util.legacyCopy(textToCopy);
        }
    });
}

/**
 * 글로벌 노출. classic `journal_interpretation_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalInterpretationCrudService.<method>(...)` 로 호출한다.
 */
const journalInterpretationCrudService = {
    regModal,
    submit,
    regAjax,
    mdfModal,
    delAjax,
    copy,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalInterpretationCrudService = journalInterpretationCrudService;
})();

export default journalInterpretationCrudService;
