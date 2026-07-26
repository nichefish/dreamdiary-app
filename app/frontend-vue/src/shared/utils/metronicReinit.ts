import { nextTick } from "vue";
import { reinitializeComponents } from "@metronic/core/plugins/keenthemes";

/**
 * Vue DOM 반영 후 Metronic data-kt-* 컴포넌트(KTMenu 등)를 재바인딩한다.
 * 라우터 afterEach 만으로는 비동기 목록(저널 일자 카드·테이블 행 ⋯ 메뉴) 렌더 이전에 실행되어
 * 컨텍스트 메뉴가 동작하지 않는 경우가 있다.
 * `.table-responsive` 안의 행 메뉴는 트리거에 `data-kt-menu-overflow="true"` 를 둔다(body portal).
 */
export async function reinitMetronicAfterDom(): Promise<void> {
  await nextTick();
  setTimeout(() => {
    reinitializeComponents();
  }, 0);
}

/**
 * Metronic KTMenu 는 `document.body` 위임 클릭으로 연다.
 * 트리거에서 `stopPropagation` 하면 위임 핸들러에 도달하지 않아 메뉴가 열리지 않는다
 * (일자 카드는 트리거에 stop 이 없고, Bootstrap dropdown 은 버튼에 직접 붙어 달랐다).
 * 행 클릭(상세 이동)만 막을 때는 트리거 stop 대신 이 가드를 쓴다.
 */
export function isMetronicMenuEventTarget(target: EventTarget | null): boolean {
  if (!(target instanceof Element)) return false;
  return Boolean(target.closest('[data-kt-menu-trigger], [data-kt-menu="true"], .menu-sub'));
}
