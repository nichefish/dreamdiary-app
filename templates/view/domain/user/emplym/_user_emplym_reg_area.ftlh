<#--
 * 사용자 인사정보(user_emplym) > 추가 Template
 * @included_in : user_reg_form
 -->

<script>
    const UserEmplym = (function() {
        return {
            /** 직원정보 창 토글 */
            enableUserEmplym: function() {
                // 영역 표시
                cF.handlebars.template({}, "user_emplym");
                // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
                let $btn = $("#userEmplymBtn");
                $btn.removeClass("btn-primary").addClass("btn-danger");
                $btn.text("직원 인사정보 삭제-");
                $btn.attr("onclick", "UserEmplym.disableUserEmplym();");
                $btn.attr("title", "직원 인사정보를&#10;삭제합니다.");

                // 전화번호 형식 유효성 검사
                cF.validate.cttpc("#emplymCttpc");
                // 이메일 도메인 select시 자동입력
                $("#emplymEmailDomainSelect").on("change", function() {
                    $("#emplymEmailDomain").val($(this).val());
                });

                // 직급 변경시 '사원'일 때만 수습여부 영역 출력 + 수습여부 초기화
                $("#rankCd").on("change", function() {
                    if ($("#rankCd").val() === "STAFF") {
                        $("#apntcYnDiv").show();
                    } else {
                        $("#apntcYnDiv").hide();
                    }
                });
                // 수습여부 클릭시 글씨 변경
                cF.util.chckboxLabel("apntcYn", "수습//해당없음", "blue//gray");
                // 퇴사여부 클릭시 글씨 변경
                cF.util.chckboxLabel("retireYn", "퇴사//해당없음", "red//gray", function() {$(".retireDtDiv").show();}, function() {$(".retireDtDiv").hide();});

                cF.datepicker.singleDatePicker("#ecnyDt", "yyyy-MM-DD", "${(user.emplym.ecnyDt)!}");
                cF.datepicker.singleDatePicker("#retireDt", "yyyy-MM-DD", "${(user.emplym.retireDt)!}");
            },
            /** 직원정보 창 토글 */
            disableUserEmplym: function() {
                // 영역 삭제
                $("#user_emplym_div").empty();
                // 버튼 교체 :: 툴팁 작동 유지 위해 속성만 교체
                let $btn = $("#userEmplymBtn");
                $btn.removeClass("btn-danger").addClass("btn-primary");
                $btn.text("직원 인사정보 추가+");
                $btn.attr("title", "직원 인사정보를 &#10;추가합니다.");
                $btn.attr("onclick", "UserEmplym.enableUserEmplym();");
            },
        }
    })();
</script>

<#--include:: Handlebars template-->
<#include "/view/domain/user/emplym/_user_emplym_reg_template.hbs">
