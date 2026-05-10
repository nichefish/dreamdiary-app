/**
 * scopedI18nService.ts
 * 화면 단위 i18n 상태(load/t)를 만드는 경량 팩토리.
 *
 * @author nichefish
 */
import i18nCatalogService from "./i18nCatalogService.js";

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

