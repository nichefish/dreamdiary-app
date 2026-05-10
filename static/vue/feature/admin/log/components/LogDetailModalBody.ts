import { LogDetail } from "../types.js";

export default {
    name: "LogDetailModalBody",
    props: {
        detail: { type: Object, required: false, default: null },
    },
    computed: {
        item(): LogDetail {
            return (this.detail || {}) as LogDetail;
        },
        resultText(): string {
            const value = this.item.rslt;
            if (value === true || value === "true") return this.$t("txt.admin.log.list.result.success");
            if (value === false || value === "false") return this.$t("txt.admin.log.list.result.failure");
            return String(value || "-");
        },
    },
    methods: {
        valueOf(value: unknown): string {
            if (value === undefined || value === null || value === "") return "-";
            return String(value);
        },
    },
    template: `
    <div class="mb-0">
        <div class="d-flex align-items-center mb-4">
            <div class="row">
                <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                    <span class="vertical-middle">{{ valueOf(item.title) }}</span>
                </h1>
            </div>
        </div>
        <div class="separator separator-dashed border-gray-300 my-8"></div>
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.log-type') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label">{{ valueOf(item.actvtyCtgrNm) }}</div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.actor') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label">{{ valueOf(item.username) }}</div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.action-datetime') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label">{{ valueOf(item.logDt) }}</div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.action-ip') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label">{{ valueOf(item.ipAddr) }}</div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">URL</div></div>
                <div class="col-xl-10 col-8 col-form-label">
                    <div>{{ valueOf(item.url) }}</div>
                    <div>referer: {{ valueOf(item.referer) }}</div>
                </div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.parameters') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label text-break">{{ valueOf(item.param) }}</div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.action-content') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label text-break">{{ valueOf(item.content) }}</div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.action-result') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label">
                    <div>{{ resultText }}</div>
                    <div>{{ valueOf(item.rsltMsg) }}</div>
                </div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-4"><div class="col-form-label text-center fs-6 fw-bold">{{ $t('txt.admin.log.detail.exception') }}</div></div>
                <div class="col-xl-10 col-8 col-form-label">
                    <div>{{ valueOf(item.exceptionNm) }}</div>
                    <div class="text-break">{{ valueOf(item.exceptionMsg) }}</div>
                </div>
            </div>
        </div>
    </div>
    `,
};
