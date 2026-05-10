/**
 * journalDayRuntimeShell.ts
 * 저널 일자 런타임 액션 서비스 — 전역 <code>dF.JournalDayRuntimeService</code> 구현.
 *
 * 변경(P6): 본문을 <code>static/js/view/feature/journal/day/journal_day_runtime_service.ts</code> 에서 이 파일로 이전하여
 * Vue 축(<code>static/vue/feature/journal/day</code>) 소유로 수렴했다. 로드는 <code>registerJournalDayShellServices</code> 의 side-effect import 만 사용한다.
 *
 * 변경(P4): classic 전역 스크립트 전제의 <code>var dF</code> 제거.
 * ES 모듈(<code>registerJournalDayShellServices</code> 등)에서 side-effect import 될 때도
 * <code>globalThis.dF</code> 에 동일 객체를 붙인다.
 */
const __journalDayGlobal: any = typeof globalThis !== "undefined" ? globalThis : (window as any);
if (__journalDayGlobal.dF == null) {
    __journalDayGlobal.dF = {};
}
/** 전역 <code>dF</code> 객체. 식별자명을 <code>dF</code>로 두면 TS의 <code>declare namespace dF</code>와 충돌한다. */
const dfNs: any = __journalDayGlobal.dF;
dfNs.JournalDayRuntimeService = (function(): Module {
    /** @keepInSync static/vue/feature/journal/day/journalDayListBridge.ts */
    const journalDayResolveListBridge = (): JournalDayListAppBridge | undefined =>
        window.JournalDayMonthlyApp ?? window.JournalDayWeeklyApp ?? window.JournalDayDailyApp;

    let legacyJournalDayActionBound: boolean = false;
    const syncPinnedYyMnthLabel = (): void => {
        const pinnedYy: string | null = localStorage.getItem("journal_pinned_yy");
        if (pinnedYy != null) {
            const pinnedYyEl: HTMLElement | null = document.querySelector("#journal_aside #pinnedYy");
            if (pinnedYyEl) pinnedYyEl.textContent = pinnedYy;
        }
        const pinnedMnth: string | null = localStorage.getItem("journal_pinned_mnth");
        if (pinnedMnth != null) {
            const pinnedMnthEl: HTMLElement | null = document.querySelector("#journal_aside #pinnedMnth");
            if (pinnedMnthEl) pinnedMnthEl.textContent = pinnedMnth;
        }
    };
    const syncAsideWeekNavigatorByVueBridge = (stdrdDt?: string, weeklyList?: Record<string, any>[]): boolean => {
        const bridge = window.JournalDayAsideWeekNavigatorVueApp;
        if (bridge?.mounted === true && typeof bridge.syncWeekNavigator === "function") {
            bridge.syncWeekNavigator(stdrdDt, weeklyList);
            return true;
        }
        if (bridge && bridge.mounted !== true) {
            bridge.pendingSyncRequest = { stdrdDt, weeklyList };
            return true;
        }
        return false;
    };

    /* 변경(Phase 16): submit/closeModal/refreshIcon을 클로저 내 로컬 함수로 추출.
     * runLegacyAction에서 dF 네임스페이스 자기참조 없이 직접 호출. */
    const submitForm = (): void => {
        $("#journalDayRegForm").submit();
    };

    const closeModalAction = (): void => {
        /* 모달 이력 되돌리기 */
        ModalHistory.prev();
    };

    const refreshIconAction = (): void => {
        const iconClassElmt: HTMLInputElement = document.querySelector("#journalDayRegForm #weather");
        if (!iconClassElmt) return;
        // 입력값은 여기서 항상 문자열이다.
        const iconVal: string = iconClassElmt.value;
        if (cF.util.isNotEmpty(iconVal)) {
            const weatherIconDiv: HTMLElement = document.querySelector("#journalDayRegForm #weather_icon_div") as HTMLElement;
            if (weatherIconDiv) weatherIconDiv.innerHTML = iconVal;
        }
    };

    const runLegacyAction = (actionEl: HTMLElement): void => {
        const action: string = String(actionEl.dataset.journalDayAction ?? "").trim();
        if (action.length === 0) return;

        if (action === "open-detatched") {
            const stdrdDt: string = String(actionEl.dataset.journalDayStdrdDt ?? "").trim();
            if (stdrdDt.length === 0) return;
            /* 변경 후: 월·주·일 전역 브리지 중 활성 브리지로 Vue 소유 openDetached 호출. */
            journalDayResolveListBridge()?.openDetached?.(stdrdDt);
            return;
        }

        if (action === "move-weekly") {
            const stdrdDt: string = String(actionEl.dataset.journalDayStdrdDt ?? "").trim();
            if (stdrdDt.length === 0) return;
            /* 변경 후: dF.JournalDayViewService 제거 → 목록 Vue 브리지 사용. */
            journalDayResolveListBridge()?.moveToWeeklyView?.(stdrdDt);
            return;
        }

        if (action === "mdf-modal") {
            const idRaw: string = String(actionEl.dataset.journalDayId ?? "").trim();
            if (idRaw.length === 0) return;
            journalDayResolveListBridge()?.mdfModal?.(idRaw);
            window.JournalDayCalVueApp?.mdfModal?.(idRaw);
            return;
        }

        if (action === "delete-day") {
            const idRaw: string = String(actionEl.dataset.journalDayId ?? "").trim();
            if (idRaw.length === 0) return;
            journalDayResolveListBridge()?.delAjax?.(idRaw);
            window.JournalDayCalVueApp?.delAjax?.(idRaw);
            return;
        }

        if (action === "toggle-param") {
            /* Vue 검색 상태 브리지로 표시 필터 토글을 위임한다. */
            journalDayResolveListBridge()?.toggleParam?.();
            return;
        }

        if (action === "toggle-chapter-ctgr") {
            /* Vue 검색 상태 브리지로 챕터 카테고리 필터 토글을 위임한다. */
            journalDayResolveListBridge()?.toggleChapterCtgr?.();
            return;
        }

        if (action === "reg-modal") {
            journalDayResolveListBridge()?.regModal?.();
            window.JournalDayCalVueApp?.regModal?.();
            return;
        }

        if (action === "submit-form") {
            submitForm();
            return;
        }

        if (action === "close-modal") {
            closeModalAction();
            return;
        }

        if (action === "refresh-icon") {
            refreshIconAction();
            return;
        }

        if (action === "tag-submit-profile") {
            dfNs.JournalDayTagService?.submitProfile?.();
            return;
        }

        if (action === "tag-delete-profile") {
            dfNs.JournalDayTagService?.deleteProfileAjax?.();
            return;
        }

        if (action === "tag-ctgr-sync") {
            dfNs.JournalDayTagService?.tagCtgrSyncAjax?.();
            return;
        }

        if (action === "tag-close-modal") {
            ModalHistory.prev();
        }
    };

    const bindLegacyJournalDayActionDelegation = (): void => {
        if (legacyJournalDayActionBound) return;
        legacyJournalDayActionBound = true;

        document.addEventListener("mousedown", function(event: MouseEvent): void {
            const target = event.target as HTMLElement | null;
            const actionEl = target?.closest?.("[data-journal-day-action]") as HTMLElement | null;
            if (!actionEl) return;
            const action: string = String(actionEl.dataset.journalDayAction ?? "").trim();
            if (action !== "chapter-ctgr-select") return;
            /* Vue 검색 상태 브리지로 챕터 카테고리 전체 선택 처리를 위임한다. */
            journalDayResolveListBridge()?.handleChapterCtgrMouseDown?.(event);
        });

        document.addEventListener("change", function(event: Event): void {
            const target = event.target as HTMLElement | null;
            const actionEl = target?.closest?.("[data-journal-day-action]") as HTMLElement | null;
            if (!actionEl) return;
            const action: string = String(actionEl.dataset.journalDayAction ?? "").trim();
            if (action !== "chapter-ctgr-select") return;
            /* Vue 검색 상태 브리지로 챕터 카테고리 변경 처리를 위임한다. */
            journalDayResolveListBridge()?.changeChapterCtgr?.();
        });

        document.addEventListener("click", function(event: MouseEvent): void {
            const target = event.target as HTMLElement | null;
            const metaElmt = target?.closest?.(".meta-item") as HTMLElement | null;
            if (metaElmt) {
                event.preventDefault();
                const metaId: string = String(metaElmt.getAttribute("data-meta-id") ?? "").trim();
                if (metaId.length > 0) {
                    if (typeof dfNs.JournalDayMetaService?.openMetaModal === "function") {
                        dfNs.JournalDayMetaService.openMetaModal(metaId);
                    } else {
                        /* 변경 후(P6): 파일 이동에 맞춰 로그 접두사만 journalDayRuntimeShell 로 통일. 메시지 의미는 동일. */
                        console.error("[journalDayRuntimeShell] dF.JournalDayMetaService.openMetaModal unavailable.");
                    }
                }
                return;
            }
            const actionEl = target?.closest?.("[data-journal-day-action]") as HTMLElement | null;
            if (!actionEl) return;
            runLegacyAction(actionEl);
        });
    };

    return {
        handleLegacyActionClick: function(event: Event): void {
            const target = event?.target as HTMLElement | null;
            const actionEl = target?.closest?.("[data-journal-day-action]") as HTMLElement | null;
            if (!actionEl) return;
            runLegacyAction(actionEl);
        },

        bindLegacyJournalDayActions: function(): void {
            bindLegacyJournalDayActionDelegation();
        },

        /**
         * Tag/Meta 초기화와 레거시 <code>data-journal-day-action</code> 위임 바인딩을 한 번에 수행한다.
         *
         * 변경 전: 각 진입점(월간·캘린더·메타 페이지·일간·주간)에서 <code>dF.JournalDayTag.init()</code>,
         *   <code>dF.JournalDayMeta.init()</code>, <code>bindLegacyJournalDayActions()</code> 세 줄을 직접 호출했다.
         * 변경 후: 태그 셸은 <code>dF.JournalDayTagService.getCtgrMap()</code> + 컨텍스트 메뉴 바인딩으로 시작한다.
         *   순서 고정 및 중복 호출 방지는 동일하게 이 함수 단일 진입점에 둔다.
         */
        bootstrapDfJournalDayShell: function(): void {
            void dfNs.JournalDayTagService?.getCtgrMap?.();
            dfNs.JournalDayTagContextMenu?.bindContextMenuEvents?.();
            dfNs.JournalDayTagContextMenu?.syncContextTooltipTargets?.();
            void dfNs.JournalDayMetaService?.getCtgrMap?.();
            bindLegacyJournalDayActionDelegation();
        },

        /**
         * Aside 년월/주간 표시·Pinpoint 라벨 초기화를 수행한다.
         *
         * 변경 전: <code>dF.JournalDayAside.init()</code>가 state_service + 내부 도우미를 통해 처리했다.
         * 변경 후: 레거시 Aside 모듈 제거 후 런타임 서비스 단일 진입점으로 통합.
         */
        initJournalDayAsideShell: function(): void {
            const bridge = journalDayResolveListBridge();
            if (bridge?.mounted === true) {
                bridge.initAsideYyMnth?.();
            }
            syncPinnedYyMnthLabel();

            if (String(bridge?.getSearchParams?.()?.viewType ?? "") === "weekly") {
                if (syncAsideWeekNavigatorByVueBridge()) return;
                return;
            }
            if (bridge?.mounted === true && typeof bridge.syncAsideWeekNavigator === "function") {
                bridge.syncAsideWeekNavigator();
                return;
            }
            syncAsideWeekNavigatorByVueBridge();
        },

        /* 변경(Phase 16): refresh() 외부 호출자 0 확인 → 공개 API에서 제거.
         * reloadByView() 외부 호출자 0 → 삭제.
         * openDetatched() 외부 호출자 0 → 공개 API에서 제거.
         * submit/closeModal/refreshIcon은 HTML 템플릿 호출 가능성으로 유지. */

        /**
         * 날씨 아이콘 미리보기를 새로고침한다.
         */
        refreshIcon: refreshIconAction,

        /**
         * 폼을 제출한다.
         */
        submit: submitForm,

        /**
         * 모달 이력을 사용해 모달을 닫는다.
         */
        closeModal: closeModalAction,
    };
})();

export {};
