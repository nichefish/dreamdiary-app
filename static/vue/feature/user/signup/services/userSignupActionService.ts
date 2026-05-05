/**
 * 계정 신청: 유효성·중복 확인·FormData·전송
 *
 * @author nichefish
 */
import type { CodeRow } from "./userSignupDataService.js";
import type { UserSignupFormState } from "../types.js";

type TFn = (k: string) => string;

/** FormData 필드명은 기존 스프링 바인딩/Handlebars 폼과 동일하게 유지한다. */
function appendIfHas(fd: FormData, name: string, value: string | null | undefined): void {
    if (value === undefined || value === null)
        return;
    fd.append(name, String(value));
}

/** 체크박스 필드명 — 체크 시에만 레거그와 동일하게 전송된다. */
function appendYnCheckbox(fd: FormData, name: string, checked: boolean): void {
    if (checked)
        fd.append(name, "Y");
}

function normalizeUsername(u: string): string {
    return (u || "").trim().toLowerCase();
}

/**
 * 상태를 레거시 Multipart 형식과 동일한 이름으로 채운 FormData 로 만든다.
 *
 * 변경 전/후: `@Size(max = 15)` 인 서버 비밀번호 제약과 맞추기 위해 클라이언트 허용 길이를 15자까지로 제한(기존 jQuery 규칙은 maxlength 20 과 불일치).
 */
export function buildUserSignupFormData(st: Readonly<UserSignupFormState>): FormData {
    const fd = new FormData();

    appendIfHas(fd, "id", st.id || "");
    appendIfHas(fd, "fileGroupId", st.fileGroupId);

    const un = normalizeUsername(st.username);
    fd.append("username", un);

    fd.append("ipDupChckPassed", st.usernameDupPassed);
    fd.append("password", st.password);

    fd.append("userRoles[0].roleKey", st.authUserRoleKey || "");
    appendIfHas(fd, "roleKeysStr", st.authUserRoleKey || "");

    fd.append("nickname", (st.nickname || "").trim());
    fd.append("emailId", (st.emailId || "").trim());
    fd.append("emailDomain", (st.emailDomain || "").trim());
    fd.append("emailDupChckPassed", st.emailDupPassed);

    fd.append("phoneNumber", (st.phoneNumber || "").trim());
    fd.append("content", st.content || "");

    appendYnCheckbox(fd, "useAllowedIpYn", st.useAllowedIpYn);

    const allowedIpInput = document.getElementById("allowedIpListStr") as HTMLInputElement | null;
    const rawIpVal = allowedIpInput ? allowedIpInput.value : "";
    if (st.useAllowedIpYn)
        fd.append("allowedIpListStr", rawIpVal);

    if (st.showProfile) {
        appendIfHas(fd, "profile.proflCn", st.profile.proflCn || "");
        appendIfHas(fd, "profile.brthdy", st.profile.brthdy || "");
        appendYnCheckbox(fd, "profile.lunarYn", st.profile.lunarYn);
    }

    if (st.showEmplym) {
        appendIfHas(fd, "emplym.userNm", st.emplym.userNm || "");
        appendIfHas(fd, "emplym.emplymEmailId", st.emplym.emplymEmailId || "");
        appendIfHas(fd, "emplym.emplymEmailDomain", st.emplym.emplymEmailDomain || "");
        fd.append("emplym.emplymPhoneNumber", (st.emplym.emplymPhoneNumber || "").trim());
        appendIfHas(fd, "emplym.cmpyCd", st.emplym.cmpyCd || "");
        appendIfHas(fd, "emplym.teamCd", st.emplym.teamCd || "");
        appendIfHas(fd, "emplym.emplymCd", st.emplym.emplymCd || "");
        appendIfHas(fd, "emplym.rankCd", st.emplym.rankCd || "");
        appendYnCheckbox(fd, "emplym.apntcYn", st.emplym.apntcYn);
        appendYnCheckbox(fd, "emplym.retireYn", st.emplym.retireYn);
        appendIfHas(fd, "emplym.ecnyDt", st.emplym.ecnyDt || "");
        appendIfHas(fd, "emplym.retireDt", st.emplym.retireDt || "");
        appendIfHas(fd, "emplym.acntBank", st.emplym.acntBank || "");
        appendIfHas(fd, "emplym.acntNo", st.emplym.acntNo || "");
    }

    return fd;
}

export type ValidationResult = { ok: true } | { ok: false; message: string };

/**
 * 레거시 jQuery validate 규칙(구 user_reqst_module.initForm — 제거됨)과 동등한 브라우저 검증.
 */
