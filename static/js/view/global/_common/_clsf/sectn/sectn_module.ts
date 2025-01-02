/**
 * sectn_module.ts
 *
 * @author nichefish
 */
const Sectn: Module = (function(): Module {
    return {
        /**
         * Sectn 객체 초기화
         * @param {Object} options - 초기화 옵션 객체.
         * @param {Function} [options.refreshFunc] - 섹션 새로 고침에 사용할 함수 (선택적).
         */
        init: function({ refreshFunc }: { refreshFunc?: Function } = {}): void {
            console.log("'Sectn' module initialized.");

            if (refreshFunc !== undefined) Sectn.refreshFunc = refreshFunc;
        },

        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: any): void {
            /* show modal */
            cF.handlebars.template(obj, "sectn_reg_header");
            cF.handlebars.template(obj, "sectn_reg", "show");

            /* jquery validation */
            cF.validate.validateForm("#sectnRegForm", Sectn.regAjax);
            // checkbox init
            cF.util.chckboxLabel("deprcYn", "만료//해당없음", "red//gray");
            /* tagify */
            cF.tagify.initWithCtgr("#sectnRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_sectnCn');
            cF.tinymce.setContentWhenReady("tinymce_sectnCn", obj?.cn || "");
        },

        /**
         * Draggable 컴포넌트 init
         * @param {Object} options - 초기화 옵션 객체.
         * @param {Function} [options.refreshFunc] - 드래그 완료 후 호출할 함수 (선택적).
         */
        initDraggable: function({ refreshFunc }: { refreshFunc?: Function } = {}): void {
            const keyExtractor: Function = (item: HTMLElement) => ({ "postNo": $(item).attr("id") });
            const url: string = Url.SECTN_SORT_ORDR_AJAX;
            Sectn.swappable = cF.draggable.init(keyExtractor, url, refreshFunc);
        },

        /**
         * 전체 태그 목록 조회 (Ajax)
         * @param {Object} options - 조회 옵션 객체.
         * @param {string|number} options.refPostNo - 참조할 포스트 번호.
         * @param {string} options.refContentType - 참조할 콘텐츠 유형.
         */
        listAjax: function({ refPostNo, refContentType }): void {
            const url: string = Url.SECTN_LIST_AJAX;
            const ajaxData: Record<string, any> = { "refPostNo": refPostNo, "refContentType": refContentType };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 태그 상세 정보
                cF.handlebars.template(res.rsltList, "sectn_list");
            });
        },

        /**
         * 등록 모달 호출
         * @param {string|number} refPostNo - 참조할 포스트 번호.
         * @param {string} refContentType - 참조할 콘텐츠 유형.
         */
        regModal: function(refPostNo: string|number, refContentType: string): void {
            if (isNaN(Number(refPostNo)) || !refContentType) return;

            const obj = { "refPostNo": refPostNo, "refContentType": refContentType };
            /* initialize form. */
            Sectn.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            $("#sectnRegForm").submit();
        },

        /**
         * 단락 등록 (Ajax)
         */
        regAjax: function(): void {
            const isReg: boolean = $("#sectnRegForm #postNo").val() === "";
            Swal.fire({
                text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isReg ? Url.SECTN_REG_AJAX : Url.SECTN_MDF_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("sectnRegForm") as HTMLFormElement);
                cF.ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (Sectn.refreshFunc !== undefined) {
                                Sectn.refreshFunc();
                            } else {
                                cF.util.blockUIReload();
                            }
                        });
                });
            });
        },

        /**
         * 단락 수정 모달 호출
         * @param {string|number} postNo - 단락 번호.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.SECTN_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                Sectn.initForm(rsltObj);
            });
        },

        /**
         * 단락 삭제 (Ajax)
         * @param {string|number} postNo - 단락 번호.
         */
        delAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.SECTN_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo": postNo, "actvtyCtgrCd": "${actvtyCtgrCd!}" };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (Sectn.refreshFunc !== undefined) {
                                Sectn.refreshFunc();
                            } else {
                                cF.util.blockUIReload();
                            }
                        });
                });
            });
        },
    } as Module
})();