/**
 * 인증 정책 저장 버튼 영역 (card-footer).
 * 저장 확정은 부모(authPolicyActionService)에서 처리한다.
 */
export default {
    name: "AuthPolicyFooter",
    emits: ["save"],
    template: `
    <div class="card-footer">
        <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-sm btn-primary mx-2" @click="$emit('save')"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                    :title="$t('bs.tooltip.save')">
                <i class="bi bi-pencil-square"></i>
                <span class="indicator-label">{{ $t('txt.admin.auth.policy.btn.save') }}</span>
                <span class="indicator-progress">Please wait...
                    <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                </span>
            </button>
        </div>
    </div>
    `,
};
