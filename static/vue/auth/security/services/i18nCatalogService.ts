/**
 * i18nCatalogService.ts
 * 정적 JSON 메시지 catalog를 읽고 키 기반 조회 함수를 제공합니다.
 *
 * @author nichefish
 */
type Catalog = Record<string, string>;

const catalogCache: Record<string, Promise<Catalog>> = {};

function normalizeLocale(locale: string): string {
    const normalized = (locale || "default").replace("_", "-").toLowerCase();
    if (normalized.startsWith("ko")) return "ko";
    if (normalized.startsWith("en")) return "en";
    return "default";
}

function findMessage(catalog: Catalog, key: string): string|null {
    const value = catalog[key];
    return typeof value === "string" ? value : null;
}

async function fetchCatalog(locale: string): Promise<Catalog> {
    if (!catalogCache[locale]) {
        catalogCache[locale] = fetch(`/i18n/${locale}.json`, {
            headers: { "Accept": "application/json" },
        }).then((response): Promise<Catalog>|Catalog => response.ok ? response.json() : {});
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
    t(catalog: Catalog, key: string): string {
        return findMessage(catalog, key) || key;
    },
};
