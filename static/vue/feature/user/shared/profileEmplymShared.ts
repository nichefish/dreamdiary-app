/**
 * 사용자 프로필/인사 폼 공통 유틸
 *
 * UserFormApp, UserSignupApp, UserDetailApp, UserMyPageApp 에서 공통 사용.
 *
 * @author nichefish
 */
export function fallbackText(value: string | null | undefined, fallback: string = "-"): string {
    return value || fallback;
}

export type UserProfileReadRow = {
    label: string;
    value: string;
    lunarBadge?: boolean;
    lunarLabel?: string;
    asTextarea?: boolean;
};

export type UserEmplymReadRow = {
    label: string;
    value: string;
    asTextarea?: boolean;
};

type EmplymReadShape = {
    cmpyNm?: string | null;
    teamNm?: string | null;
    emplymNm?: string | null;
    rankNm?: string | null;
    rankCd?: string | null;
    apntcYn?: string | null;
    ecnyDt?: string | null;
    retireYn?: string | null;
    retireDt?: string | null;
    acntBank?: string | null;
    acntNo?: string | null;
};

/**
 * 사용자 소속(회사/팀/재직구분) 표시 문자열을 공통 규칙으로 조합한다.
 */
export function formatEmplymAffiliation(info: EmplymReadShape, separator: string = " / "): string {
    return `${fallbackText(info.cmpyNm || "")}${separator}${fallbackText(info.teamNm || "")}${separator}${fallbackText(info.emplymNm || "")}`;
}

/**
 * 사용자 직급 표시 문자열을 공통 규칙으로 조합한다.
 */
export function formatEmplymRank(info: EmplymReadShape, probationLabel: string): string {
    const isProbation = info.rankCd === "STAFF" && info.apntcYn === "Y";
    return `${fallbackText(info.rankNm || "")}${isProbation ? ` (${probationLabel})` : ""}`;
}

/**
 * 입/퇴사일 표시 문자열을 공통 규칙으로 조합한다.
 */
export function formatEmplymJoinRetire(info: EmplymReadShape, options?: { retirePrefix?: string }): string {
    const joinText = fallbackText(info.ecnyDt || "");
    const hasRetired = info.retireYn === "Y";
    if (!hasRetired)
        return joinText;
    const retireText = fallbackText(info.retireDt || "");
    const prefix = options?.retirePrefix ? `${options.retirePrefix}: ` : "";
    return `${joinText} / ${prefix}${retireText}`;
}

/**
 * 급여계좌 표시 문자열을 공통 규칙으로 조합한다.
 */
export function formatEmplymPayrollAccount(info: EmplymReadShape, separator: string = " / "): string {
    return `${fallbackText(info.acntBank || "")}${separator}${fallbackText(info.acntNo || "")}`;
}

/**
 * 사용자 권한 키를 아이콘 class 로 변환한다.
 *
 * @param roleKey 권한 키(MNGR/USER/DEV ...)
 * @returns 부트스트랩 아이콘 class 문자열
 */
export function resolveUserRoleIconClass(roleKey: string): string {
    // 관리자
    if (roleKey === "MNGR") return "bi bi-person-lines-fill text-info ms-1 opacity-75";
    // 사용자
    if (roleKey === "USER") return "bi bi-people-fill ms-1";
    // 개발자
    if (roleKey === "DEV") return "bi bi-person-fill-gear ms-1";
    return "bi bi-person ms-1";
}

/**
 * 프로필 생년월일 datepicker 초기화.
 *
 * @param selector datepicker 대상 selector
 */
export function initProfileBirthDatepicker(selector: string): void {
    const $fn = (globalThis as any).$;
    if (!$fn?.fn || typeof cF === "undefined")
        return;
    cF.datepicker.singleDatePicker(selector, "yyyy-MM-DD", $fn(selector).val());
}

/**
 * 인사 폼 공통 플러그인 초기화.
 *
 * @param options 대상 selector 옵션
 */
export function initEmplymFormPlugins(options: {
    phoneSelector: string;
    emailDomainSelectSelector: string;
    emailDomainInputSelector: string;
    joinDateSelector: string;
    retireDateSelector?: string;
    bindNamespace?: string;
}): void {
    const $fn = (globalThis as any).$;
    if (!$fn?.fn || typeof cF === "undefined")
        return;

    cF.validate.phoneNumber(options.phoneSelector);

    const ns = options.bindNamespace ? "." + options.bindNamespace : "";
    $fn(options.emailDomainSelectSelector).off(`change${ns}`).on(`change${ns}`, function(this: HTMLElement): void {
        $fn(options.emailDomainInputSelector).val($fn(this).val());
    });

    cF.datepicker.singleDatePicker(options.joinDateSelector, "yyyy-MM-DD", $fn(options.joinDateSelector).val());

    if (options.retireDateSelector)
        cF.datepicker.singleDatePicker(options.retireDateSelector, "yyyy-MM-DD", $fn(options.retireDateSelector).val());
}
