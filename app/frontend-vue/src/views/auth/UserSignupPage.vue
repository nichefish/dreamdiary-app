<template>
  <!--begin::계정 신청 화면-->
  <div class="w-100 p-5 p-lg-10">
    <div class="card post w-100 w-xl-75 mx-auto my-auto">

      <!--begin::카드 헤더-->
      <div class="card-header min-h-auto mb-4">
        <div class="d-flex align-items-center gap-2">
          <a href="#" @click.prevent="goBack" class="text-muted">
            <i class="bi bi-house-fill fs-4 text-primary"></i>
          </a>
          <span class="text-muted fs-6">/ 계정 신청</span>
        </div>
      </div>
      <!--end::카드 헤더-->

      <!--begin::카드 바디-->
      <div class="card-body">
        <form id="userSignupForm" class="form" @submit.prevent>

          <!--begin::아이디-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="username" class="fw-bold col-form-label text-lg-center text-sm-end required">아이디</label>
            </div>
            <div class="col-xl-2 col-8">
              <input id="username" v-model.trim="form.username" type="text"
                class="form-control form-control-solid required no-space"
                placeholder="아이디" maxlength="16" autocomplete="username"
                @input="resetUsernameDup" />
              <div class="form-text text-noti">영문 소문자+숫자, 4~16자</div>
              <div class="small" :class="form.usernameMsgIsError ? 'text-danger' : 'text-success'">{{ form.usernameMsg }}</div>
            </div>
            <div class="col-xl-2 col-4">
              <button type="button" id="idDupChckBtn" class="btn btn-sm blink"
                :class="form.idDupBtnDisabled ? 'btn-success' : 'btn-secondary'"
                :disabled="form.idDupBtnDisabled" @click="onUsernameDup">
                아이디 중복 확인
              </button>
            </div>
          </div>
          <!--end::아이디-->

          <!--begin::비밀번호-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="password" class="fw-bold col-form-label text-lg-center text-sm-end required">비밀번호</label>
            </div>
            <div class="col-xl-2">
              <input id="password" v-model="form.password" type="password"
                class="form-control form-control-solid required no-space"
                maxlength="15" autocomplete="new-password" placeholder="비밀번호" />
              <div class="form-text text-noti">영문+숫자+특수문자, 9~15자</div>
            </div>
            <div class="col-xl-2">
              <label for="passwordCf" class="fw-bold col-form-label text-sm-start text-lg-end required">비밀번호 확인</label>
            </div>
            <div class="col-xl-2">
              <input id="passwordCf" v-model="form.passwordCf" type="password"
                class="form-control form-control-solid required no-space"
                maxlength="15" autocomplete="new-password" placeholder="비밀번호 확인" />
            </div>
          </div>
          <!--end::비밀번호-->

          <!--begin::닉네임-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="nickname" class="fw-bold col-form-label text-lg-center text-sm-start required">닉네임</label>
            </div>
            <div class="col-xl-2">
              <input id="nickname" v-model.trim="form.nickname" maxlength="20" type="text"
                class="form-control form-control-solid required" placeholder="닉네임" autocomplete="nickname" />
            </div>
          </div>
          <!--end::닉네임-->

          <!--begin::이메일-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="emailId" class="fw-bold col-form-label text-sm-start text-lg-center required">E-mail</label>
            </div>
            <div class="col-lg-2 col-4">
              <input id="emailId" v-model.trim="form.emailId" maxlength="20" type="text"
                class="form-control form-control-solid required no-space"
                @input="resetEmailDup" @change="resetEmailDup" />
              <div class="small" :class="form.emailMsgIsError ? 'text-danger' : 'text-success'">{{ form.emailMsg }}</div>
            </div>
            <div class="col-auto d-flex-center fw-bold text-gray-700">@</div>
            <div class="col-lg-2 col-5 vertical-center">
              <input id="emailDomain" v-model.trim="form.emailDomain" maxlength="20" type="text"
                class="form-control form-control-solid required no-space"
                @input="resetEmailDup" @change="resetEmailDup" />
            </div>
            <div class="col-xl-2 col-3">
              <select class="form-select form-select-solid" v-model="form.emailDomainSelect"
                @change="onEmailDomainSelect">
                <option value="">직접 입력</option>
                <option v-for="d in EMAIL_DOMAINS" :key="d" :value="d">{{ d }}</option>
              </select>
            </div>
            <div class="col-xl-2 col-4">
              <button id="emailDupChckBtn" type="button" class="btn btn-sm blink"
                :class="form.emailDupBtnDisabled ? 'btn-success' : 'btn-secondary'"
                :disabled="form.emailDupBtnDisabled" @click="onEmailDup">
                EMAIL 중복 확인
              </button>
            </div>
          </div>
          <!--end::이메일-->

          <!--begin::전화번호-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="phoneNumber" class="fw-bold col-form-label text-sm-start text-lg-center">전화번호</label>
            </div>
            <div class="col-xl-2">
              <input id="phoneNumber" v-model.trim="form.phoneNumber" maxlength="20" type="text"
                class="form-control form-control-solid" placeholder="010-0000-0000" />
            </div>
          </div>
          <!--end::전화번호-->

          <!--begin::접속 허용 IP-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="useAllowedIpYn" class="fw-bold col-form-label text-lg-center text-sm-start">접속 허용 IP</label>
            </div>
            <div class="col-xl-10">
              <div class="form-check form-switch form-check-custom form-check-solid mt-2">
                <input id="useAllowedIpYn" v-model="form.useAllowedIpYn" type="checkbox"
                  class="form-check-input cursor-pointer" />
                <label class="form-check-label fw-bold ms-3 cursor-pointer" for="useAllowedIpYn" style="color:gray;">
                  {{ form.useAllowedIpYn ? '사용' : '미사용' }}
                </label>
              </div>
              <div v-show="form.useAllowedIpYn" class="mt-2 mb-0">
                <input id="allowedIpListStr" v-model="form.allowedIpListStr" type="text"
                  class="form-control form-control-solid no-space" maxlength="500" autocomplete="off"
                  placeholder="허용할 IP 주소 (콤마 구분, 예: 192.168.1.1,10.0.0.1)" />
                <div class="form-text text-noti">IP 주소를 콤마로 구분하여 입력하세요.</div>
              </div>
            </div>
          </div>
          <!--end::접속 허용 IP-->

          <!--begin::신청 사유-->
          <div class="row mb-4">
            <div class="col-xl-2">
              <label for="content" class="fw-bold col-form-label text-sm-start text-lg-center">신청 사유</label>
            </div>
            <div class="col-xl-9">
              <textarea id="content" v-model="form.content"
                class="form-control form-control-solid h-100px" wrap="hard" maxlength="4000"></textarea>
            </div>
          </div>
          <!--end::신청 사유-->

          <!--begin::프로필 섹션 (선택)-->
          <div v-if="form.showProfile" class="border rounded p-4 mb-4 bg-light">
            <h6 class="fw-bold mb-3 text-gray-700">프로필 정보</h6>
            <div class="row mb-3">
              <div class="col-xl-2"><label for="proflCn" class="fw-bold col-form-label">자기소개</label></div>
              <div class="col-xl-9">
                <textarea id="proflCn" v-model="form.profile.proflCn"
                  class="form-control form-control-solid" rows="3" maxlength="4000"></textarea>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-xl-2"><label for="brthdy" class="fw-bold col-form-label">생년월일</label></div>
              <div class="col-xl-2">
                <input id="brthdy" v-model="form.profile.brthdy" type="date"
                  class="form-control form-control-solid" />
              </div>
              <div class="col-xl-3 d-flex align-items-center">
                <div class="form-check form-check-custom form-check-solid ms-4">
                  <input id="lunarYn" v-model="form.profile.lunarYn" type="checkbox" class="form-check-input" />
                  <label class="form-check-label ms-2" for="lunarYn">음력</label>
                </div>
              </div>
            </div>
          </div>
          <!--end::프로필 섹션-->

          <!--begin::인사 섹션 (선택)-->
          <div v-if="form.showEmplym" class="border rounded p-4 mb-4 bg-light">
            <h6 class="fw-bold mb-3 text-gray-700">인사 정보</h6>
            <div class="row mb-3">
              <div class="col-xl-2"><label for="emplymUserNm" class="fw-bold col-form-label required">실명</label></div>
              <div class="col-xl-2">
                <input id="emplymUserNm" v-model.trim="form.emplym.userNm" type="text"
                  class="form-control form-control-solid" maxlength="20" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-xl-2"><label class="fw-bold col-form-label required">업무 이메일</label></div>
              <div class="col-lg-2 col-4">
                <input v-model.trim="form.emplym.emplymEmailId" type="text"
                  class="form-control form-control-solid" maxlength="20" />
              </div>
              <div class="col-auto d-flex-center fw-bold">@</div>
              <div class="col-lg-2 col-5">
                <input id="emplymEmailDomain" v-model.trim="form.emplym.emplymEmailDomain" type="text"
                  class="form-control form-control-solid" maxlength="20" />
              </div>
              <div class="col-xl-2 col-3">
                <select class="form-select form-select-solid" v-model="form.emplym.emplymEmailDomainSelect"
                  @change="onEmplymEmailDomainSelect">
                  <option value="">직접 입력</option>
                  <option v-for="d in EMAIL_DOMAINS" :key="d" :value="d">{{ d }}</option>
                </select>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-xl-2"><label for="emplymPhoneNumber" class="fw-bold col-form-label required">업무 전화</label></div>
              <div class="col-xl-2">
                <input id="emplymPhoneNumber" v-model.trim="form.emplym.emplymPhoneNumber" type="text"
                  class="form-control form-control-solid" maxlength="20" placeholder="010-0000-0000" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-xl-2"><label for="ecnyDt" class="fw-bold col-form-label required">입사일</label></div>
              <div class="col-xl-2">
                <input id="ecnyDt" v-model="form.emplym.ecnyDt" type="date"
                  class="form-control form-control-solid" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-xl-2"><label class="fw-bold col-form-label">퇴직 여부</label></div>
              <div class="col-xl-10 d-flex align-items-center gap-4">
                <div class="form-check form-check-custom form-check-solid">
                  <input id="retireYn" v-model="form.emplym.retireYn" type="checkbox" class="form-check-input" />
                  <label class="form-check-label ms-2" for="retireYn">퇴직</label>
                </div>
                <input v-if="form.emplym.retireYn" id="retireDt" v-model="form.emplym.retireDt"
                  type="date" class="form-control form-control-solid w-auto" />
              </div>
            </div>
          </div>
          <!--end::인사 섹션-->

        </form>
      </div>
      <!--end::카드 바디-->

      <!--begin::카드 푸터-->
      <div class="card-footer">
        <div class="d-flex justify-content-between">
          <div class="d-flex gap-2">
            <button type="button" class="btn btn-sm btn-outline-secondary"
              @click="form.showProfile = !form.showProfile">
              {{ form.showProfile ? '프로필 정보 제거' : '프로필 정보 추가' }}
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary"
              @click="form.showEmplym = !form.showEmplym">
              {{ form.showEmplym ? '인사 정보 제거' : '인사 정보 추가' }}
            </button>
          </div>
          <div class="d-flex gap-2">
            <button type="button" class="btn btn-sm btn-primary" :disabled="store.submitting" @click="submit">
              <span v-if="store.submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
              <i class="bi bi-pencil-square"></i> 계정 신청
            </button>
            <button type="button" class="btn btn-sm btn-light" @click="goBack">
              <i class="bi bi-backspace"></i> 돌아가기
            </button>
          </div>
        </div>
      </div>
      <!--end::카드 푸터-->

    </div>
  </div>
  <!--end::계정 신청 화면-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { reactive } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useUserSignupStore } from "@/stores/userSignup";

