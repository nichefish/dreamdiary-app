import { fallbackText } from "../profileEmplymShared.js";

type ReadRow = {
    label: string;
    value: string | null | undefined;
    lunarBadge?: boolean;
    lunarLabel?: string;
    asTextarea?: boolean;
};

/**
 * 사용자 프로필/인사 조회 공통 섹션
 *
 * @author nichefish
 */
export default {
    name: "UserProfileEmplymReadSection",
    props: {
        profileRows: { type: Array, required: true },
        emplymRows: { type: Array, required: true },
    },
    methods: {
        fallback(value: string | null | undefined): string {
            return fallbackText(value);
        },
    },
    template: `
    <template v-if="profileRows.length">
        <div class="separator my-2"></div>
        <div class="card-body">
            <div v-for="row in profileRows" :key="'p-' + row.label" class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ row.label }}</div>
                </div>
                <div class="col-xl-9 col-8 col-form-label">
                    <template v-if="row.asTextarea">
                        <div class="div-textarea h-auto min-h-125px">{{ fallback(row.value) }}</div>
                    </template>
                    <template v-else>
                        {{ fallback(row.value) }}
                        <span v-if="row.lunarBadge" class="badge badge-primary ms-5">{{ row.lunarLabel }}</span>
                    </template>
                </div>
            </div>
        </div>
    </template>

    <template v-if="emplymRows.length">
        <div class="separator my-2"></div>
        <div class="card-body">
            <div v-for="row in emplymRows" :key="'e-' + row.label" class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ row.label }}</div>
                </div>
                <div class="col-xl-9 col-8 col-form-label">
                    <template v-if="row.asTextarea">
                        <div class="div-textarea h-auto min-h-125px">{{ fallback(row.value) }}</div>
                    </template>
                    <template v-else>
                        {{ fallback(row.value) }}
                    </template>
                </div>
            </div>
        </div>
    </template>
    `,
};
