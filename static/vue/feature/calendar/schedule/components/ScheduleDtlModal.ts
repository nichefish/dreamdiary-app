/**
 * ScheduleDtlModal.ts
 * 일정 상세 조회 모달 Vue 컴포넌트
 *
 * 변경(D): schedule_module.ts의 dtlModal/mdfModal/delAjax 로직 흡수.
 *          - Handlebars 템플릿(_schedule_dtl_modal_template.hbs) 제거 후 Vue 컴포넌트로 대체.
 *          - window CustomEvent('schedule:open-dtl-modal') 수신 → AJAX 조회 후 모달 표시.
 *          - 수정 버튼: schedule:open-mdf-modal 발행 → ScheduleRegModal 이 수신.
 *          - 삭제 버튼: scheduleActionService.del() 호출 후 schedule:calendar-refresh 발행.
 *
 * @author nichefish
 */
import type { ScheduleDetail } from "../types.js";
import scheduleDataService   from "../services/scheduleDataService.js";
import scheduleActionService from "../services/scheduleActionService.js";

declare const Vue: any;
declare const Swal: any;

const ScheduleDtlModal = {
    name: "ScheduleDtlModal",

    data() {
        return {
            detail:      null as ScheduleDetail | null,
            holyDayCode: ""   as string,
        };
    },

    computed: {
        /** 일정 종류가 공휴일(HOLYDAY)인지 여부 */
        isHolyDay(): boolean {
            return !!(this as any).detail && (this as any).detail.scheduleCd === (this as any).holyDayCode;
        },
        /** "[종류명] 제목" 형식으로 표시할 제목 */
        displayTitle(): string {
            const d: ScheduleDetail | null = (this as any).detail;
            if (!d) return "";
            const prefix: string = d.scheduleNm ? `[${d.scheduleNm}] ` : "";
            return prefix + (d.title || "");
        },
    },

    methods: {
        /**
         * 상세 조회 후 모달 표시
         * @param {string|number} id - 조회할 일정 번호.
         */
        async openDtl(id: string | number): Promise<void> {
            try {
                (this as any).detail = await scheduleDataService.getDetail(id);
                (this as any)._showModal();
            } catch (message) {
                if (message) (Swal as any).fire({ text: message });
            }
        },

        /** Bootstrap 5 모달 표시 */
        _showModal(): void {
            const el = document.getElementById("schedule_dtl_modal");
            if (!el) return;
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
        },

        /** Bootstrap 5 모달 숨김 */
        _hideModal(): void {
            const el = document.getElementById("schedule_dtl_modal");
            if (!el) return;
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).hide();
        },
    },

    mounted(): void {
        // holyDayCode 읽기 (schedule_cal.ftlh 에서 주입한 schedule_reg_form_data 공유)
        const dataEl = document.getElementById("schedule_reg_form_data");
        if (dataEl) {
            try {
                const parsed: { holyDayCode?: string } = JSON.parse(dataEl.textContent || "{}");
                (this as any).holyDayCode = parsed.holyDayCode || "";
            } catch { /* ignore parse error */ }
        }

        // CustomEvent 수신: FullCalendar 이벤트 클릭 → 상세 조회
        window.addEventListener("schedule:open-dtl-modal", (e: Event): void => {
            const id = (e as CustomEvent<{ id: string | number }>).detail.id;
            (this as any).openDtl(id);
        });

        // CustomEvent 수신: 모달 footer 수정 버튼 클릭
        window.addEventListener("schedule:dtl-mdf-click", (): void => {
            if (!(this as any).detail) return;
            (this as any)._hideModal();
            window.dispatchEvent(
                new CustomEvent("schedule:open-mdf-modal", { detail: { data: (this as any).detail } })
            );
        });

        // CustomEvent 수신: 모달 footer 삭제 버튼 클릭
        window.addEventListener("schedule:dtl-del-click", (): void => {
            const detail: ScheduleDetail | null = (this as any).detail;
            if (!detail?.id) return;
            scheduleActionService.del(detail.id, (): void => {
                (this as any)._hideModal();
                window.dispatchEvent(new CustomEvent("schedule:calendar-refresh"));
            });
        });
    },

    template: `
    <template v-if="detail">
        <!-- begin::Row: 제목 -->
        <div class="row mb-3">
            <div class="col-xl-2 col-3 col-form-label">
                <i class="bi bi-justify-left fs-5 me-1"></i>
                <label class="fs-6 fw-bold mb-2">제목</label>
            </div>
            <div class="col-xl-8 col-9 col-form-label">
                {{ displayTitle }}
            </div>
        </div>
        <!-- begin::Row: 참여자 (공휴일 제외) -->
        <div v-if="!isHolyDay" class="row mb-3">
            <div class="col-xl-2 col-3 col-form-label">
                <i class="bi bi-people fs-5 me-1"></i>
                <label class="fs-6 fw-bold mb-2">참여자</label>
            </div>
            <div class="col-xl-10 col-9 col-form-label">
                {{ detail.prtcpnt || "-" }}
            </div>
        </div>
        <!-- begin::Row: 설명 -->
        <div class="row mb-3">
            <div class="col-xl-2 col-3 col-form-label">
                <i class="bi bi-info-circle fs-5 me-1"></i>
                <label class="fs-6 fw-bold mb-2">설명</label>
            </div>
            <div class="col-xl-10 col-9 col-form-label">
                <div class="div-textarea-smp h-auto min-h-100px">{{ detail.content }}</div>
            </div>
        </div>
        <!-- begin::Row: 시작일 + 종료일 -->
        <div class="row row-cols-lg-2 mb-3">
            <div class="col-xl-2 col-3 col-form-label">
                <i class="bi bi-calendar3 fs-5 me-1"></i>
                <label class="fs-6 fw-bold mb-2">시작일</label>
            </div>
            <div class="col-xl-4 col-3 pe-0 col-form-label">
                <span id="bgnDtDtlSpan">{{ detail.bgnDt }}</span>
            </div>
            <div class="col-xl-2 col-3 col-form-label">
                <i class="bi bi-calendar3 fs-5 me-1"></i>
                <label class="fs-6 fw-bold mb-2">종료일</label>
            </div>
            <div class="col-xl-4 col-3 pe-0 col-form-label">
                <span id="endDtDtlSpan">{{ detail.endDt }}</span>
            </div>
        </div>
    </template>
    `,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    const mountEl = document.getElementById("schedule_dtl_modal_app");
    if (!mountEl) return;

    (Vue as any).createApp({
        components: { ScheduleDtlModal },
        template: `<ScheduleDtlModal />`,
    }).mount("#schedule_dtl_modal_app");
});