/**
 * journalDayTagProfileShell.ts
 * 저널 일자 태그 프로필 저장/삭제 서비스 — 전역 <code>dF.JournalDayTagProfileService</code>.
 *
 * 변경(P7): <code>static/js/view/feature/journal/day/tag/journal_day_tag_profile_service.ts</code> 본문을 Vue 축으로 이전.
 * 로드: <code>registerJournalDayShellServices</code> side-effect import.
 */
const __journalDayGlobal: any = typeof globalThis !== "undefined" ? globalThis : (window as any);
if (__journalDayGlobal.dF == null) {
    __journalDayGlobal.dF = {};
}
const dfNs: any = __journalDayGlobal.dF;
dfNs.JournalDayTagProfileService = (function(): Module {
    return {
        getProfileFormData: function(): Record<string, any> {
            const data: Record<string, any> = {};
            $("#tagProfileForm").serializeArray().forEach(function(item: JQuery.NameValuePair): void {
                data[item.name] = item.value;
            });
            return data;
        },

        submitProfile: function(): void {
            /* 변경 후(P7): <code>dF</code> 식별자 대신 <code>dfNs</code> — ES 모듈 스코프 및 TS <code>declare namespace dF</code> 충돌 회피. 호출 시점에는 이미 <code>dfNs.JournalDayTagProfileService</code> 할당 완료. */
            const ajaxData: Record<string, any> = dfNs.JournalDayTagProfileService.getProfileFormData();
            if (isNaN(Number(ajaxData.tagId)) || cF.util.isEmpty(ajaxData.contentType)) return;

            Swal.fire({
                text: Message.get(cF.util.isEmpty(ajaxData.id) ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { tagId: ajaxData.tagId });
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): boolean {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (!res.rslt) return;
                        $("#tag_profile_modal").modal("hide");
                        window.location.reload();
                    });
                    return res.rslt;
                });
            });
        },

        deleteProfileAjax: function(): void {
            const tagId: string = cF.util.getInputValue("#tagProfileForm [name='tagId']");
            const contentType: string = cF.util.getInputValue("#tagProfileForm [name='contentType']");
            if (isNaN(Number(tagId)) || cF.util.isEmpty(contentType)) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { tagId });
                cF.$ajax.delete(url, { contentType }, function(res: AjaxResponse): boolean {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (!res.rslt) return;
                        $("#tag_profile_modal").modal("hide");
                        window.location.reload();
                    });
                    return res.rslt;
                });
            });
        },
    };
})();

export {};
