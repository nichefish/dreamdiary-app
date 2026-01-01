/**
 * jrnl_sumry_module.ts
 * 저널 결산 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlSumry = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlSumry.initialized) return;

            dF.JrnlSumry.initialized = true;
            console.log("'dF.JrnlSumry' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_sumry_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlSumryRegForm", dF.JrnlSumry.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#jrnlSumryRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_jrnlSumryCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlSumryCn", obj.cn || "");
        },

        /**
         * 상세 화면으로 이동 (key로 조회)
         */
        listAjax: function(): void {
            const url: string = Url.JRNL_SUMRIES;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                cF.ui.closeModal();
                cF.handlebars.template(rsltList, "jrnl_sumry_list");
                KTMenu.createInstances();
            }, "block");
        },

        /**
         * 상세 화면으로 이동 (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        dtlView: function(yy: string|number): void {
            if (isNaN(Number(yy))) return;

            location.href = cF.util.bindUrl(Url.JRNL_SUMRY_VIEW, {yy}) + "?section=DIARY";
        },

        /**
         * 섹션 전환 이동 (년도로 조회)
         * @param {"DIARY"|"DREAM"} section - 조회 섹션
         */
        dtlViewWithSection: function(section: "DIARY"|"DREAM"): void {
            const yy: string = cF.util.getPathVariableFromUrl(/\/sumry\/(\d{4})(?:\.do)?$/);
            if (!yy) return console.warn("invalid yy.");

            location.href = cF.util.bindUrl(Url.JRNL_SUMRY_VIEW, {yy}) + `?section=${section}`;
        },

        /**
         * 상세 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        dtlAjax: function(yy: string|number): void {
            const url: string = cF.util.bindUrl(Url.JRNL_SUMRY, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.template(rsltObj, "jrnl_sumry_dtl");
            });
        },

        /**
         * 중요 일기 목록 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        getImprtcDiaryListAjax: function(yy: string|number): void {
            const url: string = cF.util.bindUrl(Url.JRNL_SUMRY_IMPRTC_DIARIES, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList = [] } = res;
                /* show modal */
                cF.handlebars.template(rsltList, "jrnl_sumry_imprtc_diary_list");
                document.querySelectorAll(".cn.collapsed").forEach(el => el.classList.remove("collapsed"));
            });
        },

        /**
         * 중요 꿈 목록 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        getImprtcDreamListAjax: function(yy: string|number): void {
            const url: string = cF.util.bindUrl(Url.JRNL_SUMRY_IMPRTC_DREAMS, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList = [] } = res;
                /* show modal */
                cF.handlebars.template(rsltList, "jrnl_sumry_imprtc_diary_list");
                document.querySelectorAll(".cn.collapsed").forEach(el => el.classList.remove("collapsed"));
            });
        },

        /**
         * 중요 일기 목록 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         * @param {"DAY"|"DIARY"|"DREAM"} type - 조회 타입
         */
        getTagListAjax: function(yy: string|number, type: "DAY"|"DIARY"|"DREAM"): void {
            const url: string = cF.util.bindUrl(Url.JRNL_SUMRY_TAGS, { yy }) + `?type=${type}`;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList = [] } = res;
                /* show modal */
                const lowerType = type.toLowerCase();
                cF.handlebars.template(rsltList, `jrnl_sumry_${lowerType}_tag_list`);
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_SUMRY_LIST);
        },

        /**
         * 특정 년도 결산 생성 (Ajax)
         * @param {string|number} yy - 결산을 생성할 년도.
         */
        makeYySumryAjax: function(yy: string|number): void {
            const yYElmt: HTMLSelectElement = document.querySelector("#listForm #yy");
            if (yy == null) yy = yYElmt.value;
            if (cF.util.isEmpty(yy)) {
                cF.ui.swalOrAlert("yy는 필수 항목입니다.");
                return;
            }
            const url: string = Url.JRNL_SUMRY_MAKE_AJAX;
            const ajaxData: Record<string, any> = { "yy": yy };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 전체 년도 결산 갱신 (Ajax)
         */
        makeTotalSumryAjax: function(): void {
            const url: string = Url.JRNL_SUMRY_MAKE_TOTAL_AJAX;
            cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 꿈 기록 완료 처리 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        comptAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_SUMRY_DREAM_COMPT_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlSumryCn").save();
            $("#jrnlSumryRegForm").submit();
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} yy - 년도.
         */
        mdfModal: function(yy: string|number): void {
            if (isNaN(Number(yy))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_SUMRY, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlSumry.initForm(rsltObj);
            });
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.JRNL_SUMRY_REG_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlSumryRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            cF.ui.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();