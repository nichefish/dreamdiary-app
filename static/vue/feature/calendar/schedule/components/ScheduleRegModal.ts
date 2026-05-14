/**
 * ScheduleRegModal.ts
 * 일정 등록/수정 모달 Vue 컴포넌트
 *
 * 변경(D): schedule_module.ts의 regModal/prvtRegModal/addPrtcpnt/removePrtcpnt/regAjax 로직 흡수.
 *          - Handlebars 템플릿(_schedule_reg_modal_template.hbs) 제거 후 Vue 컴포넌트로 대체.
 *          - window CustomEvent('schedule:open-reg-modal') / ('schedule:open-prvt-reg-modal') 수신 → 신규 등록.
 *          - window CustomEvent('schedule:open-mdf-modal') 수신 → 수정 (dtl 모달에서 발행).
 *          - 제출 성공 시 window CustomEvent('schedule:calendar-refresh') 발행.
 *
 * @author nichefish
 */
import type { ScheduleForm, ScheduleDetail, ScheduleCodeOption, ScheduleUserOption, SchedulePrtcpnt } from "../types.js";
import scheduleActionService from "../services/scheduleActionService.js";

declare const Vue: any;
declare const cF: any;

/** 폼 기본값 */
const EMPTY_FORM: ScheduleForm = {
    scheduleCd: "",
    title:       "",
    content:     "",
    bgnDt:       "",
    endDt:       "",
    jandiYn:     "N",
    privateYn:   "N",
};

/** 참가자 행 내부 인덱스 포함 타입 */
type PrtcpntRow = SchedulePrtcpnt & { _idx: number };

/** isPrvt 시 제외할 코드 목록 */
const PRVT_EXCLUDE_CODES = ["HOLYDAY", "CEREMONY", "TLCMMT"];

