/**
 * global.d.ts
 *
 * @author nichefish
 */

// TypeScript 선언
declare let cF: {
    util: {
        [key: string]: any;
    };
    [key: string]: any;
};
/**
 * Model : Spring 컨텍스트에서 Model에 추가된 요소들.
 */
declare const Model: {
    [key: string]: any;
};
/**
 * Message : Spring Boot에서 메세지 번들로 관리되는 Message 요소들.
 */
declare const AuthInfo: {
    userId: string,
    nickNm: string,
    email: string,
    proflImgUrl: string,
    isMngr: boolean,
    authList: Array
};
/**
 * Url : Spring Boot에서 정적으로 관리되는 Url 요소들.
 */
declare const Url: {
    [key: string]: string;
};
/**
 * Message : Spring Boot에서 메세지 번들로 관리되는 Message 요소들.
 */
declare const Message: {
    get: Function
};
/**
 * AjaxResponse : Spring Boot에서 Ajax 요청에 반환되는 응답 객체
 */
declare interface AjaxResponse {
    rslt: boolean;
    message: string;
    status: number;

    rsltObj?: object;
    rsltList?: object[];
    rsltMap?: Record<string, any>;
    rsltVal?: number;
    rsltStr?: string;
    url?: string;
}
