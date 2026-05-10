/**
 * messageHelper.ts
 * Vue ESM 컴포넌트의 글로벌 `Message` 결의 통일 헬퍼.
 *
 * 도입 배경(D, A-9 hotfix 가 드러낸 race):
 *   - Vue ESM 컴포넌트가 `Message.get(key)` 를 직접 호출하면 ESM 스코프의 식별자 결의에 의존하게 되어,
 *     일부 적재 순서/시점에서 `Message` 가 undefined 로 결의되어
 *     `Cannot read properties of undefined (reading 'get')` 가 first-render 시 터질 수 있다.
 *   - A-9 hotfix 에서 `JournalDayEntryTagListApp` 한 곳만 `window.Message?.get?.(...)` 패턴으로 봉합했으나,
 *     동일 함정이 다수 컴포넌트에 잠재 — 통일 헬퍼 1개로 사전 차단한다.
 *
 * 결의 규칙:
 *   - `window.Message` 또는 `globalThis.Message` 를 우선 결의해 ESM 스코프 식별자 결의 의존을 끊는다.
 *   - `Message.get` 이 함수가 아니면 폴백 반환(절대 throw 하지 않는다 — 렌더 안전성 우선).
 *   - `Message.get(key)` 가 null/undefined 를 반환하면 폴백 반환.
 *   - 폴백은 명시적 `fallback` 인자 → 없으면 `key` 자체(i18n 미적용 상태에서도 화면에 식별 가능한 문자열을 남김).
 *
 * 사용 패턴:
 *   - 단발 호출: `resolveMessage("view.cnfm.del")` — 함수 호출 시점에 결의(예: 이벤트 핸들러 안 Swal.fire 텍스트).
 *   - 한 번 결의 후 캐시: `data() { return { tooltipTitle: resolveMessage("view.tag.content-list") }; }`
 *     — Vue `data()` 가 마운트 시점에 한 번 평가되므로 결의 비용 0(매 렌더 평가 X).
 *
 * 주의:
 *   - 본 헬퍼는 사이드이펙트가 없는 순수 결의. 호출 실패는 console.warn 으로 로깅만 한다(룰: 핵심 분기 로그).
 *
 * @author nichefish
 */

/**
 * 글로벌 `Message.get(key)` 호출을 안전하게 결의한다.
 * ESM 스코프 식별자 결의 race(`Cannot read properties of undefined (reading 'get')`) 를 차단한다.
 *
 * @param key 메시지 키
 * @param fallback 결의 실패 시 반환값(미지정 시 `key` 자체 반환)
 * @returns 결의된 메시지 문자열 또는 폴백
 */
export function resolveMessage(key: string, fallback?: string): string {
    const w: any = typeof window !== "undefined"
        ? window
        : (typeof globalThis !== "undefined" ? globalThis : undefined);
    const ns: any = w?.Message;
    if (ns && typeof ns.get === "function") {
        try {
            const value: unknown = ns.get(key);
            if (value !== null && value !== undefined) return String(value);
        } catch (e) {
            // 핵심 분기 로그(룰): get 호출 자체가 throw 한 경우만 기록.
            console.warn("[resolveMessage] Message.get 호출 실패:", key, e);
        }
    }
    return fallback ?? key;
}
