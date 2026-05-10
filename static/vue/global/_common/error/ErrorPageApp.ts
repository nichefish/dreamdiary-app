/**
 * ErrorPageApp.ts
 * 에러 페이지 Vue 앱 진입점
 *
 * @author nichefish
 */
import { resolveMessage } from "../../../common/messageHelper.js";

declare const Vue: {
    createApp: (component: unknown, props?: unknown) => { mount: (container: Element) => unknown };
};

/**
 * 에러 타입 정의
 * - 'general': 일반 에러 (500 등)
 * - 'not_found': 404 NOT FOUND
 * - 'bad_request': 400 BAD REQUEST
 * - 'access_denied': 403 FORBIDDEN
 */
type ErrorType = "general" | "not_found" | "bad_request" | "access_denied";

/**
 * 에러 페이지 컴포넌트
 */
const ErrorPageComponent = {
    name: "ErrorPageApp",
    props: {
        errorType: { type: String, default: "general" },
        errorMsg: { type: String, default: "" },
    },
    computed: {
        /**
         * 에러 타입에 따른 카테고리 레이블
         */
        categoryLabel(): string {
            switch (this.errorType) {
                case "not_found":
                    return "Error Page";
                case "bad_request":
                    return "Error Page";
                case "access_denied":
                    return "Access Denied";
                default:
                    return "Error Page";
            }
        },
        /**
         * 에러 타입에 따른 설명 텍스트
         */
        errorDescription(): string {
            switch (this.errorType) {
                case "not_found":
                    return "존재하지 않는 경로입니다";
                case "bad_request":
                    return "잘못된 요청입니다.";
                case "access_denied":
                    return resolveMessage(
                        "view.error.forbidden.2",
                        "접근 권한이 없습니다"
                    );
                default:
                    return "서버 처리 중 에러가 발생했습니다.";
            }
        },
        /**
         * 메인으로 돌아가기 레이블
         */
        returnToMainLabel(): string {
            return resolveMessage("view.return-to-main", "메인으로");
        },
        /**
         * 메인으로 돌아가기 tooltip
         */
        returnToMainTooltip(): string {
            return resolveMessage(
                "bs.tooltip.return-to-main",
                "메인으로 돌아갑니다"
            );
        },
    },
    methods: {
        /**
         * 메인으로 돌아가기 버튼 클릭 핸들러
         */
        handleMainClick(): void {
            const url = (window as any).Url?.MAIN || "/";
            if (typeof (window as any).cF?.ui?.blockUIReplace === "function") {
                (window as any).cF.ui.blockUIReplace(url);
            } else {
                window.location.href = url;
            }
        },
    },
    template: `
        <div class="d-flex-center">
            <span class="ctgr-span ctgr-imprtc">{{ categoryLabel }}</span>
        </div>
        <div class="d-flex-center flex-column mt-8">
            <div>{{ errorDescription }}</div>

            <div class="text-gray-500 fw-bold min-h-100px">{{ errorMsg }}</div>

            <div class="mt-10">
                <button
                    type="button"
                    class="btn btn-sm btn-outlined btn-light-primary btn-active-primary py-2 px-3"
                    @click="handleMainClick"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="returnToMainTooltip"
                >
                    <i class="bi bi-arrow-counterclockwise"></i>{{ returnToMainLabel }}...
                </button>
            </div>
        </div>
    `,
};

/**
 * Vue 앱 마운트 함수
 */
export function mountErrorPageApp(
    containerSelector: string,
    options?: {
        errorType?: ErrorType;
        errorMsg?: string;
    }
): void {
    const container = document.querySelector(containerSelector);
    if (!container) {
        console.error(`[ErrorPageApp] Container not found: ${containerSelector}`);
        return;
    }

    const errorType = options?.errorType || "general";
    const errorMsg = options?.errorMsg || "";

    // Vue 앱 생성 및 마운트
    Vue.createApp(ErrorPageComponent, {
        errorType,
        errorMsg,
    }).mount(container);
}

/**
 * 글로벌 Vue 앱 인스턴스 노출
 */
(window as any).ErrorPageVueApp = {
    mount: mountErrorPageApp,
};
