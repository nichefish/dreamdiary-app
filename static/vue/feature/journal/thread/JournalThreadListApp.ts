/**
 * JournalThreadListApp.ts
 * 저널 스레드 목록 화면 Vue 엔트리.
 *
 * 변경: 헤더·모달 CustomEvent 브리지는 유지하되, 사용자 목록 `UserListApp` 과 같은 계약으로
 * 목록 줄(`tbody`)을 `<teleport to="#journal_thread_list_div">` 로 Vue 가 렌더한다.
 *
 * 변경 전: 서버 SSR 가 `<tbody>` 안에서 `<#list journalThreadList>` 로 행을 직접 생성.
 * 변경 후: `journal_thread_list_data` 에 직렬화된 동일 데이터를 포함하고, 목록 줄 DOM 과 클래스 구성은
 * `JournalThreadListTable` 에서 재현한다(`journal_thread_list.ftlh` 과 동등한 마크업).
 *
 * 페이징 UI 는 기존 `_pagination.ftlh` 를 그대로 둔다 — `Pagination.fnPage` 가 목록 페이지 전체 제출만 수행함.
 *
 * @author nichefish
 */
import JournalThreadListTable from "./components/JournalThreadListTable.js";
import journalThreadListDataService from "./services/journalThreadListDataService.js";
import createJournalThreadListActions from "./services/journalThreadListActionService.js";
import type { JournalThreadListLabels } from "./types.js";

function reinitJournalThreadDomDecorations(): void {
    Vue.nextTick(function(): void {
        const target = document.getElementById("journal_thread_list_div");
        if (!target)
            return;

        const bsTooltip = (window as any).bootstrap?.Tooltip;
        target.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
            if (!bsTooltip)
                return;
            const htmlEl = el as HTMLElement;
            const existing = bsTooltip.getInstance?.(htmlEl);
            if (existing)
                existing.dispose();
            new bsTooltip(htmlEl);
        });
        if (typeof KTMenu !== "undefined" && typeof (KTMenu as any).createInstances === "function") {
            (KTMenu as any).createInstances();
        }
    });
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function bindEvents(actions: ReturnType<typeof createJournalThreadListActions>): void {
    window.addEventListener("journal-thread:list-search", function(): void {
        actions.search();
    });
    window.addEventListener("journal-thread:list-mypapr", function(): void {
        actions.myPaprList();
    });
    window.addEventListener("journal-thread:open-regist-form", function(): void {
        actions.registForm();
    });
    window.addEventListener("journal-thread:list-xlsx-download", function(): void {
        actions.xlsxDownload();
    });
    window.addEventListener("journal-thread:open-detail-modal", function(evt: Event): void {
        const customEvt = evt as CustomEvent<{ id?: string | number }>;
        const id = customEvt.detail?.id;
        if (id === undefined || id === null)
            return;
        actions.detailModal(id);
    });
}

runWhenDomReady(function(): void {
    if (!document.getElementById("journal_thread_list_app") || !document.getElementById("journal_thread_list_div")) {
        console.error("[JournalThreadListApp] Vue mount roots not found.");
        return;
    }

    const state = Vue.reactive({
        rows: journalThreadListDataService.parseRowsFromPageData(),
        labels: journalThreadListDataService.parseLabels() as JournalThreadListLabels,
    });

    const actions = createJournalThreadListActions();
    bindEvents(actions);
    cF.table.initSort();

    const Root = {
        name: "JournalThreadListRoot",
        components: { JournalThreadListTable },
        data(): { state: typeof state } {
            return { state };
        },
        watch: {
            "state.rows": {
                handler(): void {
                    reinitJournalThreadDomDecorations();
                },
                deep: true,
            },
        },
        mounted(): void {
            reinitJournalThreadDomDecorations();
        },
        template: `
            <teleport to="#journal_thread_list_div">
                <JournalThreadListTable :rows="state.rows" :labels="state.labels" />
            </teleport>
        `,
    };

    Vue.createApp(Root).mount("#journal_thread_list_app");
});

export {};
