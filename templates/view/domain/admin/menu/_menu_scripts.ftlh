<#--
 * 메뉴(menu) : 스크립트 모듈 분리
 -->

<script>
    const Menu = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                cF.handlebars.template(obj, "menu_reg", "show");

                /* jquery validation */
                cF.validate.validateForm("#menuRegForm", Menu.regAjax);
                cF.validate.toUpperCase("#menuRegForm #menuLabel");
            },

            /**
             * Draggable 컴포넌트 init
             */
            initDraggable: function() {
                const keyExtractor = (item) => ({ "menuNo": Number($(item).attr("id")), "upperMenuNo": Number($(item).data("upper-menu-no")) });
                const url = "${Url.MENU_SORT_ORDR_AJAX!}";
                Menu.mainSwappable = cF.draggable.init("-main", keyExtractor, url);
                Menu.subSwappable = cF.draggable.init("-sub", keyExtractor, url);
            },

            /**
             * 등록 모달 호출
             */
            regModal: function(menuTyCd, upperMenuNo, upperMenuNm) {
                event.stopPropagation();

                const obj = { "menuTyCd": menuTyCd, "upperMenuNo": upperMenuNo, "upperMenuNm": upperMenuNm };
                /* initialize form. */
                Menu.initForm(obj);
            },

            /**
             * 아이콘 새로고침
             */
            refreshIcon: function() {
                const $iconClassElmt = $("#menuRegForm #icon");
                if (!$iconClassElmt.length) return;
                $("#menuRegForm #menu_icon_div").html($iconClassElmt.val());
            },

            /**
             * 하위메뉴 존재여부에 따라 url 영역 표시
             */
            toggleUrlSpan: function(obj) {
                const menuSubExtendTyCd = $(obj).val();
                if (menuSubExtendTyCd !== "NO_SUB") {
                    $("#url_div").addClass("d-none");
                } else {
                    $("#url_div").removeClass("d-none");
                }
            },

            /**
             * form submit
             */
            submit: function() {
                $("#menuRegForm").submit();
            },

            /**
             * 메뉴 정보 등록/수정 (Ajax)
             */
            regAjax: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.save"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    let isReg = $("#menuRegForm #menuNo").val() === "";
                    const url = isReg ? "${Url.MENU_REG_AJAX!}" : "${Url.MENU_MDF_AJAX!}";
                    const ajaxData = $("#menuRegForm").serializeArray();
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 수정 모달 호출
             */
            mdfModal: function(menuNo) {
                if (isNaN(menuNo)) return;

                const url = "${Url.MENU_DTL_AJAX!}";
                const ajaxData = { "menuNo": menuNo };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    /* initialize form. */
                    Menu.initForm(rsltObj);
                });
            },

            /**
             * 삭제 (Ajax)
             * @param {string|number} menuNo - 메뉴 번호.
             */
            delAjax: function(menuNo) {
                if (isNaN(menuNo)) return;

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.MENU_DEL_AJAX!}";
                    const ajaxData = { "menuNo": menuNo };
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 목록 검색
             */
            search: function() {
                $("#listForm #pageNo").val(1);
                cF.util.blockUISubmit("#listForm", "${Url.LOG_ACTVTY_LIST!}?actionTyCd=SEARCH");
            },

            /**
             * 엑셀 다운로드
             */
            xlsxDownload: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.download"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    cF.util.blockUIFileDownload();
                    $("#listForm").attr("action", "${Url.LOG_ACTVTY_LIST_XLSX_DOWNLOAD!}").submit();
                });
            },
        }
    })();
</script>