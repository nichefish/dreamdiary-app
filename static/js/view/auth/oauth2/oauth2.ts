/**
 * oauth2.ts
 * 로그인 비밀번호 변경 스크립트 모듈
 *
 * @namespace: dF.oauth2 (노출식 모듈 패턴)
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.oauth2 = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.oauth2.initialized) return;

            dF.oauth2.initialized = true;
            console.log("'dF.oauth2' module initialized.");
        },

        /**
         * 구글 OAuth2 인증 합업 호출
         */
        popupGoogle: function(): void {
            const url: string = Url.OAUTH2_GOOGLE;
            const popupNm: string = "Authorization";
            const options: string = 'width=540,height=720,top=0,left=270';
            const popup = cF.ui.openPopup(url, popupNm, options);
            if (popup) popup.focus();
        },

        /**
         * 네이버 OAuth2 인증 팝업 호출
         */
        popupNaver: function(): void {
            const url: string = Url.OAUTH2_NAVER;
            // const clientId: string = "ePxQazoQLPYbzR6oGVZw";
            // const redirectUri: string = encodeURIComponent(`http://localhost:18081/${Url.OAUTH2_NAVER_REDIRECT_URI}`);
            // url = url + `?response_type=code`
            //     + `&client_id=${clientId}`
            //     + `&redirect_uri=${redirectUri}`;
            const popupNm: string = "Authorization";
            const options: string = 'width=540,height=720,top=0,left=270';
            const popup = cF.ui.openPopup(url, popupNm, options);
            if (popup) popup.focus();
        },

        /**
         * URL에서 해시(fragment)를 파싱하여 객체로 반환합니다.
         * @returns {Record<string, any>} 해시(fragment)에서 파싱된 key-value 쌍을 담은 객체
         */
        getHashParam: function(): Record<string, string> {
            const hash: string = window.location.hash.substring(1); // #을 제외한 해시 부분
            const params: URLSearchParams = new URLSearchParams(hash);
            // 모든 파라미터를 객체로 변환
            const paramsObj: {} = {};
            params.forEach((value: string, key: string) => {
                paramsObj[key] = value;
            });
            return paramsObj;
        },

        /**
         * OAuth2 인증 후 처리 로직
         */
        handleOAuth2Redirect: function(): void {
            const hashParam: Record<string, string> = dF.oauth2.getHashParam();
            if (hashParam) {
                // 서버에 POST 요청 보내기
                const url: string = "/login/oauth2/code/naver";
                cF.ajax.post(url, { ...hashParam }, function(res: AjaxResponse): void {
                    if (res.rslt) {
                        console.log('Token successfully sent to server:');
                    }
                });
            } else {
                console.error('No access_token found in hash.');
            }
        }
    };
})();