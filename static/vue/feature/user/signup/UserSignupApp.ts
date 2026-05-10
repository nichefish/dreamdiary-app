/**
 * 계정 신청 화면 Vue 앱
 *
 * @author nichefish
 */
declare const Vue: { createApp: (opts: unknown) => { mount: (sel: string) => unknown } };
import userSignupDataService from "./services/userSignupDataService.js";
import type { CodeRow, UserSignupBootstrap } from "./services/userSignupDataService.js";
import userSignupActionService, {
    dupCheckEmail,
    dupCheckUsername,
    isStaffRank,
    listOf,
} from "./services/userSignupActionService.js";
import { initAllowedIpTagify } from "./services/userSignupDomHooks.js";
import type { UserSignupFormState } from "./types.js";
import { initEmplymFormPlugins, initProfileBirthDatepicker } from "../shared/profileEmplymShared.js";
import UserProfileEmplymToggleButtons from "../shared/components/UserProfileEmplymToggleButtons.js";
import { COMMON_EMAIL_DOMAIN_OPTIONS } from "../shared/emailDomainShared.js";
import EmailDomainSelect from "../shared/components/EmailDomainSelect.js";
import UserProfileEmplymFormSection from "../shared/components/UserProfileEmplymFormSection.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

const CODE_JOB_TITLE_CD = "JOB_TITLE_CD";
const SEL_ROOT = "#user_signup_vue_root";

let allowedIpTagifyDone = false;
let profileDpDone = false;
let emplymDpDone = false;
const i18n = createScopedI18n();

function resolvePageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;

    const loc = w?.Model?.locale;

    if (loc)
        return loc;

    const rootLang = typeof document !== "undefined" ? document.documentElement?.lang : "";
    return (rootLang || "ko").replace(/_/g, "-");
}

function emptyForm(authKey: string, staffRankCd: string, defaults: UserSignupBootstrap["userDefaults"]): UserSignupFormState {
    return {
        id: defaults.id !== null ? String(defaults.id) : "",
        fileGroupId: defaults.fileGroupId,
        username: defaults.username || "",
        password: "",
        passwordCf: "",
        nickname: defaults.nickname || "",
        emailId: defaults.emailId || "",
        emailDomain: defaults.emailDomain || "",
        emailDomainSelect: defaults.emailDomain || "",
        phoneNumber: defaults.phoneNumber || "",
        useAllowedIpYn: false,
        usernameDupPassed: "N",
        emailDupPassed: "N",
        content: defaults.content || "",
        usernameMsg: "",
        emailMsg: "",
        usernameMsgIsError: false,
        emailMsgIsError: false,

        idDupBtnDisabled: false,
        emailDupBtnDisabled: false,
        authUserRoleKey: authKey,
        staffRankCd,
        showProfile: false,
        showEmplym: false,
        profile: {
            proflCn: "",
            brthdy: "",
            lunarYn: false,
        },
        emplym: {
            userNm: "",
            emplymEmailId: "",
            emplymEmailDomain: "",
            emplymEmailDomainSelect: "",
            emplymPhoneNumber: "",
            cmpyCd: "",
            teamCd: "",
            emplymCd: "",
            rankCd: "",
            apntcYn: false,

            retireYn: false,

            ecnyDt: "",
            retireDt: "",
            acntBank: "",
            acntNo: "",
        },
    };

}

/**
 * 레거시 cF 헬퍼(tagify/date/phoneNumber) 초기화. Vue 가 DOM 을 채운 뒤 호출해야 한다.
 */
