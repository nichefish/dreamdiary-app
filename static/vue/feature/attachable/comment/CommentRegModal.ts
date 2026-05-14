/**
 * CommentRegModal.ts
 * 댓글 등록/수정 모달 Vue 컴포넌트
 *
 * 변경(D): _comment_reg_modal.ftlh 의 comment_modal_module 인라인 스크립트와
 *          Handlebars 헤더 템플릿을 Vue 컴포넌트로 전환.
 *          - CustomEvent('comment:open-reg-modal') → 신규 등록 모달 진입.
 *          - CustomEvent('comment:open-mdf-modal') → 수정 모달 진입 (commentDataService.getDetail).
 *          - CustomEvent('comment:submit-reg-form') → 폼 제출 트리거 (모달 푸터 버튼 대응).
 *          - 성공 시 CustomEvent('comment:modal-refresh') dispatch → 각 페이지 리프레시.
 *
 * @author nichefish
 */
import commentDataService from "./services/commentDataService.js";
import commentActionService from "./services/commentActionService.js";
import type { CommentForm } from "./types.js";

declare const Vue: any;
declare const cF: any;

const CommentRegModal = {
    name: "CommentRegModal",
    data(): {
        id: string | number | undefined;
        refId: string | number;
        refContentType: string;
        actvtyCtgrCd: string;
    } {
        return {
            id: undefined,
            refId: "",
            refContentType: "",
            actvtyCtgrCd: "",
        };
    },
    mounted(): void {
        // actvtyCtgrCd JSON 주입값 읽기
        const dataEl = document.getElementById("comment_reg_modal_data");
        if (dataEl) {
            try {
                const parsed: { actvtyCtgrCd?: string } = JSON.parse(dataEl.textContent || "{}");
                this.actvtyCtgrCd = parsed.actvtyCtgrCd || "";
            } catch { /* ignore parse error */ }
        }

        window.addEventListener("comment:open-reg-modal", (e: Event): void => {
            const { refId, refContentType } =
                (e as CustomEvent<{ refId: string | number; refContentType: string }>).detail;
            this.open(refId, refContentType);
        });
        window.addEventListener("comment:open-mdf-modal", (e: Event): void => {
            const { id } = (e as CustomEvent<{ id: string | number }>).detail;
            void this.openMdf(id);
        });
        window.addEventListener("comment:submit-reg-form", (): void => {
            this.submit();
        });
    },
    methods: {
        /**
         * 신규 등록 모달 열기
         * @param {string|number} refId - 참조 게시물 번호.
         * @param {string} refContentType - 참조 콘텐츠 타입.
         */
        open(refId: string | number, refContentType: string): void {
            if (!refId || !refContentType) return;
            this.id = undefined;
            this.refId = refId;
            this.refContentType = refContentType;
            this._showModal();
            this._initTinymce("");
        },
        /**
         * 수정 모달 열기 — 서버에서 댓글 데이터 로드 후 폼 채움
         * @param {string|number} id - 수정할 댓글 번호.
         */
        async openMdf(id: string | number): Promise<void> {
            if (isNaN(Number(id))) return;
            let data: CommentForm;
            try {
                data = await commentDataService.getDetail(id);
            } catch { return; }
            this.id = data.id;
            this.refId = data.refId;
            this.refContentType = data.refContentType;
            this._showModal();
            this._initTinymce(data.content || "");
        },
        /**
         * 폼 제출 (등록 또는 수정)
         */
        submit(): void {
            (window as any).tinymce?.get("tinymce_commentCn")?.save();
            const formEl = document.getElementById("commentRegForm") as HTMLFormElement | null;
            if (!formEl) return;
            commentActionService.save(this.id, formEl, (): void => {
                this._hideModal();
                window.dispatchEvent(new CustomEvent("comment:modal-refresh"));
            });
        },
        _showModal(): void {
            const el = document.getElementById("comment_reg_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
        },
        _hideModal(): void {
            const el = document.getElementById("comment_reg_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).hide();
        },
        _initTinymce(content: string): void {
            (cF as any).tinymce.init("#tinymce_commentCn");
            (cF as any).tinymce.setContentWhenReady("tinymce_commentCn", content);
        },
    },
    template: `
    <div>
        <input type="hidden" name="id" :value="id ?? ''" />
        <input type="hidden" name="refId" :value="refId" />
        <input type="hidden" name="refContentType" :value="refContentType" />
        <input type="hidden" name="actvtyCtgrCd" :value="actvtyCtgrCd" />
    </div>
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
    const mountEl = document.getElementById("comment_reg_modal_app");
    if (!mountEl) return;

    Vue.createApp({
        components: { CommentRegModal },
        template: `<CommentRegModal />`,
    }).mount("#comment_reg_modal_app");
});