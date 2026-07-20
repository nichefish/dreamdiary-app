import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import i18nCatalogService from "@/shared/utils/i18nCatalogService";

type Catalog = Record<string, string>;

const LS_LOCALE_KEY = "dreamdiary_locale";
/**
 * 지원 로케일 목록. 이 앱의 단일 원천이며, 로케일을 추가하려면 여기에 넣는다.
 * 코드 관리 다국어 입력의 선택지도 이 목록에서 파생된다(BASE_LOCALE 제외).
 */
export const SUPPORTED_LOCALES = ["ko", "en"] as const;
/** 기준 로케일. 번역이 없을 때 fallback 되는 언어. */
export const BASE_LOCALE = "ko";
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number];

function readStoredLocale(): SupportedLocale {
  const stored = window.localStorage.getItem(LS_LOCALE_KEY);
  return (SUPPORTED_LOCALES as readonly string[]).includes(stored ?? "") ? (stored as SupportedLocale) : BASE_LOCALE;
}

export const useLocaleStore = defineStore("locale", () => {
  const locale = ref<SupportedLocale>(readStoredLocale());
  const catalog = ref<Catalog>({});
  const loadedLocale = ref<SupportedLocale | null>(null);

  async function loadCatalog(): Promise<void> {
    const targetLocale = locale.value;
    catalog.value = await i18nCatalogService.load(targetLocale);
    loadedLocale.value = targetLocale;
  }

  /** 현재 locale의 catalog가 아직 준비되지 않았을 때만 로드한다. */
  async function ensureCatalog(): Promise<void> {
    if (loadedLocale.value === locale.value) return;
    console.info("[locale] loading catalog before route navigation", { locale: locale.value });
    await loadCatalog();
  }

  /** locale을 변경하고 catalog 재로드 및 axios Accept-Language 헤더를 갱신한다. */
  async function setLocale(next: SupportedLocale): Promise<void> {
    locale.value = next;
    window.localStorage.setItem(LS_LOCALE_KEY, next);
    axios.defaults.headers.common["Accept-Language"] = next;
    await loadCatalog();
    /* 서버가 사이드바 메뉴명/설명을 요청 locale 로 지역화하므로, 언어를 바꾸면 메뉴를 재조회해야
       새 언어가 사이드바에 반영된다. 서버 캐시는 locale 별로 분리되어 있어 재조회만으로 반영된다.
       모듈 로드 순서/순환 의존을 피하기 위해 지연 import 한다. */
    try {
      const { useMenuStore } = await import("@/shared/menu/stores/menu");
      await useMenuStore().refreshMenu();
    } catch (e) {
      console.error("[locale] 메뉴 재조회 실패", e);
    }
  }

  /** catalog에서 key에 해당하는 메시지를 반환한다. 없으면 key를 그대로 반환한다. */
  function t(key: string): string {
    return i18nCatalogService.t(catalog.value, key);
  }

  return { locale, catalog, loadCatalog, ensureCatalog, setLocale, t };
});
