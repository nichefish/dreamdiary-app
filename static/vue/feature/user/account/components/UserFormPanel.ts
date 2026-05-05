import { CodeOption, UserForm, UserFormLabels, UserRoleOption } from "../types.js";

export default {
    name: "UserFormPanel",
    props: {
        form: { type: Object, required: true },
        labels: { type: Object, required: true },
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
        l(): UserFormLabels {
            return this.labels as UserFormLabels;
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
        normalizedEmailDomain(): string {
            return this.f.emailDomain || (this.isReg ? "gmail.com" : "");
        },
    },
    methods: {
        isRoleSelected(roleKey: string): boolean {
            return Array.isArray(this.f.roleKeyList) && this.f.roleKeyList.includes(roleKey);
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
                            <label for="username">{{ l.username }}</label>
                        </div>
                    </div>
                    <template v-if="isReg">
                        <div class="col-xl-2 col-8">
                            <input type="text" name="username" id="username" class="form-control form-control-solid required no-space"
                                   :value="f.username" maxlength="16" />
                            <div class="form-text">{{ l.usernameReq }}</div>
                            <div id="username_validate_span" class="text-danger"></div>
                            <div id="ipDupChckPassed_validate_span" class="text-danger"></div>
                        </div>
                        <div class="col-xl-2 col-4">
                            <button type="button" class="btn btn-sm btn-secondary required blink" id="idDupChckBtn"
                                    @click="$emit('username-dup-check')"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipUsernameDupCheck">
                                <span class="hidden-table">{{ l.username }}</span>{{ l.dupCheck }}
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
                                <label for="password">{{ l.password }}</label>
                            </div>
                        </div>
                        <div class="col-xl-2">
                            <input type="password" name="password" id="password" class="form-control form-control-solid required no-space"
                                   maxlength="20" autocomplete="off" />
                            <div class="form-text text-noti">{{ l.passwordReq }}</div>
                            <div id="password_validate_span"></div>
                        </div>
                        <div class="col-xl-2">
                            <div class="fw-bold col-form-label text-sm-start text-lg-end required">
                                <label for="passwordCf">{{ l.passwordConfirm }}</label>
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
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipRole">
                                {{ l.role }}
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
                             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipNickname">
                            <label for="nickname" class="cursor-help">
                                {{ l.nickname }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="text" name="nickname" id="nickname" class="form-control form-control-solid required"
                               :value="f.nickname" :placeholder="l.nickname" maxlength="20" />
                        <div class="form-text text-noti">{{ l.nicknameReq }}</div>
                        <span id="nickname_validate_span"></span>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                            <label for="emailId" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipEmail">
                                E-mail
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-lg-2 col-4">
                        <input type="text" name="emailId" id="emailId" class="form-control form-control-solid no-space"
                               :value="f.emailId" :placeholder="l.emailIdPlaceholder" maxlength="20" />
                        <div id="emailId_validate_span"></div>
                    </div>
                    <div class="col-xl-2 col-1 d-flex-center fw-bold text-gray-700" style="width:2%;">
                        <label for="emailDomain"><label for="emailDomainSelect">@</label></label>
                    </div>
                    <div class="col-lg-2 col-5 vertical-center">
                        <input type="text" name="emailDomain" id="emailDomain" class="form-control form-control-solid no-space"
                               :value="normalizedEmailDomain" :placeholder="l.customInput" maxlength="20" />
                        <div id="emailDomain_validate_span"></div>
                    </div>
                    <div class="col-xl-2 col-1">
                        <select name="emailDomainSelect" id="emailDomainSelect" class="form-select form-select-solid">
                            <option value="">{{ l.customInput }}</option>
                            <option value="gmail.com" :selected="isReg">gmail.com</option>
                            <option value="naver.com">naver.com</option>
                            <option value="kakao.com">kakao.com</option>
                        </select>
                    </div>
                    <div class="col-xl-2 col-4">
                        <button type="button" class="btn btn-sm btn-secondary required blink" id="emailDupChckBtn"
                                @click="$emit('email-dup-check')"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipEmailDupCheck">
                            <span class="hidden-table">EMAIL </span>{{ l.dupCheck }}
                        </button>
                        <input type="hidden" name="emailDupChckPassed" id="emailDupChckPassed" value="N"/>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center">
                            <label for="phoneNumber">{{ l.phoneNumber }}</label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="text" name="phoneNumber" id="phoneNumber" class="form-control form-control-solid"
                               :value="f.phoneNumber" maxlength="20" />
                        <div class="form-text text-noti">{{ l.phoneReq }}</div>
                        <span id="phoneNumber_valid_span"></span>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-start">
                            <label for="allowedIpListStr" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipAllowedIp">
                                {{ l.allowedIp }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <div class="form-check form-switch form-check-custom form-check-solid mt-2">
                            <input type="checkbox" name="useAllowedIpYn" id="useAllowedIpYn" class="form-check-input cursor-pointer" value="Y"
                                   :checked="f.useAllowedIp" />
                            <label class="form-check-label fw-bold ms-3" for="useAllowedIpYn" id="useAllowedIpYnLabel" :style="{ color: f.useAllowedIp ? 'blue' : 'gray' }">
                                <template v-if="!f.useAllowedIp">{{ l.unuse }}</template>{{ l.use }}
                            </label>
                        </div>
                        <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                        <div id="allowedIpListSpan" class="mt-2 mb-0" :style="{ display: f.useAllowedIp ? '' : 'none' }">
                            <input type="text" name="allowedIpListStr" id="allowedIpListStr" class="form-control form-control-solid no-space"
                                   :value="f.allowedIpListStr" maxlength="500" autocomplete="off" />
                            <div class="form-text text-noti">{{ l.allowedIpReq }}</div>
                            <div id="allowedIpListStr_valid_span"></div>
                        </div>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center">
                            <label for="content" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipAccountDescription">
                                {{ l.accountDescription }}
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-9">
                        <textarea name="content" id="content" class="form-control form-control-solid h-100px" wrap="hard" maxlength="4000" :value="f.content" :placeholder="l.accountDescriptionPlaceholder"></textarea>
                    </div>
                </div>

                <!--사용자 프로필 정보 영역-->
                <div id="user_profile_div">
                    <template v-if="f.hasProfile">
                        <div class="separator my-8"></div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center">
                                    <label for="brthdy" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.profileBirthDate }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                </div>
                            </div>
                            <div class="col-xl-2">
                                <input type="text" name="profile.brthdy" id="brthdy" class="form-control form-control-solid" :value="f.profile.brthdy" :placeholder="l.profileBirthDate" readonly/>
                            </div>
                            <div class="col-xl-2">
                                <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                                    <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">{{ l.profileLunarYn }}</label>
                                    <input type="checkbox" name="profile.lunarYn" id="lunarYn" class="form-check-input cursor-pointer ms-3" value="Y" :checked="f.profile.lunarYn === 'Y'" />
                                    <label class="form-check-label fw-bold ms-3" for="lunarYn" id="lunarYnLabel" :style="{ color: f.profile.lunarYn === 'Y' ? 'blue' : 'gray' }">
                                        {{ f.profile.lunarYn === 'Y' ? l.profileLunar : l.profileSolar }}
                                    </label>
                                </div>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center">
                                    <label for="proflCn" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">{{ l.profileProfile }}</label>
                                </div>
                            </div>
                            <div class="col-xl-9">
                                <!-- 변경 전: Handlebars 에서 같은 name 두 textarea 존재. 변경 후: profile.proflCn 단일 매핑. -->
                                <textarea name="profile.proflCn" id="proflCn" class="form-control form-control-solid h-100px" wrap="hard" maxlength="500" :value="f.profile.proflCn"></textarea>
                            </div>
                        </div>
                    </template>
                </div>

                <!--사용자 인사정보 영역-->
                <div id="user_emplym_div">
                    <template v-if="f.hasEmplym">
                        <div class="separator my-8"></div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center required">
                                    <label for="userNm" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.emplymUserName }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                </div>
                            </div>
                            <div class="col-xl-2">
                                <input type="text" name="emplym.userNm" id="userNm" class="form-control form-control-solid required" :value="f.emplym.userNm" :placeholder="l.emplymNamePlaceholder" maxlength="20" />
                                <div class="form-text text-noti">{{ l.nicknameReq }}</div>
                                <div id="userNm_validate_span"></div>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                                    <label for="emplymEmailId" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.emplymEmail }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                </div>
                            </div>
                            <div class="col-lg-2 col-4">
                                <input type="text" name="emplym.emplymEmailId" id="emplymEmailId" class="form-control form-control-solid no-space required" :value="f.emplym.emplymEmailId" :placeholder="l.emailIdPlaceholder" maxlength="20" />
                                <div id="emplymEmailId_validate_span"></div>
                            </div>
                            <div class="col-xl-2 col-1 d-flex-center fw-bold text-gray-700" style="width:2%;">
                                <label for="emailDomain"><label for="emplymEmailDomainSelect">@</label></label>
                            </div>
                            <div class="col-lg-2 col-5 vertical-center">
                                <input type="text" name="emplym.emplymEmailDomain" id="emplymEmailDomain" class="form-control form-control-solid no-space required" :value="f.emplym.emplymEmailDomain" :placeholder="l.customInput" maxlength="20" />
                                <div id="emplymEmailDomain_validate_span"></div>
                            </div>
                            <div class="col-xl-2 col-1">
                                <select name="emplymEmailDomainSelect" id="emplymEmailDomainSelect" class="form-select form-select-solid">
                                    <option value="">{{ l.customInput }}</option>
                                    <option value="gmail.com" :selected="isReg">gmail.com</option>
                                    <option value="naver.com">naver.com</option>
                                    <option value="kakao.com">kakao.com</option>
                                </select>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                                    <label for="phoneNumber" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.emplymPhoneNumber }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                </div>
                            </div>
                            <div class="col-xl-2">
                                <input type="text" name="emplym.emplymPhoneNumber" id="emplymPhoneNumber" class="form-control form-control-solid required" :value="f.emplym.emplymPhoneNumber" maxlength="20" />
                                <div class="form-text text-noti">{{ l.phoneReq }}</div>
                                <span id="phoneNumber_validate_span"></span>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center">
                                    <label for="cmpyCd" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.emplymAffiliation }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                </div>
                            </div>
                            <div class="col-xl-2 col-6">
                                <select name="emplym.cmpyCd" id="cmpyCd" class="form-select form-select-solid">
                                    <option value="">{{ l.emplymCompanyOption }}</option>
                                    <option v-for="item in cmpyList" :key="item.code" :value="item.code" :selected="item.code === f.emplym.cmpyCd">{{ item.codeName }}</option>
                                </select>
                            </div>
                            <div class="col-xl-1 col-6">
                                <select name="emplym.teamCd" id="teamCd" class="form-select form-select-solid">
                                    <option value="">{{ l.emplymTeamOption }}</option>
                                    <option v-for="item in teamList" :key="item.code" :value="item.code" :selected="item.code === f.emplym.teamCd">{{ item.codeName }}</option>
                                </select>
                            </div>
                            <div class="col-xl-2 mg-lg-0">
                                <select name="emplym.emplymCd" id="emplymCd" class="form-select form-select-solid">
                                    <option value="">{{ l.emplymEmploymentTypeOption }}</option>
                                    <option v-for="item in emplymList" :key="item.code" :value="item.code" :selected="item.code === f.emplym.emplymCd">{{ item.codeName }}</option>
                                </select>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center"><label for="rankCd">{{ l.emplymRank }}</label></div>
                            </div>
                            <div class="col-xl-2">
                                <select name="emplym.rankCd" id="rankCd" class="form-select form-select-solid">
                                    <option value="">{{ l.emplymSelectOption }}</option>
                                    <option v-for="item in rankList" :key="item.code" :value="item.code" :selected="item.code === f.emplym.rankCd">{{ item.codeName }}</option>
                                </select>
                            </div>
                            <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                            <div class="col-xl-2" id="apntcYnDiv" style="display:none;">
                                <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                                    {{ l.emplymProbation }}
                                    <input type="checkbox" name="emplym.apntcYn" id="apntcYn" class="form-check-input cursor-pointer ms-3" value="Y" :checked="f.emplym.apntcYn === 'Y'" />
                                    <label class="form-check-label fw-bold ms-3" for="apntcYn" id="apntcYnLabel" :style="{ color: f.emplym.apntcYn === 'Y' ? 'blue' : 'gray' }">{{ f.emplym.apntcYn === 'Y' ? l.emplymProbationActive : l.emplymNotApplicable }}</label>
                                </div>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center required">
                                    <label for="ecnyDt">{{ l.emplymJoinDate }}</label>
                                </div>
                            </div>
                            <div class="col-xl-2">
                                <input type="text" name="emplym.ecnyDt" id="ecnyDt" class="form-control form-control-solid required" :value="f.emplym.ecnyDt" :placeholder="l.emplymJoinDate" readonly/>
                                <div class="text-noti form-text">{{ l.emplymJoinDateGuide }}</div>
                            </div>
                            <div class="col-xl-2">
                                <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                                    <label class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.emplymRetiredYn }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                    <input type="checkbox" name="emplym.retireYn" id="retireYn" class="form-check-input cursor-pointer ms-3" value="Y" :checked="f.emplym.retireYn === 'Y'" />
                                    <label class="form-check-label fw-bold ms-3" for="retireYn" id="retireYnLabel" :style="{ color: f.emplym.retireYn === 'Y' ? 'red' : 'gray' }">{{ f.emplym.retireYn === 'Y' ? l.emplymRetired : l.emplymNotApplicable }}</label>
                                </div>
                            </div>
                            <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                            <div class="col-xl-1 col-form-label fw-bold col-2 my-2 my-lg-0 retireDtDiv" :style="{ display: f.emplym.retireYn === 'Y' ? '' : 'none' }">
                                <label for="retireDt">{{ l.emplymRetiredDate }}</label>
                            </div>
                            <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                            <div class="col-xl-2 col-10 retireDtDiv" :style="{ display: f.emplym.retireYn === 'Y' || f.emplym.retireDt ? '' : 'none' }">
                                <input type="text" name="emplym.retireDt" id="retireDt" class="form-control form-control-solid" :value="f.emplym.retireDt" :placeholder="l.emplymRetiredDate" readonly/>
                                <div class="text-noti form-text mt-0">{{ l.emplymRetiredDateGuide }}</div>
                            </div>
                        </div>
                        <!--begin::Row-->
                        <div class="row mb-4">
                            <div class="col-xl-2">
                                <div class="fs-6 fw-bold col-form-label text-sm-start text-lg-center">
                                    <label for="acntNo" class="cursor-help" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click">
                                        {{ l.emplymPayrollAccount }}
                                        <i class="bi bi-question-circle"></i>
                                    </label>
                                </div>
                            </div>
                            <div class="col-xl-1">
                                <input type="text" name="emplym.acntBank" id="acntBank" class="form-control form-control-solid" :value="f.emplym.acntBank" :placeholder="l.emplymBank" maxlength="40">
                            </div>
                            <div class="col-xl-2">
                                <input type="text" name="emplym.acntNo" id="acntNo" class="form-control form-control-solid" :value="f.emplym.acntNo" :placeholder="l.emplymAccountNumber" maxlength="40">
                                <div id="acntNo_validate_span"></div>
                            </div>
                        </div>
                    </template>
                </div>
            </form>
            <!--end:Form-->
        </div>
        <!--begin::Card footer-->
        <div class="card-footer">
            <!--버튼 영역-->
            <div class="d-flex justify-content-between">
                <div>
                    <div class="d-flex gap-2">
                        <div id="user_profile_reg_btn_div">
                            <button type="button" id="userProfileBtn" class="btn btn-sm btn-primary blink"
                                    @click="$emit('toggle-profile')"
                                    :data-profile="f.hasProfile"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipAddProfile">
                                {{ f.hasProfile ? l.removeProfile : l.addProfile }}
                            </button>
                        </div>
                        <div>
                            <button type="button" id="userEmplymBtn" class="btn btn-sm btn-primary blink"
                                    @click="$emit('toggle-emplym')"
                                    :data-emplym="f.hasEmplym"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipAddEmployment">
                                {{ f.hasEmplym ? l.removeEmployment : l.addEmployment }}
                            </button>
                            <div class="text-noti">{{ l.userEmplymReq }}</div>
                        </div>
                    </div>
                </div>
                <div class="gap-2">
                    <!--페이지:: 버튼: 저장하기-->
                    <button type="button" class="btn btn-sm btn-primary mx-2"
                            @click="$emit('submit')"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipSave">
                        <i class="bi bi-pencil-square"></i>
                        <span class="indicator-label">{{ l.save }}</span>
                        <span class="indicator-progress">Please wait...
                            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                        </span>
                    </button>
                    <!--페이지:: 버튼: 목록으로 가기-->
                    <button type="button" class="btn btn-sm btn-light"
                            @click="$emit('list')"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.tooltipList">
                        <span class="indicator-label">
                            <i class="bi bi-list"></i>{{ l.list }}
                        </span>
                    </button>
                </div>
            </div>
        </div>
    </div>
    `,
};