function bindLegacyDomHelpers(inst: VueInst): void {
    const $fn = typeof globalThis !== "undefined" ? (globalThis as any).$ : undefined;

    inst.$nextTick(function(): void {
        const hasRoot = !!(typeof document !== "undefined" && document.querySelector(`${SEL_ROOT}`));
        if (!hasRoot || !$fn?.fn || typeof cF === "undefined")
            return;

        if (inst.form.useAllowedIpYn && document.querySelector(`${SEL_ROOT} #allowedIpListStr`) && !allowedIpTagifyDone) {
            /** 변경 전 후: 접속 허용 IP 활성 후에 한 번만 tagify를 붙여야 중복 초기화로 인한 DOM 오류 가 적다. 레거시는 페이지 로드시 항상 시도 했다 */
            initAllowedIpTagify(`${SEL_ROOT} #allowedIpListStr`);
            allowedIpTagifyDone = true;
        }

        if (inst.form.showProfile && document.querySelector(`${SEL_ROOT} #brthdy`) && !profileDpDone) {

            profileDpDone = true;

            /* eslint-disable @typescript-eslint/no-unsafe-call @typescript-eslint/no-unsafe-member-access */
            try {
                initProfileBirthDatepicker(`${SEL_ROOT} #brthdy`);
            }

            catch (e) {

                console.error("[UserSignupApp] brthdy datepicker init 실패.", e);

            }

        }

        if (inst.form.showEmplym && document.querySelector(`${SEL_ROOT} #emplymPhoneNumber`) && !emplymDpDone) {

            emplymDpDone = true;

            try {
                initEmplymFormPlugins({
                    phoneSelector: `${SEL_ROOT} #emplymPhoneNumber`,
                    emailDomainSelectSelector: `${SEL_ROOT} #emplymEmailDomainSelect`,
                    emailDomainInputSelector: `${SEL_ROOT} #emplymEmailDomain`,
                    joinDateSelector: `${SEL_ROOT} #ecnyDt`,
                    retireDateSelector: inst.form.emplym.retireYn ? `${SEL_ROOT} #retireDt` : undefined,
                    bindNamespace: "userSignupVue",
                });
            }

            catch (e) {

                console.error("[UserSignupApp] emplym datepicker/phone init 실패.", e);

            }

        }

    });

}

type VueInst = {
    $nextTick: (fn: () => void) => void;
    form: UserSignupFormState;
};

