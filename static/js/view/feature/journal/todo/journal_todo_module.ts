/**
 * journal_todo_module.ts
 * 저널 할일 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalTodo = (function(): dfModule {
    return {
        initialized: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalTodo.initialized) return;

            // 목록 조회
            dF.JournalTodo.yyMnthListAjax();
            
            dF.JournalTodo.initialized = true;
            console.log("'dF.JournalTodo' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_todo_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#journalTodoRegForm", dF.JournalTodo.regAjax);
            // checkbox init
            cF.ui.chckboxLabel("#journalTodoRegForm #imprtcYn", "중요//해당없음", "red//gray");
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_journalTodoCn');
            cF.tinymce.setContentWhenReady("tinymce_journalTodoCn", obj.content || "");
            /* tagify */
            dF.JournalTodo.tagify = cF.tagify.initWithCtgr("#journalTodoRegForm #tagListStr", dF.JournalTodoTag.ctgrMap);
        },

        /**
         * 목록 조회
         */
        yyMnthListAjax: function(): void {
            const yyElmt: HTMLSelectElement = document.querySelector("#journal_aside #yy");
            const yy: string = yyElmt.value;
            if (cF.util.isEmpty(yy)) return;

            const mnthElmt: HTMLSelectElement = document.querySelector("#journal_aside #mnth");
            const mnth: string = mnthElmt.value;
            if (cF.util.isEmpty(mnth)) return;

            const url: string = Url.JOURNAL_TODOS;
            const ajaxData: Record<string, any> = { yy, mnth };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                cF.handlebars.template(rsltList, "journal_todo_list");
            }, "block");
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            const yyElmt: HTMLSelectElement = document.querySelector("#journal_aside #yy");
            const yy: string = yyElmt.value;
            if (cF.util.isEmpty(yy)) return;

            const mnthElmt: HTMLSelectElement = document.querySelector("#journal_aside #mnth");
            const mnth: string = mnthElmt.value;
            if (cF.util.isEmpty(mnth)) return;

            /* initialize form. */
            dF.JournalTodo.initForm({ yy, mnth });
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_journalTodoCn").save();
            $("#journalTodoRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const id = cF.util.getInputValue("#journalTodoRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.binfUrl(Url.JOURNAL_TODO, { id }) : Url.JOURNAL_TODOS;
                const ajaxData: FormData = new FormData(document.getElementById("journalTodoRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalTodo.yyMnthListAjax();

                            /* modal history pop */
                            ModalHistory.reset();
                        });
                }, "block");
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} id - 글 번호.
         */
        dtlModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JOURNAL_TODO, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "journal_diary_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} id - 글 번호.
         */
        mdfModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JOURNAL_TODO, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JournalTodo.initForm(rsltObj);

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} id - 글 번호.
         */
        delAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JOURNAL_TODO, { id });
                cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalTodo.yyMnthListAjax();

                            /* modal history pop */
                            ModalHistory.reset();
                        });
                }, "block");
            });
        },
    }
})();
