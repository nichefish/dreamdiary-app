type Catalog = Record<string, string>;

const catalogCache: Record<string, Promise<Catalog>> = {};

function normalizeLocale(locale: string): string {
  const normalized = (locale || "default").replace("_", "-").toLowerCase();
  if (normalized.startsWith("ko")) return "ko";
  if (normalized.startsWith("en")) return "en";
  return "default";
}

function findMessage(catalog: Catalog, key: string): string | null {
  const value = catalog[key];
  return typeof value === "string" ? value : null;
}

async function fetchCatalog(locale: string): Promise<Catalog> {
  if (!catalogCache[locale]) {
    catalogCache[locale] = fetch(`/i18n/${locale}.json`, {
      headers: { Accept: "application/json" },
    }).then((response): Promise<Catalog> | Catalog => response.ok ? response.json() : {});
  }
  return catalogCache[locale];
}

export default {
  async load(locale: string): Promise<Catalog> {
    const normalizedLocale = normalizeLocale(locale);
    const defaultCatalog = await fetchCatalog("default");
    if (normalizedLocale === "default") return defaultCatalog;

    const localeCatalog = await fetchCatalog(normalizedLocale);
    return { ...defaultCatalog, ...localeCatalog };
  },
  /**
   * catalog 메시지를 반환한다.
   * 변경 전: `value || key`라서 의도적 빈 문자열(예: en `date.suffix.after-month-number`)도 키로 떨어져
   * FILTER 월 버튼이 `7date.suffix.after-month-number`처럼 보였다.
   * 변경 후: 키가 있을 때(빈 문자열 포함)는 그 값을 쓰고, 키가 없을 때만 key를 반환한다.
   */
  t(catalog: Catalog, key: string): string {
    const value = findMessage(catalog, key);
    return value !== null ? value : key;
  },
};