const router = useRouter();
const store = useUserSignupStore();

/** 공통 이메일 도메인 선택지 */
const EMAIL_DOMAINS = ["gmail.com", "naver.com", "kakao.com"] as const;

/** 계정 신청 폼 로컬 상태 */
const form = reactive({
  username: "",
  password: "",
  passwordCf: "",
  nickname: "",
  emailId: "",
  emailDomain: "gmail.com",
  emailDomainSelect: "gmail.com",
  phoneNumber: "",
  useAllowedIpYn: false,
  allowedIpListStr: "",
  content: "",
  usernameDupPassed: "N",
  emailDupPassed: "N",
  usernameMsg: "",
  emailMsg: "",
  usernameMsgIsError: false,
  emailMsgIsError: false,
  idDupBtnDisabled: false,
  emailDupBtnDisabled: false,
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
    ecnyDt: "",
    retireYn: false,
    retireDt: "",
  },
});

/** 아이디 중복 확인 상태 초기화 */
function resetUsernameDup(): void {
  form.usernameMsg = "";
  form.usernameDupPassed = "N";
  form.idDupBtnDisabled = false;
}

/** 이메일 중복 확인 상태 초기화 */
function resetEmailDup(): void {
  form.emailMsg = "";
  form.emailDupPassed = "N";
  form.emailDupBtnDisabled = false;
}

