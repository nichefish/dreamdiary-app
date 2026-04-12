/**
 * history_modal_module.ts
 * common history modal module
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
if (typeof dF.History === 'undefined') { dF.History = {} as any; }
dF.History.modal = (function(): Module {
    return {
        /**
         * close opened bootstrap modals before opening another modal
         */
        closeOpenModals: function(): void {
            const openModals: NodeList = document.querySelectorAll('.modal.show');
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');
            });
        },

        buildViewModel: function(contentType: string, postNo: string|number, rsltObj: Record<string, any>): Record<string, any> {
            return {
                ...rsltObj,
                contentType,
                postNo: rsltObj.postNo ?? Number(postNo),
                historyTriggeredAt: rsltObj.historyTriggeredAt ?? rsltObj.history?.historyTriggeredAt ?? "",
                historyList: Array.isArray(rsltObj.historyList) ? rsltObj.historyList : [],
            };
        },

        listUrl: function(contentType: string, postNo: string|number): string {
            return cF.util.bindUrl(Url.HISTORIES, { contentType, postNo });
        },

        restoreUrl: function(contentType: string, postNo: string|number, historyNo: string|number): string {
            return cF.util.bindUrl(Url.HISTORY_RESTORE, { contentType, postNo, historyNo });
        },

        deleteUrl: function(contentType: string, postNo: string|number, historyNo: string|number): string {
            return cF.util.bindUrl(Url.HISTORY, { contentType, postNo, historyNo });
        },

        clearUrl: function(contentType: string, postNo: string|number): string {
            return cF.util.bindUrl(Url.HISTORY_CLEAR, { contentType, postNo });
        },

        /**
         * open history modal
         * @param {string} contentType
         * @param {string|number} postNo
         * @param {boolean} [pushToHistory]
         */
        open: function(contentType: string, postNo: string|number, pushToHistory: boolean = true): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(postNo))) return;

            const self: dfModule = this as any;
            const func: string = "open";
            const args: any[] = [contentType, postNo];
            const url: string = dF.History.modal.listUrl(contentType, postNo);
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const rsltObj: Record<string, any> = res.rsltObj ?? {};
                const viewModel: Record<string, any> = dF.History.modal.buildViewModel(contentType, postNo, rsltObj);

                dF.History.modal.closeOpenModals();
                cF.handlebars.modal(viewModel, "clsf_history");
                if (pushToHistory) ModalHistory.push(self, func, args);
            });
        },

        /**
         * restore history item
         * @param {string} contentType
         * @param {string|number} postNo
         * @param {string|number} historyNo
         */
        restore: function(contentType: string, postNo: string|number, historyNo: string|number): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(postNo)) || isNaN(Number(historyNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.restore"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = dF.History.modal.restoreUrl(contentType, postNo, historyNo);
                cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            ModalHistory.reset();
                            location.reload();
                        });
                }, "block");
            });
        },

        /**
         * delete history item
         * @param {string} contentType
         * @param {string|number} postNo
         * @param {string|number} historyNo
         */
        remove: function(contentType: string, postNo: string|number, historyNo: string|number): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(postNo)) || isNaN(Number(historyNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = dF.History.modal.deleteUrl(contentType, postNo, historyNo);
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            ModalHistory.reset();
                            location.reload();
                        });
                }, "block");
            });
        },

        /**
         * clear all history items for a post
         * @param {string} contentType
         * @param {string|number} postNo
         */
        clear: function(contentType: string, postNo: string|number): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = dF.History.modal.clearUrl(contentType, postNo);
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            ModalHistory.reset();
                            location.reload();
                        });
                }, "block");
            });
        },
    };
})();
