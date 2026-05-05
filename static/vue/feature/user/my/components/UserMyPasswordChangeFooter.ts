import { UserMyLabels } from "../types.js";

export default {
    name: "UserMyPasswordChangeFooter",
    props: {
        labels: { type: Object, required: true },
    },
    emits: ["submit"],
    computed: {
        l(): UserMyLabels {
            return this.labels as UserMyLabels;
        },
    },
    methods: {
        closeModal(): void {
            ModalHistory.pop();
        },
    },
    template: `
    <!--버튼 영역-->
    <div class="d-flex justify-content-end">
        <!--모달:: 버튼: 저장하기-->
        <button type="button" class="btn btn-sm btn-primary me-2"
                @click="$emit('submit')"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipSave">
            <i class="bi bi-pencil-square"></i>
            <span class="indicator-label">{{ l.save }}</span>
            <span class="indicator-progress">Please wait...
                <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
            </span>
        </button>
        <!--모달:: 버튼: 모달 닫기-->
        <button type="button" class="btn btn-sm btn-light"
                @click="closeModal"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" :title="l.tooltipClose">
            <i class="bi bi-x"></i>{{ l.close }}
        </button>
    </div>
    `,
};
