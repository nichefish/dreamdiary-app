import { nextTick } from "vue";
import { reinitializeComponents } from "@metronic/core/plugins/keenthemes";

/**
 * Vue DOM 반영 후 Metronic data-kt-* 컴포넌트(KTMenu 등)를 재바인딩한다.
 * 라우터 afterEach 만으로는 비동기 목록(저널 일자 카드) 렌더 이전에 실행되어
 * 챕터/일자 ⋯ 컨텍스트 메뉴가 동작하지 않는 경우가 있다.
 */
export async function reinitMetronicAfterDom(): Promise<void> {
  await nextTick();
  setTimeout(() => {
    reinitializeComponents();
  }, 0);
}
