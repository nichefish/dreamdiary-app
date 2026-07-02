import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import i18nCatalogService from "@/shared/utils/i18nCatalogService";

type Catalog = Record<string, string>;

const LS_LOCALE_KEY = "dreamdiary_locale";
const SUPPORTED = ["ko", "en"] as const;
export type SupportedLocale = (typeof SUPPORTED)[number];

function readStoredLocale(): SupportedLocale {
  const stored = window.localStorage.getItem(LS_LOCALE_KEY);
  return (SUPPORTED as readonly string[]).includes(stored ?? "") ? (stored as SupportedLocale) : "ko";
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
  }

  /** catalog에서 key에 해당하는 메시지를 반환한다. 없으면 key를 그대로 반환한다. */
  function t(key: string): string {
    return i18nCatalogService.t(catalog.value, key);
  }

  return { locale, catalog, loadCatalog, ensureCatalog, setLocale, t };
});
