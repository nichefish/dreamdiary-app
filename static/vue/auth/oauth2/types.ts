export type OAuth2PopupData = {
    providerKey: string;
    providerLabel: string;
    authenticatedText: string;
    errorMsg: string;
    returnMainLabel: string;
    returnMainTooltip: string;
};

export type OAuth2Actions = {
    init: () => void;
    popupGoogle: () => void;
    popupNaver: () => void;
    getHashParam: () => Record<string, string>;
    handleOAuth2Redirect: () => void;
    main: () => void;
};
