/**
 * journalDayMetaService.ts
 * Vue 소유 저널 일자 메타 조율 서비스.
 */

import { getJournalDayListBridge } from "../journalDayListBridge.js";

type YearOption = {
    value: string | number;
    label: string | number;
    selected?: boolean;
};
type JournalDayMetaPageBridge = {
    mounted?: boolean;
    setMetaList?: (list: Record<string, any>[]) => void;
    setSelectedConfig?: (obj: Record<string, any> | null) => void;
    pendingList?: Record<string, any>[] | null;
    pendingConfig?: Record<string, any> | null;
};
let dayMetaCategoryMap: Record<string, any> = {};

function showAjaxFailure(res: AjaxResponse): void {
    if (cF.util.isNotEmpty(res?.message)) Swal.fire({ text: res.message });
}

function getSelectedYy(yy?: string | number): string {
    if (yy != null && cF.util.isNotEmpty(String(yy))) return String(yy);

    const currentSearchYy: string = getJournalDayListBridge()?.getSearchParams?.()?.yy;
    if (cF.util.isNotEmpty(currentSearchYy)) return currentSearchYy;

    console.warn(
        "[journalDayMetaService.getSelectedYy] Vue SSOT yy 없음; URL yy 폴백 제거됨. 현재 연도 문자열로 진행한다."
    );
    return cF.date.getCurrYyStr();
}

function normalizeSelectedYy(selectedYy: string, yyList: (string | number)[]): string {
    if (yyList.length === 0) return selectedYy;

    const matchedYy = yyList.find((yy: string | number): boolean => String(yy) === String(selectedYy));
    if (matchedYy != null) return String(matchedYy);

    return String(yyList[0]);
}

function getYearOptions(selectedYy: string, yyList: (string | number)[]): YearOption[] {
    return yyList.map((yy: string | number): YearOption => ({
        value: yy,
        label: yy,
        selected: String(yy) === String(selectedYy),
    }));
}

function openMetaModalByVueBridge(payload: Record<string, any>): boolean {
    const bridge = window.JournalDayMetaVueApp as {
        mounted?: boolean;
        open?: (payload: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    } | undefined;
    if (!bridge || typeof bridge !== "object") {
        console.error("[journalDayMetaService] JournalDayMetaVueApp bridge is not available.");
        return false;
    }
    if (bridge.mounted === true && typeof bridge.open === "function") {
        bridge.open(payload);
        return true;
    }
    bridge.pendingPayload = payload;
    return true;
}

function setSelectedConfigByVueBridge(cfg: Record<string, any>): boolean {
    const pageBridge = window.JournalDayMetaPageVueApp as JournalDayMetaPageBridge | undefined;
    if (!pageBridge || typeof pageBridge !== "object") return false;
    if (pageBridge.mounted === true && typeof pageBridge.setSelectedConfig === "function") {
        pageBridge.setSelectedConfig(cfg);
        return true;
    }
    pageBridge.pendingConfig = cfg;
    return true;
}

function setMetaListByVueBridge(rows: Record<string, any>[]): boolean {
    const pageBridge = window.JournalDayMetaPageVueApp as JournalDayMetaPageBridge | undefined;
    if (!pageBridge || typeof pageBridge !== "object") return false;
    if (pageBridge.mounted === true && typeof pageBridge.setMetaList === "function") {
        pageBridge.setMetaList(rows);
        return true;
    }
    pageBridge.pendingList = rows;
    return true;
}

function getCtgrMap(): Promise<Record<string, any>> {
    return new Promise((resolve): void => {
        cF.ajax.get(Url.JOURNAL_DAY_META_CTGR_MAP, {}, function(res: AjaxResponse): void {
            if (res.rsltMap) dayMetaCategoryMap = res.rsltMap as Record<string, any>;
            resolve(dayMetaCategoryMap);
        });
    });
}

function listMetaHeaders(): void {
    cF.ajax.get(Url.JOURNAL_DAY_METAS, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            showAjaxFailure(res);
            return;
        }
        const rows: Record<string, any>[] = Array.isArray(res.rsltList) ? res.rsltList : [];
        setMetaListByVueBridge(rows);
    });
}

function getYyList(metaId: string | number): Promise<any[]> {
    return new Promise((resolve): void => {
        const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_META_YYS, { id: metaId });
        cF.ajax.get(url, null, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve([]);
                return;
            }
            resolve(Array.isArray(res.rsltList) ? res.rsltList : []);
        });
    });
}

function getDaysByMeta(metaId: string | number, yy: string): Promise<Record<string, any>[]> {
    return new Promise((resolve): void => {
        const url: string = cF.util.bindUrl(Url.JOURNAL_DAYS);
        cF.ajax.get(url, { viewType: "SEARCH", metaId, yy }, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve([]);
                return;
            }
            resolve(Array.isArray(res.rsltList) ? res.rsltList : []);
        });
    });
}

async function openMetaModal(metaId: string | number, yy?: string | number): Promise<void> {
    if (isNaN(Number(metaId))) return;

    ModalHistory.reset();

    const args: any[] = Array.from(arguments);
    const preferredYy: string = getSelectedYy(yy);
    const yyList = await getYyList(metaId);
    const selectedYy: string = normalizeSelectedYy(preferredYy, yyList);
    const list = await getDaysByMeta(metaId, selectedYy);
    const openedByVue = openMetaModalByVueBridge({
        metaId,
        yy: selectedYy,
        yearOptions: getYearOptions(selectedYy, yyList),
        list,
    });
    if (openedByVue) ModalHistory.push(journalDayMetaService, "openMetaModal", args);
}

function changeMetaYear(metaId: string | number, yy: string | number): void {
    void openMetaModal(metaId, yy);
}

function selectMeta(metaId: string | number): void {
    const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_META, { id: metaId });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            showAjaxFailure(res);
            return;
        }
        setSelectedConfigByVueBridge((res.rsltObj as Record<string, any>) ?? {});
    });
}

function getDayMetaCategoryMap(): Record<string, any> {
    return dayMetaCategoryMap;
}

const journalDayMetaService = {
    changeMetaYear,
    getCtgrMap,
    getDayMetaCategoryMap,
    listMetaHeaders,
    openMetaModal,
    selectMeta,
};

dF.JournalDayMetaService = journalDayMetaService;

export default journalDayMetaService;
