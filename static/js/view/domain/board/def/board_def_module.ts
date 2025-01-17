/**
 * board_def_module.ts
 * 게시판 정의 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.BoardDef = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.BoardDef.initialized) return;

            dF.BoardDef.initialized = true;
            console.log("'dF.BoardDef' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "board_def_reg");

            /* jquery validation */
            cF.validate.validateForm("#boardDefRegForm", dF.BoardDef.regAjax);
            // checkbox init
            cF.util.chckboxLabel("useYn", "사용//미사용", "blue//gray");
            cF.validate.replaceBlankIfMatches("#boardDefRegForm .cddata", cF.regex.nonCd);
            cF.validate.onlyNum(".number");
        },

        /**
         * Draggable 컴포넌트 init
         */
        initDraggable: function(): void {
            const keyExtractor: Function = (item: HTMLElement) => ({ "boardDef": $(item).attr("id") });
            const url: string = Url.BOARD_DEF_SORT_ORDR_AJAX;
            dF.BoardDef.swappable = cF.draggable.init(keyExtractor, url);
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            /* initialize form. */
            dF.BoardDef.initForm();
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#boardDefRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_DEF_REG_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#boardDefRegForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 수정 모달 호출
         * @param {string} boardDef - 게시판 코드 (key).
         */
        mdfModal: function(boardDef: string): void {
            const url: string = Url.BOARD_DEF_DTL_AJAX;
            const ajaxData: Record<string, any> = { "boardDef": boardDef };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                /* initialize form. */
                dF.BoardDef.initForm(rsltObj);
            });
        },

        /**
         * 사용으로 변경 (Ajax)
         * @param {string} key - 게시판 코드 (key).
         */
        useAjax: function(key: string): void {
            Swal.fire({
                text: Message.get("view.cnfm.use"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_DEF_USE_AJAX;
                const ajaxData: Record<string, any> = { "boardDef": key };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 미사용으로 변경 (Ajax)
         * @param {string} key - 게시판 코드 (key).
         */
        unuseAjax: function(key: string): void {
            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_DEF_UNUSE_AJAX;
                const ajaxData: Record<string, any> = { "boardDef": key };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string} key - 게시판 코드 (key).
         */
        delAjax: function(key: string): void {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_DEF_DEL_AJAX;
                const ajaxData: Record<string, any> = { "boardDef": key };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },
    }
})();