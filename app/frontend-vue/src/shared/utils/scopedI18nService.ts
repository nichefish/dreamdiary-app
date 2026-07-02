import i18nCatalogService from "@/shared/utils/i18nCatalogService";

type Catalog = Record<string, string>;

export function createScopedI18n() {
  let messages: Catalog = {};

  return {
    async load(locale: string): Promise<void> {
      messages = await i18nCatalogService.load(locale);
    },
    t(key: string): string {
      return i18nCatalogService.t(messages, key);
    },
  };
}
