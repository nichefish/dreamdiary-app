/**
 * JournalDayAsideTodoCardApp.ts
 * 저널 일자 사이드바 — TODO 카드(헤더·목록) Vue 렌더.
 *
 * 변경 전: _journal_day_aside_base.ftlh 정적 헤더 + Handlebars <code>journal_todo_list_template</code> 가 <code>#journal_todo_list_div</code> 를 채움.
 * 변경 후: 동일 id·클래스·행 구조 유지. 데이터 반영은 <code>journal_todo_module.yyMnthListAjax</code> 가
 *         <code>window.JournalDayAsideTodoVueApp.applyTodoListPayload</code> 로 위임(마운트 전 응답은 <code>pendingTodoListPayload</code> 대기).
 */

import journalTodoCrudService from "../todo/services/journalTodoCrudService.js";
// 변경(D): 글로벌 `Message` 결의를 `resolveMessage` 헬퍼로 위임 — typeof guard 분기 통일.
import { resolveMessage } from "../../../common/messageHelper.js";

type TodoRow = { id: string | number; title: string };

/** 마운트 훅에서 참조할 옵션 API 인스턴스 최소 면 */
type ThisTodoCard = {
    applyTodoRows: (raw: unknown) => void;
    $el: Element;
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function asideMsg(key: string): string {
    return resolveMessage(key);
}

function coerceTodoRows(raw: unknown): TodoRow[] {
    if (!Array.isArray(raw)) {
        console.warn("[JournalDayAsideTodoCardApp] rsltList 가 배열이 아님 — 빈 목록으로 표시.", raw);
        return [];
    }
    return raw.map(function(row: Record<string, unknown>): TodoRow {
        const id = row.id as string | number;
        return { id, title: String(row.title ?? "") };
    });
}

function initTodoCardTooltips(root: HTMLElement): void {
    const win = globalThis as unknown as {
        bootstrap?: { Tooltip?: new (el: HTMLElement) => unknown };
    };
    const TooltipCtor = win.bootstrap?.Tooltip;
    Vue.nextTick(function(): void {
        root.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
            try {
                if (TooltipCtor) {
                    void new TooltipCtor(el as HTMLElement);
                }
            } catch (e) {
                console.warn("[JournalDayAsideTodoCardApp] tooltip init 실패:", e);
            }
        });
    });
}

