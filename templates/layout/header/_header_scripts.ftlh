<!--
 * 레이아웃 > 헤더 스크립트
 * @included_in : _header_base.ftlh
 -->

<script>
    /** 레이아웃 */
    const Header = (function() {
        return {
            /**
             * Header 객체 초기화
             */
            init: function() {
                // 사용자/관리자 전환
                Header.userModeSelectInit();
                // 현재 접속중인 메뉴 활성화
                Header.activateSelectedMenu();
            },

            /**
             * 사용자/관리자 전환 <select> init
             */
            userModeSelectInit: function() {
                const userModeSelect = document.querySelector("#userMode");
                if (!userModeSelect) return;
                userModeSelect.addEventListener("change", function() {
                    let mode = this.value;
                    if (mode === "${Constant.AUTH_USER!}") {
                        location.replace("${Url.MAIN!}");
                    } else if (mode === "${Constant.AUTH_MNGR!}") {
                        location.replace("${Url.ADMIN_MAIN!}");
                    }
                });
            },

            /**
             * 현재 접속중인 메뉴 활성화
             */
            activateSelectedMenu: function() {
                const upperMenuNo = "${upperMenuNo!}";
                const siteMenuNo = "${siteMenuNo!}";

                const siteAcsInfoExists = (upperMenuNo !== "") && (siteMenuNo !== "");
                if (!siteAcsInfoExists) return;

                document.getElementById(upperMenuNo)?.classList.add("hover");
                document.getElementById(siteMenuNo)?.classList.add("hover");
            },

            /**
             * 사이트맵 모달 호출
             */
            siteMapModal: function() {
                $("#sitemap_modal").modal("show");
            },

            /**
             * 내 정보 상세 페이지 이동
             */
            myInfoDtl: function() {
                location.replace("${Url.USER_MY_DTL!}");
            },

            /**
             * 로그아웃 처리
             */
            lgout: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.lgout"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    location.replace("${Url.AUTH_LGOUT!}");
                });
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Header.init();
    });
</script>