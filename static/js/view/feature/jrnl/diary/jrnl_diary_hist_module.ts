/**
 * jrnl_diary_hist_module.ts
 * 일기 히스토리 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDiaryHist = (function(): dfModule {
    return {
        initialized: false,

        init: async function(): Promise<void> {
            this.initialized = true;
        },

        /**
         * 히스토리 모달 호출
         * @param {string|number} postNo - 글 번호
         * @param {boolean} pushToHistory
         */
        historyModal: function(postNo: string|number, pushToHistory: boolean = true): void {
            if (isNaN(Number(postNo))) return;

            const self = this;
            const func: string = "historyModal";
            const args: any[] = [postNo];
            const url: string = cF.util.bindUrl(Url.JRNL_DIARY_HISTORIES, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const rsltObj: Record<string, any> = res.rsltObj ?? {};
                const viewModel: Record<string, any> = {
                    postNo: rsltObj.postNo,
                    stdrdDt: rsltObj.stdrdDt,
                    jrnlDtWeekDay: rsltObj.jrnlDtWeekDay,
                    history: rsltObj.history ?? {},
                    historyList: Array.isArray(rsltObj.historyList) ? rsltObj.historyList : [],
                };

                const openModals: NodeList = document.querySelectorAll('.modal.show');
                openModals.forEach((modal: Node): void => {
                    $(modal).modal('hide');
                });

                cF.handlebars.modal(viewModel, "jrnl_diary_history");
                if (pushToHistory) ModalHistory.push(self, func, args);
            });
        },

        restoreHistoryAjax: function(postNo: string|number, historyNo: string|number): void {
            if (isNaN(Number(postNo)) || isNaN(Number(historyNo))) return;

            Swal.fire({
                text: Message.get("txt.comm.restore"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JRNL_DIARY_HISTORY_RESTORE, { postNo, historyNo });
                cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            ModalHistory.reset();
                            dF.JrnlDiary.refresh();
                        });
                }, "block");
            });
        },

        deleteHistoryAjax: function(postNo: string|number, historyNo: string|number): void {
            if (isNaN(Number(postNo)) || isNaN(Number(historyNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JRNL_DIARY_HISTORY, { postNo, historyNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDiaryHist.historyModal(postNo, false);
                        });
                }, "block");
            });
        },
    }
})();
