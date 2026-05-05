import i18nCatalogService from "../../../auth/security/services/i18nCatalogService.js";

type I18nState = {
    messages: Record<string, string>;
};

const state: I18nState = { messages: {} };

export default {
    async load(locale: string): Promise<void> {
        state.messages = await i18nCatalogService.load(locale);
    },
    t(key: string): string {
        return i18nCatalogService.t(state.messages, key);
    },
};