export function validateUserSignupForm(st: Readonly<UserSignupFormState>, t: TFn): ValidationResult {
    const pwCheck = validatePasswordRules(st.password, st.passwordCf, t);

    const un = normalizeUsername(st.username);
    if (un.length < 4 || un.length > 16)
        return { ok: false, message: t("txt.req.username") };
    if (!cF.regex.id.test(un))
        return { ok: false, message: t("msg.user.signup.username.format") };

    if (st.usernameDupPassed !== "Y")
        return { ok: false, message: t("msg.user.signup.dupchk.username.required") };

    if (!pwCheck.ok)
        return pwCheck;

    const nick = (st.nickname || "").trim();
    if (!nick)
        return { ok: false, message: t("txt.req.nicknm") };

    const eid = (st.emailId || "").trim();
    const ed = (st.emailDomain || "").trim();
    const em = `${eid}@${ed}`;
    if (!cF.regex.email.test(em))
        return { ok: false, message: t("msg.user.signup.email.format") };

    if (st.emailDupPassed !== "Y")
        return { ok: false, message: t("msg.user.signup.dupchk.email.required") };

    if (st.useAllowedIpYn) {
        const el = document.getElementById("allowedIpListStr") as HTMLInputElement | null;
        const v = el ? String(el.value || "").trim() : "";
        if (!v)
            return { ok: false, message: t("msg.user.signup.allowed-ip.required") };
    }

    if (st.showEmplym) {
        const emp = st.emplym;
        if (!(emp.userNm || "").trim())
            return { ok: false, message: t("txt.req.nicknm") };
        const eeid = (emp.emplymEmailId || "").trim();
        const edom = (emp.emplymEmailDomain || "").trim();
        const eem = `${eeid}@${edom}`;
        if (!cF.regex.email.test(eem))
            return { ok: false, message: t("msg.user.signup.email.format") };
        if (!(emp.emplymPhoneNumber || "").trim())
            return { ok: false, message: t("txt.req.phoneNumber") };
        if (!(emp.ecnyDt || "").trim())
            return { ok: false, message: t("txt.user.emplym.join-date") };
    }

    return { ok: true };
}

export function validatePasswordRules(password: string, passwordCf: string, t: TFn): ValidationResult {
    if (!password || password.length < 9 || password.length > 15)
        return { ok: false, message: t("txt.req.password") };
    const rx = typeof cF !== "undefined" && cF.regex ? cF.regex.pw : null;

    /** @remarks 레거시 jQuery.additional-methods 패턴 검사 결과와 동일 경로 유지 */

    const okRx = rx && typeof rx.test === "function" ? rx.test(password) : true;
    if (!okRx)
        return { ok: false, message: t("msg.user.signup.password.regex") };

    if (password !== passwordCf)
        return { ok: false, message: t("msg.user.signup.password.cf.mismatch") };
    return { ok: true };
}

export function dupCheckUsername(username: string, form: UserSignupFormState, t: TFn): void {
    const un = normalizeUsername(username);
    if (!cF.regex.id.test(un)) {
        form.usernameMsg = t("msg.user.signup.username.format");
        form.usernameMsgIsError = true;
        form.usernameDupPassed = "N";
        form.idDupBtnDisabled = false;
        return;
    }

    cF.ajax.get(Url.USERNAME_DUP_CHK_AJAX, { username: un }, function(res: AjaxResponse): void {
        form.usernameMsg = res.message || "";
        form.usernameDupPassed = res.rslt ? "Y" : "N";
        form.usernameMsgIsError = !res.rslt;
        form.idDupBtnDisabled = !!res.rslt;
    });
}

export function dupCheckEmail(emailId: string, emailDomain: string, form: UserSignupFormState, t: TFn): void {
    const eid = (emailId || "").trim();
    const ed = (emailDomain || "").trim();
    const em = `${eid}@${ed}`;
    if (!cF.regex.email.test(em)) {
        form.emailMsg = t("msg.user.signup.email.format");
        form.emailMsgIsError = true;
        form.emailDupPassed = "N";
        form.emailDupBtnDisabled = false;
        return;
    }

    cF.ajax.get(Url.USER_EMAIL_DUP_CHK_AJAX, { email: em }, function(res: AjaxResponse): void {
        form.emailMsg = res.message || "";
        form.emailDupPassed = res.rslt ? "Y" : "N";
        form.emailMsgIsError = !res.rslt;
        form.emailDupBtnDisabled = !!res.rslt;
    });
}

/** 인사 블록의 직급이 사원 코드일 때에만 노출해야 하는 수습 스위치(레거시 user_emplym_module 과 동치). */

export function isStaffRank(rankCd: string, staffCd: string): boolean {
    return rankCd === staffCd;
}

/** 코드 select 옵션 리스트 접근 헬퍼 */
export function listOf(codeLists: Record<string, CodeRow[]>, groupKey: string): CodeRow[] {
    const l = codeLists[groupKey];

    return Array.isArray(l) ? l : [];
}

export default {
    buildUserSignupFormData,
    validateUserSignupForm,
    validatePasswordRules,
    dupCheckUsername,
    dupCheckEmail,
    goLoginForm(): void {
        cF.ui.blockUIReplace(Url.APP_AUTH_LGN_FORM);
    },

    submitMultipart(st: Readonly<UserSignupFormState>, t: TFn): void {
        const v = validateUserSignupForm(st, t);

        if (v.ok === false) {
            void Swal.fire({ text: v.message });
            return;
        }

        void Swal.fire({
            text: t("view.cnfm.reg"),
            showCancelButton: true,
        }).then(function(result: SwalResult): void {
            if (!(result && (result as any).value))
                return;
            const fd = buildUserSignupFormData(st);
            void cF.$ajax.multipart(Url.USER_SIGNUP_REQUESTS, fd, function(res: AjaxResponse): void {

                void Swal.fire({ text: res.message }).then(function(): void {

                    if (res.rslt)
                        cF.ui.blockUIReplace(Url.APP_AUTH_LGN_FORM);

                });

            }, "block");
        });

    },

    confirmNavigateAway(t: TFn, onOk: () => void): void {
        void Swal.fire({
            text: t("msg.user.signup.return.confirm"),

            showCancelButton: true,
        }).then(function(result: SwalResult): void {
            if (result && (result as any).value)
                onOk();

        });

    },
};