/** 이메일 도메인 선택 시 emailDomain 필드를 갱신한다. */
function onEmailDomainSelect(): void {
  if (form.emailDomainSelect) {
    form.emailDomain = form.emailDomainSelect;
    resetEmailDup();
  }
}

/** 인사 이메일 도메인 선택 시 emplymEmailDomain 필드를 갱신한다. */
function onEmplymEmailDomainSelect(): void {
  if (form.emplym.emplymEmailDomainSelect) {
    form.emplym.emplymEmailDomain = form.emplym.emplymEmailDomainSelect;
  }
}

/** 아이디 중복 확인 */
async function onUsernameDup(): Promise<void> {
  const un = form.username.trim().toLowerCase();
  if (un.length < 4 || un.length > 16) {
    form.usernameMsg = "아이디는 영문 소문자+숫자 4~16자로 입력하세요.";
    form.usernameMsgIsError = true;
    return;
  }
  try {
    const res = await axios.get("/api/users/duplicate-check/username", { params: { username: un } });
    form.usernameMsg = (res.data?.message as string | undefined) ?? "";
    form.usernameDupPassed = res.data?.rslt ? "Y" : "N";
    form.usernameMsgIsError = !res.data?.rslt;
    form.idDupBtnDisabled = !!res.data?.rslt;
  } catch {
    form.usernameMsg = "중복 확인 중 오류가 발생했습니다.";
    form.usernameMsgIsError = true;
  }
}

