export {};

declare global {
    interface Window {
        JOURNAL?: {
            stdrdDt?: string;
        };
        /**
         * 페이지 스크립트(Metronic 등)에서 주입하는 jQuery.
         * submit 시 네이티브 HTMLFormElement.submit() 대신 jQuery.trigger("submit") 이 필요한 경우에만 사용한다.
         */
        jQuery?: JQueryStatic;
    }
}
