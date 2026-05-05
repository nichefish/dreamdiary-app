/**
 * 계정 신청 화면 i18n (messages 번들 카탈로그)
 *
 * @author nichefish
 */
import i18nCatalogService from "../../../../auth/security/services/i18nCatalogService.js";

let cached: Record<string, string> | null = null;

export default {
    async load(locale: string): Promise<void> {
        cached = await i18nCatalogService.load(locale);
    },
    t(key: string): string {
        if (!cached)
            return key;
        return i18nCatalogService.t(cached, key);
    },
};
