/**
 * commentActionService.ts
 * 댓글 CRUD 액션 서비스
 *
 * 변경(D): comment_page_module.ts / comment_modal_module.ts 의 regAjax / mdfAjax / delAjax 로직을 서비스로 분리.
 *
 * @author nichefish
 */
declare const cF: any;
declare const Url: any;
declare const Swal: any;
declare const Message: any;

export default {
    /**
     * 댓글 등록 (페이지 레벨) — multipart POST
     * 기존 dF.Comment.page.regAjax() 대응.
     * @param {HTMLFormElement} formEl - 제출할 폼 엘리먼트.
     * @param {() => void} onSuccess - 성공 시 콜백 (목록 갱신 등).
     */
    reg(formEl: HTMLFormElement, onSuccess: () => void): void {
        (Swal as any).fire({
            text: (Message as any).get("view.cnfm.reg"),
            showCancelButton: true,
        }).then((result: { value: boolean }): void => {
            if (!result.value) return;
            const ajaxData: FormData = new FormData(formEl);
            (cF as any).$ajax.multipart((Url as any).COMMENTS, ajaxData, (res: any): void => {
                (Swal as any).fire({ text: res.message }).then((): void => {
                    if (res.rslt) onSuccess();
                });
            });
        });
    },

    /**
     * 댓글 수정 (페이지 레벨) — JSON POST
     * 기존 dF.Comment.page.mdfAjax() 대응.
     * @param {string|number} id - 수정할 댓글 번호.
     * @param {Record<string, unknown>} ajaxData - 폼에서 수집한 요청 데이터.
     * @param {() => void} onSuccess - 성공 시 콜백.
     */
    mdf(id: string | number, ajaxData: Record<string, unknown>, onSuccess: () => void): void {
        if (isNaN(Number(id))) return;
        (Swal as any).fire({
            text: (Message as any).get("view.cnfm.mdf"),
            showCancelButton: true,
        }).then((result: { value: boolean }): void => {
            if (!result.value) return;
            const url: string = (cF as any).util.bindUrl((Url as any).COMMENT, { id });
            (cF as any).$ajax.post(url, ajaxData, (res: any): void => {
                (Swal as any).fire({ text: res.message }).then((): void => {
                    if (res.rslt) onSuccess();
                });
            });
        });
    },

    /**
     * 댓글 삭제 — JSON POST
     * 기존 dF.Comment.page.delAjax() / dF.Comment.modal.delAjax() 대응.
     * @param {string|number} id - 삭제할 댓글 번호.
     * @param {Record<string, unknown>} extraData - 추가 파라미터 (actvtyCtgrCd 등).
     * @param {() => void} onSuccess - 성공 시 콜백.
     */
    del(id: string | number, extraData: Record<string, unknown>, onSuccess: () => void): void {
        if (isNaN(Number(id))) return;
        (Swal as any).fire({
            text: (Message as any).get("view.cnfm.del"),
            showCancelButton: true,
        }).then((result: { value: boolean }): void => {
            if (!result.value) return;
            const url: string = (cF as any).util.bindUrl((Url as any).COMMENT, { id });
            (cF as any).$ajax.post(url, extraData, (res: any): void => {
                (Swal as any).fire({ text: res.message }).then((): void => {
                    if (res.rslt) onSuccess();
                });
            });
        });
    },

    /**
     * 댓글 등록/수정 통합 (모달 레벨) — multipart POST
     * 기존 dF.Comment.modal.regAjax() 대응.
     * id 가 있으면 수정(COMMENT/{id}), 없으면 신규 등록(COMMENTS).
     * @param {string|number|undefined} id - 수정 시 댓글 번호 (신규 등록 시 undefined).
     * @param {HTMLFormElement} formEl - 제출할 폼 엘리먼트.
     * @param {() => void} onSuccess - 성공 시 콜백.
     */
    save(id: string | number | undefined, formEl: HTMLFormElement, onSuccess: () => void): void {
        const isMdf: boolean = id != null && id !== "" && !isNaN(Number(id));
        (Swal as any).fire({
            text: (Message as any).get("view.cnfm.save"),
            showCancelButton: true,
        }).then((result: { value: boolean }): void => {
            if (!result.value) return;
            const url: string = isMdf
                ? (cF as any).util.bindUrl((Url as any).COMMENT, { id })
                : (Url as any).COMMENTS;
            const ajaxData: FormData = new FormData(formEl);
            (cF as any).$ajax.multipart(url, ajaxData, (res: any): void => {
                (Swal as any).fire({ text: res.message }).then((): void => {
                    if (res.rslt) onSuccess();
                });
            });
        });
    },
};