const ScheduleRegModal = {
    name: "ScheduleRegModal",

    data() {
        return {
            form:         { ...EMPTY_FORM } as ScheduleForm,
            isPrvt:       false            as boolean,
            prtcpntList:  []               as PrtcpntRow[],
            prtcpntCnt:   0               as number,
            showEndDt:    true             as boolean,
            codeOptions:  []               as ScheduleCodeOption[],
            userOptions:  []               as ScheduleUserOption[],
            holyDayCode:  ""               as string,
        };
    },

    computed: {
        /** isPrvt 시 공개 전용 코드(HOLYDAY·CEREMONY·TLCMMT) 제외 */
        filteredCodeOptions(): ScheduleCodeOption[] {
            if (!(this as any).isPrvt) return (this as any).codeOptions;
            return ((this as any).codeOptions as ScheduleCodeOption[]).filter(
                (opt: ScheduleCodeOption): boolean => !PRVT_EXCLUDE_CODES.includes(opt.code)
            );
        },
    },

    methods: {
        /**
         * 폼 초기화 후 모달 표시 (신규 등록)
         * @param {boolean} isPrvt - 개인용 일정 여부.
         */
        open(isPrvt: boolean = false): void {
            (this as any).isPrvt      = isPrvt;
            (this as any).form        = { ...EMPTY_FORM, privateYn: isPrvt ? "Y" : "N" };
            (this as any).prtcpntList = [];
            (this as any).prtcpntCnt  = 0;
            (this as any).showEndDt   = true;
            (this as any)._showModal();
            (Vue as any).nextTick((): void => {
                (this as any)._initDatepickers();
                if (!isPrvt) (this as any)._initJandiCheckbox();
                (this as any)._toggleJandiArea(!isPrvt);
            });
        },

        /**
         * 기존 데이터로 폼 초기화 후 모달 표시 (수정)
         * @param {ScheduleDetail} data - 서버에서 조회한 일정 상세 데이터.
         */
        openMdf(data: ScheduleDetail): void {
            const isPrvt: boolean = data.privateYn === "Y";
            (this as any).isPrvt = isPrvt;
            (this as any).form   = {
                id:         data.id          ?? "",
                scheduleCd: data.scheduleCd  || "",
                title:      data.title       || "",
                content:    data.content     || "",
                bgnDt:      data.bgnDt       || "",
                endDt:      data.endDt       || "",
                jandiYn:    "N",
                privateYn:  data.privateYn   || "N",
            };
            const prtcpnts: SchedulePrtcpnt[] = data.prtcpntList || [];
            (this as any).prtcpntList = prtcpnts.map(
                (p: SchedulePrtcpnt, i: number): PrtcpntRow => ({ ...p, _idx: i })
            );
            (this as any).prtcpntCnt = (this as any).prtcpntList.length;
            (this as any).showEndDt  = true;
            (this as any)._showModal();
            (Vue as any).nextTick((): void => {
                (this as any)._initDatepickers(data.bgnDt, data.endDt);
                if (!isPrvt) (this as any)._initJandiCheckbox();
                (this as any)._toggleJandiArea(!isPrvt);
            });
        },

        /**
         * scheduleCd 변경 시 HOLYDAY 코드이면 종료일 숨김
         * @param {Event} evt - select change 이벤트.
         */
        onScheduleCdChange(evt: Event): void {
            const val: string = (evt.target as HTMLSelectElement).value;
            if (val && val === (this as any).holyDayCode) {
                (this as any).form.endDt  = (this as any).form.bgnDt;
                (this as any).showEndDt   = false;
            } else {
                (this as any).showEndDt = true;
            }
        },

        /** 참가자 행 추가 */
        addPrtcpnt(): void {
            ((this as any).prtcpntList as PrtcpntRow[]).push({
                username: "",
                _idx: (this as any).prtcpntCnt++,
            });
        },

        /**
         * 참가자 행 삭제
         * @param {number} idx - 삭제할 행의 내부 인덱스.
         */
        removePrtcpnt(idx: number): void {
            const list: PrtcpntRow[] = (this as any).prtcpntList;
            const i: number = list.findIndex((p: PrtcpntRow): boolean => p._idx === idx);
            if (i !== -1) list.splice(i, 1);
        },

        /**
         * 날짜 선택기 초기화 (모달 열릴 때마다 재설정)
         * @param {string} [bgnDtVal] - 시작일 초기값 (수정 모드에서 사용).
         * @param {string} [endDtVal] - 종료일 초기값 (수정 모드에서 사용).
         */
        _initDatepickers(bgnDtVal?: string, endDtVal?: string): void {
            (cF as any).datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", bgnDtVal || "");
            (cF as any).datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", endDtVal || "");
        },

        /** 잔디 발송 체크박스 라벨 초기화 */
        _initJandiCheckbox(): void {
            (cF as any).ui.chckboxLabel(
                "#jandiYn", "발송//미발송", "blue//gray",
                (): void => {
                    const el = document.getElementById("trgetTopicSpan");
                    if (el) el.style.display = "";
                },
                (): void => {
                    const el = document.getElementById("trgetTopicSpan");
                    if (el) el.style.display = "none";
                }
            );
            (document.getElementById("jandiYn") as HTMLElement | null)?.click();
        },

        /**
         * 잔디 알림 영역 표시/숨김
         * @param {boolean} show - true 이면 표시.
         */
        _toggleJandiArea(show: boolean): void {
            const el = document.getElementById("schedule_jandi_area");
            if (el) el.style.display = show ? "" : "none";
        },

        /** Bootstrap 5 모달 표시 */
        _showModal(): void {
            const modalEl = document.getElementById("schedule_reg_modal");
            if (!modalEl) return;
            (window as any).bootstrap?.Modal.getOrCreateInstance(modalEl).show();
        },

        /** Bootstrap 5 모달 숨김 */
        _hideModal(): void {
            const modalEl = document.getElementById("schedule_reg_modal");
            if (!modalEl) return;
            (window as any).bootstrap?.Modal.getOrCreateInstance(modalEl).hide();
        },

        /** 등록/수정 폼 제출 (scheduleActionService 위임) */
        submitForm(): void {
            const ajaxData: Record<string, unknown> = (cF as any).util.getJsonFormData("#scheduleRegForm");
            scheduleActionService.reg(ajaxData, (): void => {
                (this as any)._hideModal();
                window.dispatchEvent(new CustomEvent("schedule:calendar-refresh"));
            });
        },
    },

    mounted(): void {
        // form data 초기화 (schedule_cal.ftlh 주입)
        const dataEl = document.getElementById("schedule_reg_form_data");
        if (dataEl) {
            try {
                const parsed: {
                    holyDayCode?: string;
                    codeOptions?: ScheduleCodeOption[];
                    userOptions?: ScheduleUserOption[];
                } = JSON.parse(dataEl.textContent || "{}");
                (this as any).holyDayCode = parsed.holyDayCode  || "";
                (this as any).codeOptions = parsed.codeOptions  || [];
                (this as any).userOptions = parsed.userOptions  || [];
            } catch { /* ignore parse error */ }
        }

        // jQuery Validate 초기화 (DOM 마운트 후 즉시)
        (cF as any).validate.validateForm("#scheduleRegForm", (): void => {
            (this as any).submitForm();
        });

        // CustomEvent 수신: 신규 등록
        window.addEventListener("schedule:open-reg-modal", (): void => {
            (this as any).open(false);
        });
        // CustomEvent 수신: 개인용 신규 등록
        window.addEventListener("schedule:open-prvt-reg-modal", (): void => {
            (this as any).open(true);
        });
        // CustomEvent 수신: 수정 (ScheduleDtlModal 에서 발행)
        window.addEventListener("schedule:open-mdf-modal", (e: Event): void => {
            (this as any).openMdf((e as CustomEvent<{ data: ScheduleDetail }>).detail.data);
        });
        // CustomEvent 수신: 모달 footer 버튼이 폼 submit 트리거
        window.addEventListener("schedule:submit-reg-form", (): void => {
            (document.getElementById("scheduleRegForm") as HTMLFormElement | null)?.dispatchEvent(
                new Event("submit", { bubbles: true, cancelable: true })
            );
        });
    },

    template: `
    <template>
        <input type="hidden" name="id" id="id" :value="form.id || ''">
        <input type="hidden" name="privateYn" id="privateYn" :value="form.privateYn || 'N'">

        <!-- begin::Row: 구분 + 제목 -->
        <div class="row mb-3">
            <div class="col-xl-2 col-2">
                <label class="col-form-label text-center fs-6 fw-bold required">제목</label>
            </div>
            <div class="col-xl-3 col-4">
                <select name="scheduleCd" id="scheduleCd" class="form-select form-select-solid required"
                        :value="form.scheduleCd" @change="onScheduleCdChange">
                    <option value="">--구분--</option>
                    <option v-for="opt in filteredCodeOptions" :key="opt.code" :value="opt.code">
                        [{{ opt.codeName }}]
                    </option>
                </select>
                <span id="scheduleCd_validate_span"></span>
            </div>
            <div class="col-xl-7 col-6">
                <input type="text" name="title" id="title" class="form-control form-control-solid required"
                       :value="form.title" placeholder="제목" maxlength="120" />
                <span id="title_validate_span"></span>
            </div>
        </div>

        <!-- begin::Row: 참여자 -->
        <div class="row mb-3">
            <div class="col-xl-2 col-2 d-flex align-items-center justify-content-between">
                <label class="col-form-label text-center fs-6 fw-bold" for="prtcpnt">참여자</label>
                <span v-if="!isPrvt" class="btn btn-sm btn-icon btn-light-primary" @click="addPrtcpnt">
                    <i class="fas fa-plus float-end"></i>
                </span>
            </div>
            <template v-if="isPrvt">
                <div class="col-xl-10 col-10 d-flex align-items-center">
                    <div class="text-noti fs-small">※개인용 일정의 경우 참여자에게 공유할 수 있습니다. (본인과 참여자 화면에만 노출됩니다.)</div>
                </div>
                <div class="col-xl-2 col-2"></div>
            </template>
            <div class="col-xl-10 col-10" id="schedule_reg_prtcpnt_div">
                <div v-for="row in prtcpntList" :key="row._idx" class="d-flex align-items-center mb-1">
                    <select :name="'prtcpntList[' + row._idx + '].username'"
                            class="form-select form-select-solid me-2">
                        <option value="">--선택--</option>
                        <option v-for="user in userOptions" :key="user.username" :value="user.username"
                                :selected="row.username === user.username">
                            {{ user.userNm }}
                        </option>
                    </select>
                    <span class="btn btn-sm btn-icon btn-light-danger" @click="removePrtcpnt(row._idx)">
                        <i class="fas fa-trash float-end"></i>
                    </span>
                </div>
            </div>
        </div>

        <!-- begin::Row: 내용 -->
        <div class="row mb-3">
            <div class="col-xl-2 col-2">
                <label class="col-form-label text-center fs-6 fw-bold mb-2" for="content">내용</label>
            </div>
            <div class="col-xl-10 col-10">
                <textarea name="content" id="content" class="form-control form-control-solid h-100px"
                          wrap="hard" maxlength="500">{{ form.content }}</textarea>
            </div>
        </div>

        <!-- begin::Row: 시작일 + 종료일 -->
        <div class="row row-cols-lg-2 mb-3">
            <div class="col-xl-2 col-2">
                <label class="col-form-label text-center fs-6 fw-bold mb-2">일정</label>
            </div>
            <div class="col-xl-5 col-5">
                <label class="fs-6 mt-3 mb-2 required" for="bgnDt">시작일</label>
                <input name="bgnDt" id="bgnDt" class="form-control form-control-solid required"
                       :value="form.bgnDt" placeholder="시작일" readonly />
                <div id="bgnDt_validate_span"></div>
            </div>
            <div class="col-xl-5 col-5" id="endDtDiv" :style="showEndDt ? '' : 'display:none'">
                <label class="fs-6 mt-3 mb-2" for="endDt">종료일</label>
                <input name="endDt" id="endDt" class="form-control form-control-solid"
                       :value="form.endDt" placeholder="종료일" readonly />
                <div id="endDtSpan" class="text-noti">※미입력시 시작일자와 같게끔 처리됩니다.</div>
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
    const mountEl = document.getElementById("schedule_reg_modal_app");
    if (!mountEl) return;

    (Vue as any).createApp({
        components: { ScheduleRegModal },
        template: `<ScheduleRegModal />`,
    }).mount("#schedule_reg_modal_app");
});