/**
 * 레거시 cF 헬퍼(tagify) 연동
 *
 * 계정 신청 Vue 마운트/섹션 토글 후 DOM이 준비된 시점에서 호출한다.
 *
 * @author nichefish
 */
/**
 * 접속 허용 IP 입력에 tagify 초기화.
 *
 * @param selector 기본 레거시 `#allowedIpListStr`. 필요 시 `#user_signup_vue_root #allowedIpListStr` 등으로 특정한다.
 */
export function initAllowedIpTagify(selector: string = "#allowedIpListStr"): void {
    if (typeof cF === "undefined" || !(cF as any).tagify)
        return;
    try {
        (cF as any).tagify.init(selector);
    }
    catch (e) {
        console.error("[UserSignupApp] tagify init 실패.", selector, e);
    }
}
