/**
 * jrnl_todo_module.ts
 * 저널 할일 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlTodo = (function(): dfModule {
    return {
        initialized: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlTodo.initialized) return;

            // 목록 조회
            dF.JrnlTodo.yyMnthListAjax();
            
            dF.JrnlTodo.initialized = true;
            console.log("'dF.JrnlTodo' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_todo_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlTodoRegForm", dF.JrnlTodo.regAjax);
            // checkbox init
            cF.ui.chckboxLabel("imprtcYn", "중요//해당없음", "red//gray");
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_jrnlTodoCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlTodoCn", obj.cn || "");
            /* tagify */
            dF.JrnlTodo.tagify = cF.tagify.initWithCtgr("#jrnlTodoRegForm #tagListStr", dF.JrnlTodoTag.ctgrMap);
        },

        /**
         * 목록 조회
         */
        yyMnthListAjax: function(): void {
            const yyElmt: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const yy: string = yyElmt.value;
            if (cF.util.isEmpty(yy)) return;

            const mnthElmt: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const mnth: string = mnthElmt.value;
            if (cF.util.isEmpty(mnth)) return;

            const url: string = Url.JRNL_TODO_LIST_AJAX;
            const ajaxData: Record<string, any> = { yy, mnth };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                cF.handlebars.template(rsltList, "jrnl_todo_list");
            }, "block");
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            const yyElmt: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const yy: string = yyElmt.value;
            if (cF.util.isEmpty(yy)) return;

            const mnthElmt: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const mnth: string = mnthElmt.value;
            if (cF.util.isEmpty(mnth)) return;

            /* initialize form. */
            dF.JrnlTodo.initForm({ yy, mnth });
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlTodoCn").save();
            $("#jrnlTodoRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNoElmt: HTMLInputElement = document.querySelector("#jrnlTodoRegForm [name='postNo']") as HTMLInputElement;
            const isReg: boolean = postNoElmt?.value === "";
            Swal.fire({
                text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isReg ? Url.JRNL_TODO_REG_AJAX : Url.JRNL_TODO_MDF_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlTodoRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlTodo.yyMnthListAjax();

                            /* modal history pop */
                            ModalHistory.reset();
                        });
                }, "block");
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = Url.JRNL_TODO_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_diary_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = Url.JRNL_TODO_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlTodo.initForm(rsltObj);

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        delAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.JRNL_TODO_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo": postNo };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlTodo.yyMnthListAjax();

                            /* modal history pop */
                            ModalHistory.reset();
                        });
                }, "block");
            });
        },
    }
})();