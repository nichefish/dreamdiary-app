import { CodeOption, UserForm, UserRoleOption } from "../types.js";
import UserProfileEmplymToggleButtons from "../../shared/components/UserProfileEmplymToggleButtons.js";
import { COMMON_EMAIL_DOMAIN_OPTIONS } from "../../shared/emailDomainShared.js";
import EmailDomainSelect from "../../shared/components/EmailDomainSelect.js";
import UserProfileEmplymFormSection from "../../shared/components/UserProfileEmplymFormSection.js";

export default {
    name: "UserFormPanel",
    components: {
        UserProfileEmplymToggleButtons,
        EmailDomainSelect,
        UserProfileEmplymFormSection,
    },
    props: {
        form: { type: Object, required: true },
        t: { type: Function, required: true },
        roles: { type: Array, required: true },
        cmpyOptions: { type: Array, required: true },
        teamOptions: { type: Array, required: true },
        emplymOptions: { type: Array, required: true },
        rankOptions: { type: Array, required: true },
    },
    emits: ["username-dup-check", "email-dup-check", "toggle-profile", "toggle-emplym", "submit", "list"],
    computed: {
        f(): UserForm {
            return this.form as UserForm;
        },
        roleOptions(): UserRoleOption[] {
            return this.roles as UserRoleOption[];
        },
        cmpyList(): CodeOption[] {
            return this.cmpyOptions as CodeOption[];
        },
        teamList(): CodeOption[] {
            return this.teamOptions as CodeOption[];
        },
        emplymList(): CodeOption[] {
            return this.emplymOptions as CodeOption[];
        },
        rankList(): CodeOption[] {
            return this.rankOptions as CodeOption[];
        },
        isReg(): boolean {
            return this.f.mode === "regist";
        },
        isMdf(): boolean {
            return this.f.mode === "modify";
        },
        emailDomainOptions(): string[] {
            return [...COMMON_EMAIL_DOMAIN_OPTIONS];
        },
        normalizedEmailDomain(): string {
            return this.f.emailDomain || (this.isReg ? "gmail.com" : "");
        },
    },
    methods: {
        isRoleSelected(roleKey: string): boolean {
            return Array.isArray(this.f.roleKeyList) && this.f.roleKeyList.includes(roleKey);
        },
        tx(key: string): string {
            const tr = this.t as (k: string) => string;
            return typeof tr === "function" ? tr(key) : key;
        },
    },
    template: `
    <div class="card post">
        <!--begin::Card body-->
        <div class="card-body">
            <!--begin::Form-->
            <form name="userRegForm" id="userRegForm" class="form mt-10" :data-mode="f.mode" method="post" enctype="multipart/form-data">
                <input type="hidden" name="id" :value="f.id || ''">
                <input type="hidden" name="fileGroupId" :value="f.fileGroupId || ''">
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-end" :class="{ required: isReg }">
                            <label for="username">{{ tx('txt.user.form.username') }}</label>
                        </div>
                    </div>
                    <template v-if="isReg">
                        <div class="col-xl-2 col-8">
                            <input type="text" name="username" id="username" class="form-control form-control-solid required no-space"
                                   :value="f.username" maxlength="16" />
                            <div class="form-text">{{ tx('txt.req.username') }}</div>
                            <div id="username_validate_span" class="text-danger"></div>
                            <div id="ipDupChckPassed_validate_span" class="text-danger"></div>
                        </div>
                        <div class="col-xl-2 col-4">
                            <button type="button" class="btn btn-sm btn-secondary required blink" id="idDupChckBtn"
                                    @click="$emit('username-dup-check')"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.username-dup-check')">
                                <span class="hidden-table">{{ tx('txt.user.form.username') }}</span>{{ tx('txt.user.form.dup-check') }}
                            </button>
                            <input type="hidden" name="ipDupChckPassed" id="ipDupChckPassed" value="N"/>
                        </div>
                    </template>
                    <template v-if="isMdf">
                        <div class="col-xl-2">
                            <input type="hidden" name="username" id="username" :value="f.username" />
                            <div class="fw-bold col-form-label text-start">{{ f.username }}</div>
                        </div>
                    </template>
                </div>
                <template v-if="isReg">
                    <!--begin::Row-->
                    <div class="row mb-4">
                        <div class="col-xl-2">
                            <div class="fw-bold col-form-label text-lg-center text-sm-end required">
                                <label for="password">{{ tx('txt.user.form.password') }}</label>
                            </div>
                        </div>
                        <div class="col-xl-2">
                            <input type="password" name="password" id="password" class="form-control form-control-solid required no-space"
                                   maxlength="20" autocomplete="off" />
                            <div class="form-text text-noti">{{ tx('txt.req.password') }}</div>
                            <div id="password_validate_span"></div>
                        </div>
                        <div class="col-xl-2">
                            <div class="fw-bold col-form-label text-sm-start text-lg-end required">
                                <label for="passwordCf">{{ tx('txt.user.form.password-confirm') }}</label>
                            </div>
                        </div>
                        <div class="col-xl-2">
                            <input type="password" name="passwordCf" id="passwordCf" class="form-control form-control-solid no-space"
                                   maxlength="20" autocomplete="off" />
                            <div id="passwordCf_validate_span"></div>
                        </div>
                    </div>
                </template>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-end required">
                            <label for="roleKey" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.role')">
                                {{ tx('txt.user.form.role') }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <select name="roleKeysStr" id="roleKey" class="form-select form-select-solid required"
                                data-control="select2" data-allow-clear="true" data-hide-search="true" data-close-on-select="false" multiple="multiple">
                            <option v-for="role in roleOptions" :key="role.roleKey" :value="role.roleKey" :selected="isRoleSelected(role.roleKey)">
                                {{ role.roleName }}
                            </option>
                        </select>
                        <div id="roleKey_validate_span"></div>
                    </div>
                </div>
                <!--begin::Row-->
                <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                <div class="row mb-4" id="nicknameShowDiv">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-start required"
                             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.nickname-request')">
                            <label for="nickname" class="cursor-help">
                                {{ tx('txt.user.form.nickname') }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="text" name="nickname" id="nickname" class="form-control form-control-solid required"
                               :value="f.nickname" :placeholder="tx('txt.user.form.nickname')" maxlength="20" />
                        <div class="form-text text-noti">{{ tx('txt.req.nicknm') }}</div>
                        <span id="nickname_validate_span"></span>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                            <label for="emailId" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.email')">
                                E-mail
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-lg-2 col-4">
                        <input type="text" name="emailId" id="emailId" class="form-control form-control-solid no-space"
                               :value="f.emailId" :placeholder="tx('txt.user.form.custom-input')" maxlength="20" />
                        <div id="emailId_validate_span"></div>
                    </div>
                    <div class="col-xl-2 col-1 d-flex-center fw-bold text-gray-700" style="width:2%;">
                        <label for="emailDomain"><label for="emailDomainSelect">@</label></label>
                    </div>
                    <div class="col-lg-2 col-5 vertical-center">
                        <input type="text" name="emailDomain" id="emailDomain" class="form-control form-control-solid no-space"
                               :value="normalizedEmailDomain" :placeholder="tx('txt.user.form.custom-input')" maxlength="20" />
                        <div id="emailDomain_validate_span"></div>
                    </div>
                    <div class="col-xl-2 col-1">
                        <EmailDomainSelect
                            id="emailDomainSelect"
                            name="emailDomainSelect"
                            :options="emailDomainOptions"
                            :custom-input-label="tx('txt.user.form.custom-input')"
                            :selected-value="isReg ? 'gmail.com' : ''"
                        />
                    </div>
                    <div class="col-xl-2 col-4">
                        <button type="button" class="btn btn-sm btn-secondary required blink" id="emailDupChckBtn"
                                @click="$emit('email-dup-check')"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.email-dup-check')">
                            <span class="hidden-table">EMAIL </span>{{ tx('txt.user.form.dup-check') }}
                        </button>
                        <input type="hidden" name="emailDupChckPassed" id="emailDupChckPassed" value="N"/>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center">
                            <label for="phoneNumber">{{ tx('txt.user.form.phone-number') }}</label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="text" name="phoneNumber" id="phoneNumber" class="form-control form-control-solid"
                               :value="f.phoneNumber" maxlength="20" />
                        <div class="form-text text-noti">{{ tx('txt.req.phoneNumber') }}</div>
                        <span id="phoneNumber_valid_span"></span>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-start">
                            <label for="allowedIpListStr" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.allowed-ip')">
                                {{ tx('txt.user.form.allowed-ip') }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <div class="form-check form-switch form-check-custom form-check-solid mt-2">
                            <input type="checkbox" name="useAllowedIpYn" id="useAllowedIpYn" class="form-check-input cursor-pointer" value="Y"
                                   :checked="f.useAllowedIp" />
                            <label class="form-check-label fw-bold ms-3" for="useAllowedIpYn" id="useAllowedIpYnLabel" :style="{ color: f.useAllowedIp ? 'blue' : 'gray' }">
                                <template v-if="!f.useAllowedIp">{{ tx('txt.status.unuse') }}</template>{{ tx('txt.status.use') }}
                            </label>
                        </div>
                        <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                        <div id="allowedIpListSpan" class="mt-2 mb-0" :style="{ display: f.useAllowedIp ? '' : 'none' }">
                            <input type="text" name="allowedIpListStr" id="allowedIpListStr" class="form-control form-control-solid no-space"
                                   :value="f.allowedIpListStr" maxlength="500" autocomplete="off" />
                            <div class="form-text text-noti">{{ tx('txt.req.acsip') }}</div>
                            <div id="allowedIpListStr_valid_span"></div>
                        </div>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center">
                            <label for="content" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.account-description')">
                                {{ tx('txt.user.form.account-description') }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-9">
                        <textarea name="content" id="content" class="form-control form-control-solid h-100px" wrap="hard" maxlength="4000" :value="f.content" :placeholder="tx('txt.user.form.account-description')"></textarea>
                    </div>
                </div>

                <UserProfileEmplymFormSection
                    :form="f"
                    :t="tx"
                    :show-profile="f.hasProfile"
                    :show-emplym="f.hasEmplym"
                    :cmpy-list="cmpyList"
                    :team-list="teamList"
                    :emplym-list="emplymList"
                    :rank-list="rankList"
                    :custom-input-label="tx('txt.user.form.custom-input')"
                    mode="userForm"
                />
            </form>
            <!--end:Form-->
        </div>
        <!--begin::Card footer-->
        <div class="card-footer">
            <!--버튼 영역-->
            <div class="d-flex justify-content-between">
                <div>
                    <UserProfileEmplymToggleButtons
                        :has-profile="f.hasProfile"
                        :has-emplym="f.hasEmplym"
                        :add-profile-label="tx('txt.user.form.add-profile')"
                        :remove-profile-label="tx('txt.user.signup.profile.remove')"
                        :add-emplym-label="tx('txt.user.form.add-employment')"
                        :remove-emplym-label="tx('txt.user.signup.employment.remove')"
                        :emplym-notice-label="tx('txt.req.user.emplym')"
                        @toggle-profile="$emit('toggle-profile')"
                        @toggle-emplym="$emit('toggle-emplym')"
                    />
                </div>
                <div class="gap-2">
                    <!--페이지:: 버튼: 저장하기-->
                    <button type="button" class="btn btn-sm btn-primary mx-2"
                            @click="$emit('submit')"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.save')">
                        <i class="bi bi-pencil-square"></i>
                        <span class="indicator-label">{{ tx('txt.user.form.save') }}</span>
                        <span class="indicator-progress">Please wait...
                            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                        </span>
                    </button>
                    <!--페이지:: 버튼: 목록으로 가기-->
                    <button type="button" class="btn btn-sm btn-light"
                            @click="$emit('list')"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tx('bs.tooltip.user.form.list')">
                        <span class="indicator-label">
                            <i class="bi bi-list"></i>{{ tx('txt.user.form.list') }}
                        </span>
                    </button>
                </div>
            </div>
        </div>
    </div>
    `,
};