/** 이메일 중복 확인 */
async function onEmailDup(): Promise<void> {
  const email = `${form.emailId.trim()}@${form.emailDomain.trim()}`;
  if (!form.emailId.trim() || !form.emailDomain.trim()) {
    form.emailMsg = "올바른 이메일 형식으로 입력하세요.";
    form.emailMsgIsError = true;
    return;
  }
  try {
    const res = await axios.get("/api/users/duplicate-check/email", { params: { email } });
    form.emailMsg = (res.data?.message as string | undefined) ?? "";
    form.emailDupPassed = res.data?.rslt ? "Y" : "N";
    form.emailMsgIsError = !res.data?.rslt;
    form.emailDupBtnDisabled = !!res.data?.rslt;
  } catch {
    form.emailMsg = "중복 확인 중 오류가 발생했습니다.";
    form.emailMsgIsError = true;
  }
}

/** 폼 유효성 검사 — null 반환 시 통과, 문자열 반환 시 해당 메시지를 alert 한다. */
function validate(): string | null {
  const un = form.username.trim().toLowerCase();
  if (un.length < 4 || un.length > 16) return "아이디는 4~16자로 입력하세요.";
  if (form.usernameDupPassed !== "Y") return "아이디 중복 확인을 완료해 주세요.";
  if (!form.password || form.password.length < 9 || form.password.length > 15)
    return "비밀번호는 9~15자로 입력하세요.";
  if (form.password !== form.passwordCf) return "비밀번호가 일치하지 않습니다.";
  if (!form.nickname.trim()) return "닉네임을 입력하세요.";
  if (!form.emailId.trim() || !form.emailDomain.trim()) return "이메일을 입력하세요.";
  if (form.emailDupPassed !== "Y") return "이메일 중복 확인을 완료해 주세요.";
  if (form.useAllowedIpYn && !form.allowedIpListStr.trim()) return "허용 IP를 입력하세요.";
  if (form.showEmplym) {
    if (!form.emplym.userNm.trim()) return "실명을 입력하세요.";
    if (!form.emplym.emplymEmailId.trim() || !form.emplym.emplymEmailDomain.trim())
      return "업무 이메일을 입력하세요.";
    if (!form.emplym.emplymPhoneNumber.trim()) return "업무 전화번호를 입력하세요.";
    if (!form.emplym.ecnyDt.trim()) return "입사일을 입력하세요.";
  }
  return null;
}

