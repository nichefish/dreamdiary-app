/**
 * user_profl_module.ts
 *
 * @author nichefish
 */
const UserProfl: Module = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'UserProfl' module initialized.");
        },

        /** 프로필 정보 창 토글 */
        enableUserProfl: function(): void {
            // 영역 표시
            cF.handlebars.template({}, "user_profl");
            // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
            const $btn = $("#userProflBtn");
            $btn.removeClass("btn-primary").addClass("btn-danger");
            $btn.text("프로필 정보 삭제-");
            $btn.attr("onclick", "UserProfl.disableUserProfl();");
            $btn.attr("title", "사용자 프로필 정보를&#10;삭제합니다.");
            // 음력여부 클릭시 글씨 변경
            cF.util.chckboxLabel("lunarYn", "음력//양력", "blue//gray");
            // datepicker init
            cF.datepicker.singleDatePicker("#brthdy", "yyyy-MM-DD", $("#brthdy").val());
        },
        /** 프로필 정보 창 토글 */
        disableUserProfl: function(): void {
            // 영역 삭제
            $("#user_profl_div").empty();
            // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
            const $btn = $("#userProflBtn");
            $btn.removeClass("btn-danger").addClass("btn-primary");
            $btn.text("프로필 정보 추가+");
            $btn.attr("title", "사용자 프로필 정보를&#10;추가합니다.");
            $btn.attr("onclick", "UserProfl.enableUserProfl();");
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    UserProfl.init();
});