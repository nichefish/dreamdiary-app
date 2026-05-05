/**
 * journalTodoCrudService.ts
 * 저널 할일 CRUD/표시 액션 서비스 (Vue 소유).
 *
 * 변경(T-2-β):
 *   - 레거시 dF.JournalTodo 의 yyMnthListAjax / delAjax 메서드를 이 서비스로 이전한다.
 *   - dF.JournalTodo.init() 의 외부 호출자 3곳(JournalDayMetaPageApp / JournalDayCalApp /
 *     journalDayListAppMount)은 이 서비스의 yyMnthListAjax 호출로 직접 교체된다(기존 init 의
 *     `initialized` 플래그는 페이지 진입 시점 1회 호출 보장으로 자연 소멸).
 *   - aside 적재 페이로드는 기존과 동일하게 window.JournalDayAsideTodoVueApp.applyTodoListPayload
 *     또는 pendingTodoListPayload 큐로 위임한다(T-1 에서 fallback HBS 경로는 이미 제거됨).
 *   - dF.JournalTodo 모듈은 외부 호출자가 0이 된 본 단계에서 통째로 제거된다(.ts/.js +
 *     6개 ftlh 의 module.js 적재 라인).
 *
 * @author nichefish
 */

type AsideTodoBridge = {
    mounted?: boolean;
    pendingTodoListPayload?: unknown;
    applyTodoListPayload?: (payload: unknown) => void;
};

function getAsideTodoBridge(): AsideTodoBridge | undefined {
    return (window as Window & { JournalDayAsideTodoVueApp?: AsideTodoBridge }).JournalDayAsideTodoVueApp;
}

/**
 * Aside TODO 카드(Vue) 로 목록 페이로드를 전달한다.
 *
 * 변경 전: dF.JournalTodo 의 module-private applyJournalTodoAsideListPayload — bridge.mounted 분기 + pending 큐.
 * 변경 후(T-2-β): 동일 분기 흐름을 service 로 이전한다.
 */
function applyAsideTodoPayload(rsltList: unknown): void {
    const bridge = getAsideTodoBridge();
    if (bridge?.mounted === true && typeof bridge.applyTodoListPayload === "function") {
        bridge.applyTodoListPayload(rsltList);
        return;
    }
    if (bridge?.mounted === false) {
        bridge.pendingTodoListPayload = rsltList;
        console.log("[journalTodoCrudService.yyMnthListAjax] Aside TODO Vue 대기 페이로드 저장(pendingTodoListPayload).");
        return;
    }
    /* 변경(T-1): aside 미포함 페이지의 todo 적재는 dead. fallback HBS 경로 제거 후 가시 로그만 남긴다. */
    console.error("[journalTodoCrudService.yyMnthListAjax] JournalDayAsideTodoVueApp bridge unavailable; aside not mounted on this page.");
}

/**
 * 목록 조회.
 * 변경 전: dF.JournalTodo.yyMnthListAjax — `#journal_aside #yy/mnth` 셀렉트 값으로 목록 ajax → 페이로드 적용.
 * 변경 후(T-2-β): 동일 동작을 service 로 이전. yy/mnth 미존재 시(예: aside 없는 페이지)는 단순 no-op.
 */
export function yyMnthListAjax(): void {
    const yyElmt: HTMLSelectElement | null = document.querySelector("#journal_aside #yy");
    const yy: string = yyElmt?.value ?? "";
    if (cF.util.isEmpty(yy)) return;

    const mnthElmt: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth");
    const mnth: string = mnthElmt?.value ?? "";
    if (cF.util.isEmpty(mnth)) return;

    const url: string = Url.JOURNAL_TODOS;
    const ajaxData: Record<string, any> = { yy, mnth };
    cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const { rsltList } = res;
        applyAsideTodoPayload(rsltList);
    }, "block");
}

/**
 * 삭제 (Ajax).
 * 변경 전: dF.JournalTodo.delAjax — POST + 성공 시 yyMnthListAjax + ModalHistory.reset.
 * 변경 후(T-2-β): 동일 분기 흐름을 service 로 이전한다(HTTP 메서드는 기존 POST 보존).
 */
export function delAjax(id: string | number): void {
    if (isNaN(Number(id))) return;

    Swal.fire({
        text: Message.get("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.JOURNAL_TODO, { id });
        cF.$ajax.post(url, null, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    yyMnthListAjax();
                    /* 모달 이력 되돌리기 */
                    ModalHistory.reset();
                });
        }, "block");
    });
}

const journalTodoCrudService = {
    yyMnthListAjax,
    delAjax,
};

export default journalTodoCrudService;