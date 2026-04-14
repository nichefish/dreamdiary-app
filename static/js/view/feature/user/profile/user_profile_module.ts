/**
 * user_profile_module.ts
 * 사용자 프로필 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.UserProfile = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.UserProfile.initialized) return;

            dF.UserProfile.initialized = true;
            console.log("'dF.UserProfile' module initialized.");
        },

        /** 프로필 정보 창 토글 */
        enableUserProfile: function(): void {
            // 영역 표시
            cF.handlebars.template({}, "user_profile");
            // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
            const $btn = $("#userProfileBtn");
            $btn.removeClass("btn-primary").addClass("btn-danger");
            $btn.text("프로필 정보 삭제-");
            $btn.attr("onclick", "dF.UserProfile.disableUserProfile();");
            $btn.attr("title", "사용자 프로필 정보를&#10;삭제합니다.");
            // 음력여부 클릭시 글씨 변경
            cF.ui.chckboxLabel("#lunarYn", "음력//양력", "blue//gray");
            // datepicker init
            cF.datepicker.singleDatePicker("#brthdy", "yyyy-MM-DD", $("#brthdy").val());
        },
        /** 프로필 정보 창 토글 */
        disableUserProfile: function(): void {
            // 영역 삭제
            $("#user_profile_div").empty();
            // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
            const $btn = $("#userProfileBtn");
            $btn.removeClass("btn-danger").addClass("btn-primary");
            $btn.text("프로필 정보 추가+");
            $btn.attr("title", "사용자 프로필 정보를&#10;추가합니다.");
            $btn.attr("onclick", "dF.UserProfile.enableUserProfile();");
        },
    }
})();
