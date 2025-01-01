<#--
  -- 사용자 프로필 정보(user_profl) > 추가 Template
  -- @included_in : user_reg_form
  -->

<script>
    const UserProfl = (function() {
        return {
            /** 프로필 정보 창 토글 */
            enableUserProfl: function() {
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
                cF.datepicker.singleDatePicker("#brthdy", "yyyy-MM-DD", "${(user.profl.brthdy)!}");
            },
            /** 프로필 정보 창 토글 */
            disableUserProfl: function() {
                // 영역 삭제
                $("#user_profl_div").empty();
                // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
                let $btn = $("#userProflBtn");
                $btn.removeClass("btn-danger").addClass("btn-primary");
                $btn.text("프로필 정보 추가+");
                $btn.attr("title", "사용자 프로필 정보를&#10;추가합니다.");
                $btn.attr("onclick", "UserProfl.enableUserProfl();");
            },
        }
    })();
</script>

<#--include:: Handlebars template-->
<#include "/view/domain/user/profl/_user_profl_reg_template.hbs">
