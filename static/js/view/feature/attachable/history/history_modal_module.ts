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

        buildViewModel: function(contentType: string, id: string|number, rsltObj: Record<string, any>): Record<string, any> {
            return {
                ...rsltObj,
                contentType,
                id: rsltObj.id ?? Number(id),
                historyTriggeredAt: rsltObj.historyTriggeredAt ?? rsltObj.history?.historyTriggeredAt ?? "",
                historyList: Array.isArray(rsltObj.historyList) ? rsltObj.historyList : [],
            };
        },

        listUrl: function(contentType: string, id: string|number): string {
            return cF.util.bindUrl(Url.HISTORIES, { contentType, id });
        },

        restoreUrl: function(contentType: string, id: string|number, historyId: string|number): string {
            return cF.util.bindUrl(Url.HISTORY_RESTORE, { contentType, id, historyId });
        },

        deleteUrl: function(contentType: string, id: string|number, historyId: string|number): string {
            return cF.util.bindUrl(Url.HISTORY, { contentType, id, historyId });
        },

        clearUrl: function(contentType: string, id: string|number): string {
            return cF.util.bindUrl(Url.HISTORY_CLEAR, { contentType, id });
        },

        /**
         * open history modal
         * @param {string} contentType
         * @param {string|number} id
         * @param {boolean} [pushToHistory]
         */
        open: function(contentType: string, id: string|number, pushToHistory: boolean = true): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(id))) return;

            const self: dfModule = this as any;
            const func: string = "open";
            const args: any[] = [contentType, id];
            const url: string = dF.History.modal.listUrl(contentType, id);
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const rsltObj: Record<string, any> = res.rsltObj ?? {};
                const viewModel: Record<string, any> = dF.History.modal.buildViewModel(contentType, id, rsltObj);

                dF.History.modal.closeOpenModals();
                cF.handlebars.modal(viewModel, "attachable_history");
                if (pushToHistory) ModalHistory.push(self, func, args);
            });
        },

        /**
         * restore history item
         * @param {string} contentType
         * @param {string|number} id
         * @param {string|number} historyId
         */
        restore: function(contentType: string, id: string|number, historyId: string|number): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(id)) || isNaN(Number(historyId))) return;

            Swal.fire({
                text: Message.get("view.cnfm.restore"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = dF.History.modal.restoreUrl(contentType, id, historyId);
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
         * @param {string|number} id
         * @param {string|number} historyId
         */
        remove: function(contentType: string, id: string|number, historyId: string|number): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(id)) || isNaN(Number(historyId))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = dF.History.modal.deleteUrl(contentType, id, historyId);
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
         * @param {string|number} id
         */
        clear: function(contentType: string, id: string|number): void {
            if (cF.util.isEmpty(contentType) || isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = dF.History.modal.clearUrl(contentType, id);
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
