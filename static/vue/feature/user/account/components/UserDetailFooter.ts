import { UserLabels } from "../types.js";

export default {
    name: "UserDetailFooter",
    props: {
        labels: { type: Object, required: true },
    },
    emits: ["modify", "delete", "list"],
    computed: {
        l(): UserLabels {
            return this.labels as UserLabels;
        },
    },
    template: `
    <div class="card-footer d-flex justify-content-end py-6 px-9">
        <!-- 1. mdfable 항목 미사용시 : 등록자/관리자만 수정 가능 -->
        <button
            type="button"
            class="btn btn-sm btn-light btn-active-primary me-2"
            @click="$emit('modify')"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            :title="l.modifyTooltip"
        >
            <i class="bi bi-pencil-square"></i>(관리자+)수정
        </button>
        <!-- 버튼 : 권한에 따른 삭제 버튼 -->
        <button
            type="button"
            class="btn btn-sm btn-light btn-active-danger me-2 btn-sm"
            @click="$emit('delete')"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            :title="l.deleteTooltip"
        >
            <i class="bi bi-trash"></i>(관리자+)삭제
        </button>
        <!-- 버튼 : 목록으로 가기 버튼 -->
        <button
            type="button"
            class="btn btn-sm btn-light"
            @click="$emit('list')"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            data-bs-dismiss="click"
            :title="l.listTooltip"
        >
            <span class="indicator-label">
                <i class="bi bi-list"></i>목록
            </span>
        </button>
    </div>
    `,
};
