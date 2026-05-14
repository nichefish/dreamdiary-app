/**
 * scheduleActionService.ts
 * 일정 CRUD 액션 서비스
 *
 * 변경(D): schedule_module.ts의 regAjax / delAjax 로직을 서비스로 분리.
 *
 * @author nichefish
 */
declare const cF: any;
declare const Url: any;
declare const Swal: any;
declare const Message: any;

export default {
    /**
     * 일정 등록/수정 (AJAX)
     * 기존 dF.Schedule.regAjax() 대응.
     * @param {Record<string, unknown>} ajaxData - 폼에서 수집한 요청 데이터.
     * @param {() => void} onSuccess - 성공 시 콜백 (달력 갱신 등).
     */
    reg(ajaxData: Record<string, unknown>, onSuccess: () => void): void {
        const isReg: boolean = !ajaxData["id"];
        (Swal as any).fire({
            text: (Message as any).get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
            showCancelButton: true,
        }).then((result: { value: boolean }): void => {
            if (!result.value) return;
            (cF as any).$ajax.post((Url as any).SCHEDULE_REG_AJAX, ajaxData, (res: any): void => {
                (Swal as any).fire({ text: res.message }).then((): void => {
                    if (res.rslt) onSuccess();
                });
            }, "block");
        });
    },

    /**
     * 일정 삭제 (AJAX)
     * 기존 dF.Schedule.delAjax() 대응.
     * @param {string|number} id - 삭제할 일정 번호.
     * @param {() => void} onSuccess - 성공 시 콜백 (달력 갱신 등).
     */
    del(id: string | number, onSuccess: () => void): void {
        if (isNaN(Number(id))) return;
        (Swal as any).fire({
            text: (Message as any).get("view.cnfm.del"),
            showCancelButton: true,
        }).then((result: { value: boolean }): void => {
            if (!result.value) return;
            (cF as any).$ajax.post((Url as any).SCHEDULE_DEL_AJAX, { id }, (res: any): void => {
                (Swal as any).fire({ text: res.message }).then((): void => {
                    if (res.rslt) onSuccess();
                });
            }, "block");
        });
    },
};
