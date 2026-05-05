/**
 * 계정 신청 화면 Vue 앱
 *
 * @author nichefish
 */
declare const Vue: { createApp: (opts: unknown) => { mount: (sel: string) => unknown } };
import userSignupDataService from "./services/userSignupDataService.js";
import type { CodeRow, UserSignupBootstrap } from "./services/userSignupDataService.js";
import userSignupI18nService from "./services/userSignupI18nService.js";
import userSignupActionService, {
    dupCheckEmail,
    dupCheckUsername,
    isStaffRank,
    listOf,
} from "./services/userSignupActionService.js";
import { initAllowedIpTagify } from "./services/userSignupDomHooks.js";
import type { UserSignupFormState } from "./types.js";

const CODE_JOB_TITLE_CD = "JOB_TITLE_CD";
const SEL_ROOT = "#user_signup_vue_root";

let allowedIpTagifyDone = false;
let profileDpDone = false;
let emplymDpDone = false;

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
                cF.datepicker.singleDatePicker(`${SEL_ROOT} #brthdy`, "yyyy-MM-DD", $fn(`${SEL_ROOT} #brthdy`).val());
            }

            catch (e) {

                console.error("[UserSignupApp] brthdy datepicker init 실패.", e);

            }

        }

        if (inst.form.showEmplym && document.querySelector(`${SEL_ROOT} #emplymPhoneNumber`) && !emplymDpDone) {

            emplymDpDone = true;

            try {
                cF.validate.phoneNumber(`${SEL_ROOT} #emplymPhoneNumber`);
                $fn(`${SEL_ROOT} #emplymEmailDomainSelect`).off("change.userSignupVue").on("change.userSignupVue", function(this: HTMLElement): void {
                    $fn(`${SEL_ROOT} #emplymEmailDomain`).val($fn(this).val());
                });

                cF.datepicker.singleDatePicker(`${SEL_ROOT} #ecnyDt`, "yyyy-MM-DD", $fn(`${SEL_ROOT} #ecnyDt`).val());

                if (inst.form.emplym.retireYn)
                    cF.datepicker.singleDatePicker(`${SEL_ROOT} #retireDt`, "yyyy-MM-DD", $fn(`${SEL_ROOT} #retireDt`).val());
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
        showApntcYn(): boolean {
            return isStaffRank(this.form.emplym.rankCd, this.staffRankCd);
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
                    cF.datepicker.singleDatePicker(`${SEL_ROOT} #retireDt`, "yyyy-MM-DD", $fn(`${SEL_ROOT} #retireDt`).val());
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
            return userSignupI18nService.t(key);
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
              <select id="emailDomainSelect" v-model="form.emailDomainSelect" class="form-select form-select-solid" @change="resetEmailDup">
                <option value="">{{ t('txt.user.form.custom-input') }}</option>
                <option value="gmail.com">gmail.com</option>
                <option value="naver.com">naver.com</option>
                <option value="kakao.com">kakao.com</option>
              </select>
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

          <div v-if="form.showProfile" id="user_profile_div">
            <div class="separator my-8"></div>
            <div class="row mb-4">
              <div class="col-xl-2">
                <label for="proflCn" class="cursor-help fw-bold">{{ t('txt.user.profile.profile') }}</label>
              </div>
              <div class="col-xl-9">
                <!-- 변경 전: Handlebars 에서 같은 name 두 textarea 존재. 변경 후: profile.proflCn 단일 매핑. -->
                <textarea id="proflCn" v-model="form.profile.proflCn" maxlength="500" wrap="hard" class="form-control form-control-solid h-100px"></textarea>
              </div>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2"><label>{{ t('txt.user.profile.birth-date') }}</label></div>
              <div class="col-xl-2">
                <input id="brthdy" v-model="form.profile.brthdy" type="text" readonly class="form-control form-control-solid ps-12" autocomplete="off" />
              </div>
              <div class="col-xl-2">
                <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                  <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.user.profile.lunar')">{{ t('txt.user.profile.lunar-yn') }}</label>
                  <input id="lunarYn" v-model="form.profile.lunarYn" type="checkbox" class="form-check-input cursor-pointer ms-3" />
                  <label class="form-check-label fw-bold ms-3" for="lunarYn">
                    {{ form.profile.lunarYn ? t('txt.user.profile.lunar') : t('txt.user.profile.solar') }}
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div v-if="form.showEmplym" id="user_emplym_div">
            <div class="separator my-8"></div>
            <div class="row mb-4">
              <div class="col-xl-2"><div class="fs-6 fw-bold col-form-label required"><label for="userNm">{{ t('txt.user.emplym.user-name') }}</label></div></div>
              <div class="col-xl-2">
                <input id="userNm" v-model.trim="form.emplym.userNm" maxlength="20" type="text" class="form-control form-control-solid required" />
              </div>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2"><label class="required">{{ t('txt.user.emplym.email') }}</label></div>
              <div class="col-lg-2 col-4">
                <input id="emplymEmailId" v-model.trim="form.emplym.emplymEmailId" maxlength="20" type="text" class="form-control form-control-solid required" />
              </div>
              <div class="col-xl-2 col-1 d-flex-center fw-bold">@</div>
              <div class="col-lg-2 col-5">
                <input id="emplymEmailDomain" v-model.trim="form.emplym.emplymEmailDomain" maxlength="20" type="text" class="form-control form-control-solid required" />
              </div>
              <div class="col-xl-2 col-1">
                <select id="emplymEmailDomainSelect" v-model="form.emplym.emplymEmailDomainSelect" class="form-select form-select-solid">
                  <option value="">{{ t('txt.user.form.custom-input') }}</option>
                  <option value="gmail.com">gmail.com</option>
                  <option value="naver.com">naver.com</option>
                  <option value="kakao.com">kakao.com</option>
                </select>
              </div>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2"><label class="required">{{ t('txt.user.emplym.phone-number') }}</label></div>
              <div class="col-xl-2">
                <input id="emplymPhoneNumber" v-model.trim="form.emplym.emplymPhoneNumber" maxlength="20" type="text" class="form-control form-control-solid required" />
              </div>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2 fs-6 fw-bold"><label>{{ t('txt.user.emplym.affiliation') }}</label></div>
              <div class="col-xl-2 col-6">
                <select id="cmpyCd" v-model="form.emplym.cmpyCd" class="form-select">
                  <option value="">{{ t('txt.user.emplym.company-option') }}</option>
                  <option v-for="o in cmpyList" :key="'c'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
              </div>
              <div class="col-xl-1 col-6">
                <select id="teamCd" v-model="form.emplym.teamCd" class="form-select">
                  <option value="">{{ t('txt.user.emplym.team-option') }}</option>
                  <option v-for="o in teamList" :key="'t'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
              </div>
              <div class="col-xl-2">
                <select id="emplymCd" v-model="form.emplym.emplymCd" class="form-select">
                  <option value="">{{ t('txt.user.emplym.employment-type-option') }}</option>
                  <option v-for="o in emplymCdList" :key="'e'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
              </div>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2 fs-6 fw-bold"><label>{{ t('txt.user.emplym.rank') }}</label></div>
              <div class="col-xl-2">
                <select id="rankCd" v-model="form.emplym.rankCd" class="form-select">
                  <option value="">{{ t('txt.user.emplym.select-option') }}</option>
                  <option v-for="o in rankList" :key="'r'+o.code" :value="o.code">{{ o.codeName }}</option>
                </select>
              </div>
              <div v-if="showApntcYn" class="col-xl-2">
                <div class="form-check form-switch mt-2">
                  <span>{{ t('txt.user.emplym.probation') }}</span>
                  <input id="apntcYn" v-model="form.emplym.apntcYn" type="checkbox" class="form-check-input cursor-pointer ms-3" />
                  <label class="form-check-label fw-bold ms-3" for="apntcYn">{{ form.emplym.apntcYn ? t('txt.user.emplym.probation.active') : t('txt.user.emplym.not-applicable') }}</label>
                </div>
              </div>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2 fs-6 fw-bold required"><label>{{ t('txt.user.emplym.join-date') }}</label></div>
              <div class="col-xl-2">
                <input id="ecnyDt" v-model="form.emplym.ecnyDt" type="text" readonly class="form-control ps-12 required" />
                <div class="text-noti form-text">{{ t('txt.user.emplym.join-date-guide') }}</div>
              </div>
              <div class="col-xl-2">
                <div class="form-check form-switch">
                  <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.user.emplym.retired')">
                    {{ t('txt.user.emplym.retired-yn') }} <i class="bi bi-question-circle"></i>
                  </label>
                  <input id="retireYn" v-model="form.emplym.retireYn" type="checkbox" class="form-check-input cursor-pointer ms-3" />
                  <label class="form-check-label fw-bold ms-3" for="retireYn" :style="{color: form.emplym.retireYn ? 'red' : 'gray'}">{{ form.emplym.retireYn ? t('txt.user.emplym.retired') : t('txt.user.emplym.not-applicable') }}</label>
                </div>
              </div>
              <template v-if="form.emplym.retireYn">
                <div class="col-xl-1 fw-bold">{{ t('txt.user.emplym.retired-date') }}</div>
                <div class="col-xl-2"><input id="retireDt" v-model="form.emplym.retireDt" type="text" readonly class="form-control ps-12" autocomplete="off" /></div>
              </template>
            </div>
            <div class="row mb-4">
              <div class="col-xl-2 fw-bold">{{ t('txt.user.emplym.payroll-account') }}</div>
              <div class="col-xl-2"><input id="acntBank" v-model.trim="form.emplym.acntBank" maxlength="40" type="text" class="form-control form-control-solid" /></div>
              <div class="col-xl-2"><input id="acntNo" v-model.trim="form.emplym.acntNo" maxlength="40" type="text" class="form-control form-control-solid" /></div>
            </div>
          </div>
        </form>
      </div>
    </div>

    <div class="card-footer">
      <div class="d-flex justify-content-between">
        <div class="d-flex gap-3 flex-wrap align-items-start">
          <button type="button" class="btn btn-sm" :class="form.showProfile ? 'btn-danger' : 'btn-primary'" @click="toggleProfile">{{ form.showProfile ? t('txt.user.signup.profile.remove') : t('txt.user.form.add-profile') }}</button>
          <div>
            <button type="button" class="btn btn-sm mb-1" :class="form.showEmplym ? 'btn-danger' : 'btn-primary'" @click="toggleEmplym">{{ form.showEmplym ? t('txt.user.signup.employment.remove') : t('txt.user.form.add-employment') }}</button>
            <div class="text-noti">{{ t('txt.req.user.emplym') }}</div>
          </div>
        </div>
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
    await userSignupI18nService.load(resolvePageLocale());

    const mount = document.getElementById("user_signup_app");
    if (!mount) {

        console.error("[UserSignupApp] mount root #user_signup_app 미존재");

        return;
    }


    (Vue as any).createApp(UserSignupRoot).mount("#user_signup_app");

});
