/**
 * journalThreadListActionService.ts
 * 저널 스레드 목록 화면 액션 서비스.
 *
 * 변경: 공지{@link createNoticeListActions} · 일반게시판 목록 브리지와 동일 계약으로 목록 헤더 액션을 ESM 에서 제공한다.
 *   - 변경 전: `_journal_thread_list_header.ftlh` 가 `dF.JournalThread.search()` 등 classic 모듈을 직접 호출.
 *   - 변경 후: 헤더는 CustomEvent 디스패치만 하고 본 서비스가 동일 블록UI·Ajax 동작을 수행한다.
 *
 * 변경: `journal_thread_module.ts` 의 `myPaprList` 는 문자열에 `${authInfo.nickname!}` FTL 치환을 전제로 해 런타임에서 깨진다.
 *   본 서비스는 `AuthInfo` 글로벌을 사용한다(공지 목록 과 동일).
 *
 * 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
 *
 * @author nichefish
 */
import { resolveMessage } from "../../../../common/messageHelper.js";

export type JournalThreadListActions = {
    search: () => void;
    myPaprList: () => void;
    xlsxDownload: () => void;
    registForm: () => void;
    detailModal: (id: string | number) => void;
};

export default function createJournalThreadListActions(): JournalThreadListActions {
    return {
        search(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", `${Url.JOURNAL_THREAD_LIST!}?actionTyCd=SEARCH`);
        },
        myPaprList(): void {
            const param = `?searchType=nickname&searchKeyword=${AuthInfo.nickname!}&createdBy=${AuthInfo.username!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.ui.blockUIReplace(`${Url.JOURNAL_THREAD_LIST!}${param}`);
        },
        xlsxDownload(): void {
            /* 일반게시판 목록과 동등: 목록 폼 GET 제출로 스트림 다운로드를 시도한다(서버가 미지원이면 목록 새로고침에 귀결될 수 있음). */
            Swal.fire({
                text: resolveMessage("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.JOURNAL_THREAD_LIST!).submit();
            });
        },
        registForm(): void {
            cF.form.blockUISubmit("#procForm", Url.JOURNAL_THREAD_REGIST_FORM!);
        },
        detailModal(id: string | number): void {
            if (isNaN(Number(id))) return;
            const e = window.event as Event | undefined;
            if (e?.stopPropagation) e.stopPropagation();
            const url = cF.util.bindUrl(Url.JOURNAL_THREAD_API!, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const bridge = (window as any).JournalThreadDetailVueApp;
                if (bridge?.mounted && typeof bridge.open === "function") {
                    bridge.open(res.rsltObj);
                    return;
                }
                (window as any).JournalThreadDetailVueApp = {
                    ...(bridge || {}),
                    pendingPayload: res.rsltObj,
                };
            });
        },
    };
}
