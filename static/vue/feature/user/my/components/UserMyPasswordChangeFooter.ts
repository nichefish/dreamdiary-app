export default {
    name: "UserMyPasswordChangeFooter",
    props: {
        t: { type: Function, required: true },
    },
    emits: ["submit"],
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
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.save')">
            <i class="bi bi-pencil-square"></i>
            <span class="indicator-label">{{ t('txt.comm.save') }}</span>
            <span class="indicator-progress">Please wait...
                <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
            </span>
        </button>
        <!--모달:: 버튼: 모달 닫기-->
        <button type="button" class="btn btn-sm btn-light"
                @click="closeModal"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" :title="t('bs.tooltip.modal.close')">
            <i class="bi bi-x"></i>{{ t('txt.comm.close') }}
        </button>
    </div>
    `,
};
