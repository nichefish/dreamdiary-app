/**
 * 저널 스레드 목록 페이지에 포함된 부트스트랩(JSON script) 파서.
 *
 * 변경: 사용자 계정 목록의 `userDataService` 와 같은 계약 — SSR 가 이미 채운 `journalThreadList` 를
 * 템플릿에서 직렬화하여 Vue 초기 상태로만 사용한다(`UserListApp` 과 동등).
 *
 * @author nichefish
 */
import type { JournalThreadListLabels, JournalThreadListRow } from "../types.js";

function parseSafeArray(payload: unknown): JournalThreadListRow[] {
    if (!Array.isArray(payload)) return [];
    return payload as JournalThreadListRow[];
}

export default {
    parseRowsFromPageData(): JournalThreadListRow[] {
        const dataEl: HTMLElement | null = document.getElementById("journal_thread_list_data");
        if (!dataEl)
            return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return parseSafeArray(parsed);
        } catch (e) {
            console.error("[JournalThreadListApp] journal_thread_list_data parse failed", e);
            return [];
        }
    },

    parseLabels(): JournalThreadListLabels {
        const fallback: JournalThreadListLabels = {
            pageDetail: "",
            comment: "",
            atchFile: "",
            tagContentList: "",
            modalView: "",
            emptyList: "",
        };
        const dataEl: HTMLElement | null = document.getElementById("journal_thread_label_data");
        if (!dataEl)
            return fallback;
        try {
            return { ...fallback, ...JSON.parse(dataEl.textContent || "{}") };
        } catch (e) {
            console.error("[JournalThreadListApp] journal_thread_label_data parse failed", e);
            return fallback;
        }
    },
};