const JournalDayAsideTodoCardRoot = {
    name: "JournalDayAsideTodoCardRoot",
    data(): { todos: TodoRow[] } {
        return { todos: [] };
    },
    mounted(): void {
        const vm = this as ThisTodoCard;
        const w = window as Window & {
            JournalDayAsideTodoVueApp?: {
                mounted?: boolean;
                pendingTodoListPayload?: unknown;
                applyTodoListPayload?: (payload: unknown) => void;
            };
        };

        const pending: unknown = w.JournalDayAsideTodoVueApp?.pendingTodoListPayload;
        w.JournalDayAsideTodoVueApp = {
            mounted: true,
            pendingTodoListPayload: null,
            applyTodoListPayload(payload: unknown): void {
                vm.applyTodoRows(payload);
            },
        };

        if (pending !== undefined && pending !== null) {
            vm.applyTodoRows(pending);
        } else {
            initTodoCardTooltips(vm.$el as HTMLElement);
        }

        console.log("'JournalDayAsideTodoCardApp' 마운트 완료(aside TODO).");
    },
    methods: {
        tooltipAdd(): string {
            return asideMsg("bs.tooltip.journal.aside-todo-add");
        },
        tooltipEmpty(): string {
            return asideMsg("txt.journal.todo.empty");
        },
        tooltipDeleteBtn(): string {
            /* 변경 없음 — 기존 HBS 의 삭제 버튼 title 과 동일(문구 혼합은 서버 레이블 키 그대로). */
            return asideMsg("txt.journal.day") + " " + asideMsg("bs.tooltip.modal.modify");
        },
        applyTodoRows(raw: unknown): void {
            this.todos = coerceTodoRows(raw);
            Vue.nextTick(function(): void {
                const listRoot = document.getElementById("journal_todo_list_div") as HTMLElement | null;
                if (listRoot) initTodoCardTooltips(listRoot);
            });
            const hdr = document.getElementById("journal_todo_aside_header") as HTMLElement | null;
            if (hdr) initTodoCardTooltips(hdr);
        },
        registModal(): void {
            /* 변경(T-2-α): dF.JournalTodo.registModal 진입 → window.JournalTodoRegistVueApp.open 단일 진입.
             * yy/mnth 파생은 기존 dF.JournalTodo.registModal 안의 로직과 동일하게 #journal_aside #yy /
             * #journal_aside #mnth 셀렉트에서 가져온다. dF.JournalTodo 모듈은 β 단계에서 제거된다. */
            const yyElmt: HTMLSelectElement | null = document.querySelector("#journal_aside #yy");
            const mnthElmt: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth");
            const yy: string = yyElmt?.value ?? "";
            const mnth: string = mnthElmt?.value ?? "";
            if (cF.util.isEmpty(yy) || cF.util.isEmpty(mnth)) return;

            const win = window as Window & {
                JournalTodoRegistVueApp?: {
                    mounted?: boolean;
                    pendingPayload?: Record<string, any> | null;
                    open?: (model: Record<string, any>) => void;
                };
            };
            const bridge = win.JournalTodoRegistVueApp;
            if (bridge && bridge.mounted === true && typeof bridge.open === "function") {
                bridge.open({ yy, mnth });
                return;
            }
            /* mounted 전 진입 시 pendingPayload 로 큐잉 — RegistModalApp.runWhenDomReady 후속에서 소비. */
            if (bridge) {
                bridge.pendingPayload = { yy, mnth };
                return;
            }
            console.error("[JournalDayAsideTodoCardApp] registModal 불가 — JournalTodoRegistVueApp bridge 미정의");
        },
        deleteTodo(id: string | number): void {
            /* 변경(T-2-β): dF.JournalTodo.deleteAjax 진입 → journalTodoCrudService.deleteAjax 단일 진입.
             * dF.JournalTodo 모듈은 본 단계에서 통째 제거된다(외부 호출자 0). */
            journalTodoCrudService.deleteAjax(id);
        },
    },
    template: `
    <div class="card card-reset card-p-0">
        <div id="journal_todo_aside_header" class="card-header min-h-auto mb-5">
            <h3 class="card-title text-gray-900 fw-bold fs-3">
                <i class="bi bi-list-task fs-2 me-1"></i> TODO List
            </h3>
            <div class="card-toolbar">
                <a href="javascript:void(0);" class="btn btn-sm btn-icon btn-primary"
                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                   :title="tooltipAdd()"
                   @click.prevent="registModal">
                    <i class="bi bi-plus fs-2 pe-0" id="journalTodoAsideRegistIcon"></i>
                </a>
            </div>
        </div>
        <div id="journal_todo_list_div">
            <template v-if="todos.length > 0">
                <div v-for="item in todos" :key="'todo-' + item.id"
                     class="row d-flex-align-center justify-content-between">
                    <div class="col text-truncate cursor-pointer"
                         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                         :title="item.title">
                        {{ item.title }}
                    </div>
                    <div class="col-3 d-flex justify-content-end">
                        <button type="button" class="btn btn-sm btn-light-danger btn-outlined py-2 px-3 cursor-pointer"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                :title="tooltipDeleteBtn()"
                                @click.prevent="deleteTodo(item.id)">
                            <i class="bi bi-trash p-0"></i>
                        </button>
                    </div>
                </div>
            </template>
            <div v-else class="journal-day d-flex-center">
                {{ tooltipEmpty() }}
            </div>
        </div>
    </div>
    `,
};

runWhenDomReady(function(): void {
    const mountEl = document.querySelector("#journal_day_aside_todo_card_mount") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayAsideTodoCardApp] 마운트 루트 #journal_day_aside_todo_card_mount 없음.");
        return;
    }

    try {
        const app = Vue.createApp(JournalDayAsideTodoCardRoot);
        app.mount(mountEl);
    } catch (e) {
        console.error("[JournalDayAsideTodoCardApp] Vue 마운트 실패:", e);
    }
});

export {};
