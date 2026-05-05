/**
 * 프로필/인사 섹션 토글 버튼 공통 컴포넌트
 *
 * UserFormApp, UserSignupApp 공용.
 *
 * @author nichefish
 */
export default {
    name: "UserProfileEmplymToggleButtons",
    props: {
        hasProfile: { type: Boolean, required: true },
        hasEmplym: { type: Boolean, required: true },
        addProfileLabel: { type: String, required: true },
        removeProfileLabel: { type: String, required: true },
        addEmplymLabel: { type: String, required: true },
        removeEmplymLabel: { type: String, required: true },
        emplymNoticeLabel: { type: String, required: true },
    },
    emits: ["toggle-profile", "toggle-emplym"],
    template: `
    <div class="d-flex gap-3 flex-wrap align-items-start">
        <button type="button" class="btn btn-sm"
                :class="hasProfile ? 'btn-danger' : 'btn-primary'"
                @click="$emit('toggle-profile')">
            {{ hasProfile ? removeProfileLabel : addProfileLabel }}
        </button>
        <div>
            <button type="button" class="btn btn-sm mb-1"
                    :class="hasEmplym ? 'btn-danger' : 'btn-primary'"
                    @click="$emit('toggle-emplym')">
                {{ hasEmplym ? removeEmplymLabel : addEmplymLabel }}
            </button>
            <div class="text-noti">{{ emplymNoticeLabel }}</div>
        </div>
    </div>
    `,
};