/**
 * 계정 신청 폼 제출.
 * 변경 전에는 성공 알림을 기다리지 않고 로그인 화면으로 이동했다.
 * 변경 후에는 성공 알림 OK 이후 로그인 화면으로 이동한다.
 */
async function submit(): Promise<void> {
  const errMsg = validate();
  if (errMsg) {
    void swalAlert(errMsg);
    return;
  }
  if (!confirm("계정을 신청하시겠습니까?")) return;

  const fd = new FormData();
  fd.append("username", form.username.trim().toLowerCase());
  fd.append("password", form.password);
  fd.append("ipDupChckPassed", form.usernameDupPassed);
  fd.append("nickname", form.nickname.trim());
  fd.append("emailId", form.emailId.trim());
  fd.append("emailDomain", form.emailDomain.trim());
  fd.append("emailDupChckPassed", form.emailDupPassed);
  fd.append("phoneNumber", form.phoneNumber.trim());
  fd.append("content", form.content);
  if (form.useAllowedIpYn) {
    fd.append("useAllowedIpYn", "Y");
    fd.append("allowedIpListStr", form.allowedIpListStr.trim());
  }
  if (form.showProfile) {
    fd.append("profile.proflCn", form.profile.proflCn);
    fd.append("profile.brthdy", form.profile.brthdy);
    if (form.profile.lunarYn) fd.append("profile.lunarYn", "Y");
  }
  if (form.showEmplym) {
    fd.append("emplym.userNm", form.emplym.userNm.trim());
    fd.append("emplym.emplymEmailId", form.emplym.emplymEmailId.trim());
    fd.append("emplym.emplymEmailDomain", form.emplym.emplymEmailDomain.trim());
    fd.append("emplym.emplymPhoneNumber", form.emplym.emplymPhoneNumber.trim());
    fd.append("emplym.ecnyDt", form.emplym.ecnyDt);
    if (form.emplym.retireYn) {
      fd.append("emplym.retireYn", "Y");
      fd.append("emplym.retireDt", form.emplym.retireDt);
    }
  }

  const result = await store.submitSignup(fd);
  await swalAlert(result.message);
  if (result.ok) {
    await router.push("/sign-in");
  }
}

/** 로그인 화면으로 돌아간다 (변경 내용 경고 포함). */
function goBack(): void {
  if (confirm("신청 내용이 사라집니다. 돌아가시겠습니까?")) {
    void router.push("/sign-in");
  }
}
</script>
