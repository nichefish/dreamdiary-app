import EmailDomainSelect from "./EmailDomainSelect.js";

/**
 * 사용자 프로필/인사 입력 섹션 공통 컴포넌트
 *
 * UserFormPanel, UserSignupApp 공용.
 *
 * @author nichefish
 */
export default {
    name: "UserProfileEmplymFormSection",
    components: {
        EmailDomainSelect,
    },
    props: {
        form: { type: Object, required: true },
        labels: { type: Object, required: false, default: () => ({}) },
        t: { type: Function, required: false, default: undefined },
        showProfile: { type: Boolean, required: true },
        showEmplym: { type: Boolean, required: true },
        cmpyList: { type: Array, required: true },
        teamList: { type: Array, required: true },
        emplymList: { type: Array, required: true },
        rankList: { type: Array, required: true },
        customInputLabel: { type: String, required: true },
        staffRankCd: { type: String, required: false, default: "STAFF" },
        mode: { type: String, required: false, default: "signup" }, // signup | userForm
    },
    computed: {
        showApntcYn(): boolean {
            const rankCd = (this.form as any)?.emplym?.rankCd || "";
            return rankCd === this.staffRankCd;
        },
    },
    methods: {
        n(fieldName: string): string | undefined {
            return this.mode === "userForm" ? fieldName : undefined;
        },
        tx(key: string, fallback: string): string {
            const tr = this.t as ((k: string) => string) | undefined;
            if (typeof tr === "function")
                return tr(key);
            return fallback;
        },
    },
    template: `
    <div id="user_profile_div" v-if="showProfile">
        <div class="separator my-8"></div>
        <div class="row mb-4">
            <div class="col-xl-2">
                <label for="proflCn" class="cursor-help fw-bold">{{ tx("txt.user.profile.profile", labels.profileProfile || "") }}</label>
            </div>
            <div class="col-xl-9">
                <textarea :name="n('profile.proflCn')" id="proflCn" v-model="form.profile.proflCn"
                          maxlength="500" wrap="hard" class="form-control form-control-solid h-100px"></textarea>
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2"><label>{{ tx("txt.user.profile.birth-date", labels.profileBirthDate || "") }}</label></div>
            <div class="col-xl-2">
                <input :name="n('profile.brthdy')" id="brthdy" v-model="form.profile.brthdy"
                       type="text" readonly class="form-control form-control-solid ps-12" autocomplete="off" />
            </div>
            <div class="col-xl-2">
                <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                    <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">{{ tx("txt.user.profile.lunar-yn", labels.profileLunarYn || "") }}</label>
                    <input :name="n('profile.lunarYn')" id="lunarYn" v-model="form.profile.lunarYn"
                           type="checkbox" class="form-check-input cursor-pointer ms-3" value="Y" />
                    <label class="form-check-label fw-bold ms-3" for="lunarYn">
                        {{ form.profile.lunarYn ? tx("txt.user.profile.lunar", labels.profileLunar || "") : tx("txt.user.profile.solar", labels.profileSolar || "") }}
                    </label>
                </div>
            </div>
        </div>
    </div>

    <div id="user_emplym_div" v-if="showEmplym">
        <div class="separator my-8"></div>
        <div class="row mb-4">
            <div class="col-xl-2"><div class="fs-6 fw-bold col-form-label required"><label for="userNm">{{ tx("txt.user.emplym.user-name", labels.emplymUserName || "") }}</label></div></div>
            <div class="col-xl-2">
                <input :name="n('emplym.userNm')" id="userNm" v-model.trim="form.emplym.userNm"
                       maxlength="20" type="text" class="form-control form-control-solid required" />
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2"><label class="required">{{ tx("txt.user.emplym.email", labels.emplymEmail || "") }}</label></div>
            <div class="col-lg-2 col-4">
                <input :name="n('emplym.emplymEmailId')" id="emplymEmailId" v-model.trim="form.emplym.emplymEmailId"
                       maxlength="20" type="text" class="form-control form-control-solid required" />
            </div>
            <div class="col-xl-2 col-1 d-flex-center fw-bold">@</div>
            <div class="col-lg-2 col-5">
                <input :name="n('emplym.emplymEmailDomain')" id="emplymEmailDomain" v-model.trim="form.emplym.emplymEmailDomain"
                       maxlength="20" type="text" class="form-control form-control-solid required" />
            </div>
            <div class="col-xl-2 col-1">
                <EmailDomainSelect
                    id="emplymEmailDomainSelect"
                    :name="n('emplymEmailDomainSelect') || 'emplymEmailDomainSelect'"
                    :options="['gmail.com','naver.com','kakao.com']"
                    :custom-input-label="customInputLabel"
                    :model-value="form.emplym.emplymEmailDomainSelect"
                    @update:modelValue="form.emplym.emplymEmailDomainSelect = $event"
                />
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2"><label class="required">{{ tx("txt.user.emplym.phone-number", labels.emplymPhoneNumber || "") }}</label></div>
            <div class="col-xl-2">
                <input :name="n('emplym.emplymPhoneNumber')" id="emplymPhoneNumber" v-model.trim="form.emplym.emplymPhoneNumber"
                       maxlength="20" type="text" class="form-control form-control-solid required" />
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 fs-6 fw-bold"><label>{{ tx("txt.user.emplym.affiliation", labels.emplymAffiliation || "") }}</label></div>
            <div class="col-xl-2 col-6">
                <select :name="n('emplym.cmpyCd')" id="cmpyCd" v-model="form.emplym.cmpyCd" class="form-select">
                    <option value="">{{ tx("txt.user.emplym.company-option", labels.emplymCompanyOption || "") }}</option>
                    <option v-for="o in cmpyList" :key="'c'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
            </div>
            <div class="col-xl-1 col-6">
                <select :name="n('emplym.teamCd')" id="teamCd" v-model="form.emplym.teamCd" class="form-select">
                    <option value="">{{ tx("txt.user.emplym.team-option", labels.emplymTeamOption || "") }}</option>
                    <option v-for="o in teamList" :key="'t'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
            </div>
            <div class="col-xl-2">
                <select :name="n('emplym.emplymCd')" id="emplymCd" v-model="form.emplym.emplymCd" class="form-select">
                    <option value="">{{ tx("txt.user.emplym.employment-type-option", labels.emplymEmploymentTypeOption || "") }}</option>
                    <option v-for="o in emplymList" :key="'e'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 fs-6 fw-bold"><label>{{ tx("txt.user.emplym.rank", labels.emplymRank || "") }}</label></div>
            <div class="col-xl-2">
                <select :name="n('emplym.rankCd')" id="rankCd" v-model="form.emplym.rankCd" class="form-select">
                    <option value="">{{ tx("txt.user.emplym.select-option", labels.emplymSelectOption || "") }}</option>
                    <option v-for="o in rankList" :key="'r'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
            </div>
            <div v-if="showApntcYn" class="col-xl-2" id="apntcYnDiv">
                <div class="form-check form-switch mt-2">
                    <span>{{ tx("txt.user.emplym.probation", labels.emplymProbation || "") }}</span>
                    <input :name="n('emplym.apntcYn')" id="apntcYn" v-model="form.emplym.apntcYn"
                           type="checkbox" class="form-check-input cursor-pointer ms-3" value="Y" />
                    <label class="form-check-label fw-bold ms-3" for="apntcYn">{{ form.emplym.apntcYn ? tx("txt.user.emplym.probation.active", labels.emplymProbationActive || "") : tx("txt.user.emplym.not-applicable", labels.emplymNotApplicable || "") }}</label>
                </div>
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 fs-6 fw-bold required"><label>{{ tx("txt.user.emplym.join-date", labels.emplymJoinDate || "") }}</label></div>
            <div class="col-xl-2">
                <input :name="n('emplym.ecnyDt')" id="ecnyDt" v-model="form.emplym.ecnyDt" type="text" readonly class="form-control ps-12 required" />
                <div class="text-noti form-text">{{ tx("txt.user.emplym.join-date-guide", labels.emplymJoinDateGuide || "") }}</div>
            </div>
            <div class="col-xl-2">
                <div class="form-check form-switch">
                    <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">{{ tx("txt.user.emplym.retired-yn", labels.emplymRetiredYn || "") }}</label>
                    <input :name="n('emplym.retireYn')" id="retireYn" v-model="form.emplym.retireYn"
                           type="checkbox" class="form-check-input cursor-pointer ms-3" value="Y" />
                    <label class="form-check-label fw-bold ms-3" for="retireYn">{{ form.emplym.retireYn ? tx("txt.user.emplym.retired", labels.emplymRetired || "") : tx("txt.user.emplym.not-applicable", labels.emplymNotApplicable || "") }}</label>
                </div>
            </div>
            <template v-if="form.emplym.retireYn" class="retireDtDiv">
                <div class="col-xl-1 fw-bold retireDtDiv">{{ tx("txt.user.emplym.retired-date", labels.emplymRetiredDate || "") }}</div>
                <div class="col-xl-2 retireDtDiv"><input :name="n('emplym.retireDt')" id="retireDt" v-model="form.emplym.retireDt" type="text" readonly class="form-control ps-12" autocomplete="off" /></div>
            </template>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 fw-bold">{{ tx("txt.user.emplym.payroll-account", labels.emplymPayrollAccount || "") }}</div>
            <div class="col-xl-2"><input :name="n('emplym.acntBank')" id="acntBank" v-model.trim="form.emplym.acntBank" maxlength="40" type="text" class="form-control form-control-solid" /></div>
            <div class="col-xl-2"><input :name="n('emplym.acntNo')" id="acntNo" v-model.trim="form.emplym.acntNo" maxlength="40" type="text" class="form-control form-control-solid" /></div>
        </div>
    </div>
    `,
};