const UserSignupRoot = {
    name: "UserSignupRoot",
    components: {
        UserProfileEmplymToggleButtons,
        EmailDomainSelect,
        UserProfileEmplymFormSection,
    },
    data(): {
        codeLists: Record<string, CodeRow[]>;
        siteAcs: UserSignupBootstrap["siteAcs"];
        staffRankCd: string;
        form: UserSignupFormState;
    } {
        const bt = userSignupDataService.parse();
        const authKey = bt?.authUserRoleKey || "";
        const staffRank = bt?.staffRankCd || "";
        const lists = bt?.codeLists || {};
        const site = bt?.siteAcs || {};
        const defaults = bt?.userDefaults || {
            id: null,
            fileGroupId: "",
            nickname: "",
            username: "",
            emailId: "",
            emailDomain: "",
            phoneNumber: "",
            content: "",
        };

        return {
            codeLists: lists,
            siteAcs: site,
            staffRankCd: staffRank,
            form: emptyForm(authKey, staffRank, defaults),
        };

    },
    computed: {
        cmpyList(): CodeRow[] {
            return listOf(this.codeLists, "CMPY_CD");
        },
        teamList(): CodeRow[] {
            return listOf(this.codeLists, "TEAM_CD");
        },
        emplymCdList(): CodeRow[] {
            return listOf(this.codeLists, "EMPLYM_CD");
        },
        rankList(): CodeRow[] {
            return listOf(this.codeLists, CODE_JOB_TITLE_CD);
        },
        emailDomainOptions(): string[] {
            return [...COMMON_EMAIL_DOMAIN_OPTIONS];
        },
    },
    watch: {
        "form.useAllowedIpYn"(): void {
            bindLegacyDomHelpers(this as unknown as VueInst);
        },
        "form.showProfile"(on: boolean): void {
            if (!on) {
                profileDpDone = false;
                return;
            }

            profileDpDone = false;
            bindLegacyDomHelpers(this as unknown as VueInst);
        },
        "form.showEmplym"(on: boolean): void {
            if (!on) {
                emplymDpDone = false;
                return;
            }

            emplymDpDone = false;
            bindLegacyDomHelpers(this as unknown as VueInst);
        },
        "form.emplym.rankCd"(v: string): void {
            if (!isStaffRank(v, this.staffRankCd))
                this.form.emplym.apntcYn = false;
        },
        "form.emplym.retireYn"(on: boolean): void {
            if (!on)
                this.form.emplym.retireDt = "";
            const self = this as unknown as VueInst;
            self.$nextTick(function(): void {
                const $fn = (globalThis as any).$;
                if (!on || !$fn?.fn)
                    return;
                try {
                    initEmplymFormPlugins({
                        phoneSelector: `${SEL_ROOT} #emplymPhoneNumber`,
                        emailDomainSelectSelector: `${SEL_ROOT} #emplymEmailDomainSelect`,
                        emailDomainInputSelector: `${SEL_ROOT} #emplymEmailDomain`,
                        joinDateSelector: `${SEL_ROOT} #ecnyDt`,
                        retireDateSelector: `${SEL_ROOT} #retireDt`,
                        bindNamespace: "userSignupVue",
                    });
                }
                catch (e) {
                    console.error("[UserSignupApp] retireDt datepicker init 실패.", e);
                }
            });
        },
        "form.emailDomainSelect"(v: string): void {
            if (v)
                this.form.emailDomain = v;
        },
        "form.emplym.emplymEmailDomainSelect"(v: string): void {
            if (v)
                this.form.emplym.emplymEmailDomain = v;
        },
    },
    methods: {
        t(key: string): string {
            return i18n.t(key);
        },
        breadcrumbHome(): void {
            userSignupActionService.confirmNavigateAway(this.t, (): void =>
                userSignupActionService.goLoginForm());
        },
        onUsernameDup(): void {
            dupCheckUsername(this.form.username, this.form, this.t);
        },
        onEmailDup(): void {
            dupCheckEmail(this.form.emailId, this.form.emailDomain, this.form, this.t);
        },
        resetUsernameDup(): void {
            this.form.usernameMsg = "";
            this.form.usernameDupPassed = "N";
            this.form.idDupBtnDisabled = false;
        },
        resetEmailDup(): void {
            this.form.emailMsg = "";
            this.form.emailDupPassed = "N";
            this.form.emailDupBtnDisabled = false;
        },
        toggleProfile(): void {
            this.form.showProfile = !this.form.showProfile;
        },
        toggleEmplym(): void {
            this.form.showEmplym = !this.form.showEmplym;
        },
        submit(): void {
            userSignupActionService.submitMultipart(this.form, this.t);
        },
    },
    mounted(): void {

        try {
            cF.validate.phoneNumber(`${SEL_ROOT} #phoneNumber`);
        }
        catch (e) {
            console.error("[UserSignupApp] phoneNumber mask init 실패.", e);

        }

        bindLegacyDomHelpers(this as unknown as VueInst);
    },

    template: `
<div id="user_signup_vue_root" class="card post w-75 mx-auto my-auto">
    <div class="card-header min-h-auto mb-10">
      <div class="row mb-8 p-0">
        <div class="page-title d-flex align-items-center flex-wrap me-3 mb-5 mb-lg-0">
          <h1 class="d-flex align-items-center text-dark fw-bolder fs-3 my-1">
            <ol class="breadcrumb text-muted fs-6 fw-semibold">
              <li class="breadcrumb-item">
                <a href="javascript:void(0);" @click.prevent="breadcrumbHome()" data-bs-toggle="tooltip"
                  data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.return-to-main')">
                  <i class="bi bi-house-fill fs-4 me-1 text-primary"></i>
                </a>
              </li>
              <li class="breadcrumb-item text-muted">{{ siteAcs.upperMenuNm || '' }}</li>
              <li class="breadcrumb-item text-muted">{{ siteAcs.menuName || '' }}</li>
            </ol>
            <span class="h-20px border-gray-200 border-start ms-3 mx-2"></span>
            <small class="text-muted fs-7 fw-bold my-1 ms-1">{{ siteAcs.pageName || '' }}</small>
          </h1>
        </div>
      </div>
    </div>

    <div class="card-body">
      <div class="card-body">
        <form id="userSignupFormVue" class="form" method="post" @submit.prevent="submit">
          <!-- 서버 검증과 동등한 필드명으로 FormData 를 userSignupActionService 에서 직접 구성한다. -->
          <input type="hidden" name="__vueRendered" id="__vueRendered" value="1" />

          <div class="row mb-4">
            <div class="col-xl-2">
              <div class="fw-bold col-form-label text-lg-center text-sm-end required"><label for="username">{{ t('txt.user.form.username') }}</label></div>
            </div>
            <div class="col-xl-2 col-8">
              <input id="username" v-model.trim="form.username" type="text"
                class="form-control form-control-solid required no-space" :placeholder="t('txt.user.form.username')" maxlength="16" autocomplete="username" @input="resetUsernameDup" />
              <div class="form-text text-noti">{{ t('txt.req.username') }}</div>
              <div class="small" :class="form.usernameMsgIsError ? 'text-danger' : 'text-success'" style="white-space: pre-wrap">{{ form.usernameMsg }}</div>
            </div>
            <div class="col-xl-2 col-4">
              <button type="button" id="idDupChckBtn" class="btn btn-sm blink required"
                :disabled="form.idDupBtnDisabled" :class="form.idDupBtnDisabled ? 'btn-success' : 'btn-secondary'"
                @click="onUsernameDup" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                :title="t('bs.tooltip.user.form.username-dup-check')">
                <span class="hidden-table">{{ t('txt.user.form.username') }} </span>{{ t('txt.user.form.dup-check') }}
              </button>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2">
              <div class="fw-bold col-form-label text-lg-center text-sm-end required"><label for="password">{{ t('txt.user.form.password') }}</label></div>
            </div>
            <div class="col-xl-2">
              <input id="password" v-model="form.password" type="password" class="form-control form-control-solid required no-space"
                maxlength="15" autocomplete="new-password" :placeholder="t('txt.user.form.password')" />
              <div class="form-text text-noti">{{ t('txt.req.password') }}</div>
            </div>
            <div class="col-xl-2">
              <div class="fw-bold col-form-label text-sm-start text-lg-end required"><label for="passwordCf">{{ t('txt.user.form.password-confirm') }}</label></div>
            </div>
            <div class="col-xl-2">
              <input id="passwordCf" v-model="form.passwordCf" type="password" class="form-control form-control-solid required no-space"
                maxlength="15" autocomplete="new-password" :placeholder="t('txt.user.form.password-confirm')" />
              <div class="form-text text-noti">{{ t('txt.req.password.cf') }}</div>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2">
              <div class="fw-bold col-form-label text-lg-center text-sm-end">
                <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.user.form.role-request')">
                  {{ t('txt.user.form.role') }} <i class="bi bi-question-circle"></i>
                </label>
              </div>
            </div>
            <div class="col-xl-6 col-form-label">
              <i class="bi bi-people-fill me-1 blink-slow"></i>
              <span class="fw-bold text-gray-600">{{ t('txt.user.form.user-role-label') }}</span>
              <span class="ms-2 text-muted">{{ form.authUserRoleKey }}</span>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2">
              <div class="fw-bold col-form-label text-lg-center text-sm-start required" data-bs-toggle="tooltip" data-bs-placement="top"
                data-bs-dismiss="click" :title="t('bs.tooltip.user.form.nickname-request')">
                <label for="nickname" class="cursor-help">{{ t('txt.user.form.nickname') }} <i class="bi bi-question-circle"></i></label>
              </div>
            </div>
            <div class="col-xl-2">
              <input id="nickname" v-model.trim="form.nickname" maxlength="20" type="text" class="form-control form-control-solid required"
                :placeholder="t('txt.user.form.nickname')" autocomplete="nickname" />
              <div class="form-text text-noti">{{ t('txt.req.nicknm') }}</div>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2">
              <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                <label for="emailId" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top"
                  data-bs-dismiss="click" :title="t('bs.tooltip.user.form.email')">E-mail <i class="bi bi-question-circle"></i></label>
              </div>
            </div>
            <div class="col-lg-2 col-4">
              <input id="emailId" v-model.trim="form.emailId" maxlength="20" type="text" class="form-control form-control-solid required no-space"
                @input="resetEmailDup" @change="resetEmailDup" />
              <div class="small" :class="form.emailMsgIsError ? 'text-danger' : 'text-success'" style="white-space: pre-wrap">{{ form.emailMsg }}</div>
            </div>
            <div class="col-xl-2 col-1 d-flex-center fw-bold text-gray-700" style="width:2%;">@</div>
            <div class="col-lg-2 col-5 vertical-center">
              <input id="emailDomain" v-model.trim="form.emailDomain" maxlength="20" type="text" class="form-control form-control-solid required no-space"
                @input="resetEmailDup" @change="resetEmailDup" />
            </div>
            <div class="col-xl-2 col-1">
              <EmailDomainSelect
                id="emailDomainSelect"
                name="emailDomainSelect"
                :options="emailDomainOptions"
                :custom-input-label="t('txt.user.form.custom-input')"
                :model-value="form.emailDomainSelect"
                @update:modelValue="form.emailDomainSelect = $event"
                @change="resetEmailDup"
              />
            </div>
            <div class="col-xl-2 col-4">
              <button id="emailDupChckBtn" type="button" class="btn btn-sm btn-secondary blink required"
                :class="form.emailDupBtnDisabled ? 'btn-success' : 'btn-secondary'" :disabled="form.emailDupBtnDisabled" @click="onEmailDup">
                EMAIL {{ t('txt.user.form.dup-check') }}
              </button>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2"><div class="fw-bold col-form-label text-sm-start text-lg-center"><label for="phoneNumber">{{ t('txt.user.form.phone-number') }}</label></div></div>
            <div class="col-xl-2">
              <input id="phoneNumber" v-model.trim="form.phoneNumber" maxlength="20" type="text" class="form-control form-control-solid" />
              <div class="form-text text-noti">{{ t('txt.req.phoneNumber') }}</div>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="useAllowedIpYn" class="cursor-help fw-bold col-form-label text-lg-center text-sm-start"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                :title="t('bs.tooltip.user.form.allowed-ip')">{{ t('txt.user.form.allowed-ip') }} <i class="bi bi-question-circle"></i></label>
            </div>
            <div class="col-xl-10">
              <div class="form-check form-switch form-check-custom form-check-solid mt-2">
                <input id="useAllowedIpYn" v-model="form.useAllowedIpYn" type="checkbox" class="form-check-input cursor-pointer" />
                <label class="form-check-label fw-bold ms-3 cursor-pointer" for="useAllowedIpYn" style="color: gray;">
                  {{ form.useAllowedIpYn ? t('txt.status.use') : t('txt.status.unuse') }}
                </label>
              </div>
              <div v-show="form.useAllowedIpYn" class="mt-2 mb-0">
                <input id="allowedIpListStr" type="text" class="form-control form-control-solid no-space" maxlength="500" autocomplete="off" />
                <div class="form-text text-noti">{{ t('txt.req.acsip') }}</div>
              </div>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="content" class="cursor-help fw-bold col-form-label text-sm-start text-lg-center"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                :title="t('bs.tooltip.user.form.account-description-simple')">{{ t('txt.user.form.account-description') }} <i class="bi bi-question-circle"></i></label>
            </div>
            <div class="col-xl-9">
              <textarea id="content" v-model="form.content" class="form-control form-control-solid h-100px" wrap="hard" maxlength="4000"></textarea>
            </div>
          </div>

          <UserProfileEmplymFormSection
            :form="form"
            :t="t"
            :show-profile="form.showProfile"
            :show-emplym="form.showEmplym"
            :cmpy-list="cmpyList"
            :team-list="teamList"
            :emplym-list="emplymCdList"
            :rank-list="rankList"
            :custom-input-label="t('txt.user.form.custom-input')"
            :staff-rank-cd="staffRankCd"
            mode="signup"
          />
        </form>
      </div>
    </div>

    <div class="card-footer">
      <div class="d-flex justify-content-between">
        <UserProfileEmplymToggleButtons
            :has-profile="form.showProfile"
            :has-emplym="form.showEmplym"
            :add-profile-label="t('txt.user.form.add-profile')"
            :remove-profile-label="t('txt.user.signup.profile.remove')"
            :add-emplym-label="t('txt.user.form.add-employment')"
            :remove-emplym-label="t('txt.user.signup.employment.remove')"
            :emplym-notice-label="t('txt.req.user.emplym')"
            @toggle-profile="toggleProfile"
            @toggle-emplym="toggleEmplym"
        />
        <div class="gap-2 d-flex flex-shrink-0">
          <button type="button" class="btn btn-sm btn-primary" @click.prevent="submit"><i class="bi bi-pencil-square"></i>{{ t('txt.user.form.request-new-account') }}</button>
          <button type="button" class="btn btn-sm btn-light" @click.prevent="breadcrumbHome"><i class="bi bi-backspace"></i>{{ t('txt.user.form.go-back') }}</button>
        </div>
      </div>
    </div>
</div>
`,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading")
        document.addEventListener("DOMContentLoaded", fn);
    else
        fn();

}

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolvePageLocale());

    const mount = document.getElementById("user_signup_app");
    if (!mount) {

        console.error("[UserSignupApp] mount root #user_signup_app 미존재");

        return;
    }


    (Vue as any).createApp(UserSignupRoot).mount("#user_signup_app");

});
