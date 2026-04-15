/**
 * related_content_module.ts
 * related content ui module
 *
 * @author nichefish
 */
// @ts-ignore
if (typeof dF === 'undefined') { var dF = {} as any; }

type RelatedSource = {
    id: number;
    contentType: string;
};

type RelatedTargetItem = {
    id: number;
    contentType: string;
    title: string;
    stdrdDt: string;
    cn: string;
};

type RelatedContentPayload = {
    srcId: number;
    srcContentType: string;
    targetId: number;
    targetContentType: string;
    relationType: string;
    reason: string;
};

type RelatedContentItem = Record<string, any>;

dF.RelatedContent = (function(): dfModule {
    return {
        initialized: false,
        popupSearchMap: {} as Record<string, RelatedTargetItem>,

        CONTENT_TYPE_LABEL_MAP: {
            JOURNAL_DIARY: "일기",
            JOURNAL_DREAM: "꿈"
        } as Record<string, string>,

        init: function(): void {
            if (dF.RelatedContent.initialized) return;

            dF.RelatedContent.initialized = true;
            console.log("'dF.RelatedContent' module initialized.");
        },

        getSection: function(elmt: Element | EventTarget | null): HTMLElement | null {
            if (!(elmt instanceof HTMLElement)) return null;
            return elmt.closest(".related-content-box");
        },

        getSource: function(elmt: Element | EventTarget | null): RelatedSource | null {
            if (!(elmt instanceof HTMLElement)) return null;

            const id: number = Number(elmt.dataset.id);
            const contentType: string = String(elmt.dataset.contentType ?? "").trim();
            return dF.RelatedContent.toSource(contentType, id);
        },

        toSource: function(contentType: string, id: number): RelatedSource | null {
            const normalizedContentType: string = String(contentType ?? "").trim();
            const normalizedId: number = Number(id);

            if (!Number.isInteger(normalizedId) || normalizedId <= 0 || normalizedContentType.length === 0) return null;

            return {
                id: normalizedId,
                contentType: normalizedContentType
            };
        },

        getSourceKey: function(source: RelatedSource): string {
            return `${source.contentType}:${source.id}`;
        },

        getAnchorsBySource: function(source: RelatedSource): HTMLElement[] {
            const selector: string = `.related-content-anchor[data-id="${source.id}"][data-content-type="${source.contentType}"]`;
            return Array.from(document.querySelectorAll(selector));
        },

        getSectionsBySource: function(source: RelatedSource): HTMLElement[] {
            const selector: string = `.related-content-box[data-id="${source.id}"][data-content-type="${source.contentType}"]`;
            return Array.from(document.querySelectorAll(selector));
        },

        getListElmt: function(section: HTMLElement | null): HTMLElement | null {
            if (!(section instanceof HTMLElement)) return null;
            return section.querySelector(".related-content-list");
        },

        ensureSection: function(anchor: HTMLElement, source: RelatedSource): HTMLElement {
            const nextElmt = anchor.nextElementSibling;
            if (nextElmt instanceof HTMLElement && nextElmt.classList.contains("related-content-box")) {
                nextElmt.dataset.id = String(source.id);
                nextElmt.dataset.contentType = source.contentType;
                return nextElmt;
            }

            const section: HTMLDivElement = document.createElement("div");
            section.className = "related-content-box mt-4 pt-4 border-top border-gray-200";
            section.dataset.id = String(source.id);
            section.dataset.contentType = source.contentType;
            section.innerHTML = [
                '<div class="d-flex align-items-center gap-2 mb-3 text-gray-700">',
                '    <i class="bi bi-link-45deg"></i>',
                '    <span class="fw-semibold">관련 글</span>',
                '</div>',
                '<div class="related-content-list"></div>'
            ].join("");

            anchor.insertAdjacentElement("afterend", section);
            return section;
        },

        ensureSectionsBySource: function(source: RelatedSource): HTMLElement[] {
            return dF.RelatedContent.getAnchorsBySource(source).map(function(anchor: HTMLElement): HTMLElement {
                return dF.RelatedContent.ensureSection(anchor, source);
            });
        },

        removeSourceSections: function(source: RelatedSource): void {
            dF.RelatedContent.getSectionsBySource(source).forEach(function(section: HTMLElement): void {
                section.remove();
            });
        },

        upsertItemBySource: function(source: RelatedSource, item: RelatedContentItem): void {
            const relatedContentId: number = Number(item?.relatedContentId ?? 0);
            if (!Number.isInteger(relatedContentId) || relatedContentId <= 0) return;

            dF.RelatedContent.ensureSectionsBySource(source).forEach(function(section: HTMLElement): void {
                const listElmt: HTMLElement | null = dF.RelatedContent.getListElmt(section);
                if (!(listElmt instanceof HTMLElement)) return;

                const selector: string = `.related-content-item[data-related-content-id="${relatedContentId}"]`;
                const itemHtml: string = dF.RelatedContent.renderItem(item);
                const existedElmt = listElmt.querySelector(selector);

                if (existedElmt instanceof HTMLElement) {
                    existedElmt.outerHTML = itemHtml;
                    return;
                }

                listElmt.insertAdjacentHTML("beforeend", itemHtml);
            });
        },

        removeItemBySource: function(source: RelatedSource, relatedContentId: number): void {
            dF.RelatedContent.getSectionsBySource(source).forEach(function(section: HTMLElement): void {
                section.querySelectorAll(`.related-content-item[data-related-content-id="${relatedContentId}"]`).forEach(function(itemElmt: Element): void {
                    itemElmt.remove();
                });

                const listElmt: HTMLElement | null = dF.RelatedContent.getListElmt(section);
                if (!(listElmt instanceof HTMLElement)) return;
                if (listElmt.querySelector(".related-content-item")) return;

                section.remove();
            });
        },

        renderItem: function(item: RelatedContentItem): string {
            const relatedContentId: number = Number(item?.relatedContentId ?? 0);
            const targetId: number = Number(item?.targetId ?? 0);
            const targetContentType: string = String(item?.targetContentType ?? "").trim();
            const contentTypeLabel: string = dF.RelatedContent.CONTENT_TYPE_LABEL_MAP[targetContentType] ?? targetContentType;
            const targetTitle: string = dF.RelatedContent.escapeHtml(item?.targetTitle || `#${targetId}`);
            const reason: string = String(item?.reason ?? "").trim();

            return [
                `<div class="related-content-item rounded border border-gray-300 bg-light px-4 py-3 mb-3" data-related-content-id="${relatedContentId}">`,
                '    <div class="d-flex align-items-start justify-content-between gap-3 flex-wrap">',
                '        <div class="flex-grow-1">',
                `            <div class="text-muted fs-8 mb-2">${contentTypeLabel} #${targetId}</div>`,
                `            <div class="fw-semibold text-gray-900 fs-6">${targetTitle}</div>`,
                reason.length > 0
                    ? `            <div class="text-muted fs-7 mt-2">${dF.RelatedContent.escapeHtml(reason)}</div>`
                    : '',
                '        </div>',
                '        <div class="d-flex align-items-center gap-2">',
                `            <button type="button" class="btn btn-xxs btn-light-primary btn-outlined" onclick="dF.RelatedContent.openTarget('${targetContentType}', ${targetId});">열기</button>`,
                `            <button type="button" class="btn btn-xxs btn-light-danger btn-outlined" onclick="dF.RelatedContent.deleteAjax(${relatedContentId}, this);">삭제</button>`,
                '        </div>',
                '    </div>',
                '</div>'
            ].filter(Boolean).join("");
        },

        openAddModal: function(btn: HTMLElement): void {
            const section: HTMLElement | null = dF.RelatedContent.getSection(btn);
            const source: RelatedSource | null = dF.RelatedContent.getSource(section);
            if (!source) return;

            dF.RelatedContent.openAddModalWithSource(source);
        },

        openAddModalBySource: function(contentType: string, id: number): void {
            const source: RelatedSource | null = dF.RelatedContent.toSource(contentType, id);
            if (!source) return;

            dF.RelatedContent.openAddModalWithSource(source);
        },

        openAddModalWithSource: function(source: RelatedSource): void {
            const sourceLabel: string = dF.RelatedContent.CONTENT_TYPE_LABEL_MAP[source.contentType] ?? source.contentType;
            const defaultTargetType: string = source.contentType === "JOURNAL_DIARY" ? "JOURNAL_DREAM" : "JOURNAL_DIARY";
            dF.RelatedContent.popupSearchMap = {};

            Swal.fire({
                title: "관련 글 추가",
                html: dF.RelatedContent.buildAddModalHtml(sourceLabel, source.id, defaultTargetType),
                width: 820,
                showCancelButton: true,
                confirmButtonText: "저장",
                cancelButtonText: "취소",
                focusConfirm: false,
                didOpen: function(): void {
                    const popup = Swal.getPopup();
                    if (!(popup instanceof HTMLElement)) return;

                    const keywordInput = popup.querySelector("#relatedTargetKeyword");
                    keywordInput?.addEventListener("keydown", function(event: Event): void {
                        const keyboardEvent = event as KeyboardEvent;
                        if (keyboardEvent.key !== "Enter") return;

                        keyboardEvent.preventDefault();
                        dF.RelatedContent.searchTargetsInPopup();
                    });
                },
                preConfirm: (): false | RelatedContentPayload => {
                    const popup = Swal.getPopup();
                    if (!(popup instanceof HTMLElement)) return false;

                    const targetContentType: string = String((popup.querySelector("#relatedTargetContentType") as HTMLSelectElement | null)?.value ?? "").trim();
                    const targetId: number = Number((popup.querySelector("#relatedTargetId") as HTMLInputElement | null)?.value ?? 0);
                    const relationType: string = String((popup.querySelector("#relatedRelationType") as HTMLSelectElement | null)?.value ?? "").trim();
                    const reason: string = String((popup.querySelector("#relatedReason") as HTMLTextAreaElement | null)?.value ?? "").trim();

                    if (targetContentType.length === 0) {
                        Swal.showValidationMessage("대상 글 유형을 선택해 주세요.");
                        return false;
                    }
                    if (!Number.isInteger(targetId) || targetId <= 0) {
                        Swal.showValidationMessage("검색 결과에서 연결할 글을 선택해 주세요.");
                        return false;
                    }
                    if (targetContentType === source.contentType && targetId === source.id) {
                        Swal.showValidationMessage("현재 글 자신과는 연결할 수 없습니다.");
                        return false;
                    }
                    if (relationType.length === 0) {
                        Swal.showValidationMessage("관련 유형을 선택해 주세요.");
                        return false;
                    }

                    return {
                        srcId: source.id,
                        srcContentType: source.contentType,
                        targetId,
                        targetContentType,
                        relationType,
                        reason
                    };
                }
            }).then(function(result: SwalResult): void {
                if (!result.isConfirmed || !result.value) return;

                dF.RelatedContent.saveAjax(source, result.value as RelatedContentPayload);
            });
        },

        buildAddModalHtml: function(sourceLabel: string, sourceId: number, defaultTargetType: string): string {
            return [
                '<div class="text-start">',
                `  <div class="rounded bg-light-primary text-primary px-4 py-3 fs-7 mb-4">현재 글: ${dF.RelatedContent.escapeHtml(sourceLabel)} #${sourceId}</div>`,
                '  <div class="row g-3 mb-4">',
                '    <div class="col-md-4">',
                '      <label for="relatedRelationType" class="form-label fw-semibold text-gray-700">관련 유형</label>',
                '      <select id="relatedRelationType" class="form-select form-select-solid">',
                '        <option value="REFERENCE">참조</option>',
                '        <option value="EXTENSION">확장</option>',
                '        <option value="PARALLEL">병렬</option>',
                '        <option value="CAUSE">원인</option>',
                '      </select>',
                '    </div>',
                '    <div class="col-md-4">',
                '      <label for="relatedTargetContentType" class="form-label fw-semibold text-gray-700">대상 글 유형</label>',
                '      <select id="relatedTargetContentType" class="form-select form-select-solid" onchange="dF.RelatedContent.resetPopupSelection();">',
                `        <option value="JOURNAL_DIARY" ${defaultTargetType === "JOURNAL_DIARY" ? "selected" : ""}>일기</option>`,
                `        <option value="JOURNAL_DREAM" ${defaultTargetType === "JOURNAL_DREAM" ? "selected" : ""}>꿈</option>`,
                '      </select>',
                '    </div>',
                '    <div class="col-md-4 d-flex align-items-end">',
                '      <button type="button" class="btn btn-light-info w-100" onclick="dF.RelatedContent.searchTargetsInPopup();">검색</button>',
                '    </div>',
                '  </div>',
                '  <div class="mb-4">',
                '    <label for="relatedTargetKeyword" class="form-label fw-semibold text-gray-700">검색 키워드</label>',
                '    <input type="text" id="relatedTargetKeyword" class="form-control form-control-solid" maxlength="100" placeholder="제목이나 내용 키워드를 입력해 주세요." />',
                '    <div class="text-muted fs-8 mt-2">검색 결과를 클릭하면 연결 대상이 선택됩니다.</div>',
                '  </div>',
                '  <input type="hidden" id="relatedTargetId" value="" />',
                '  <div id="relatedTargetSelected" class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4">아직 선택한 글이 없습니다.</div>',
                '  <div id="relatedTargetResults" class="mb-4"></div>',
                '  <div>',
                '    <label for="relatedReason" class="form-label fw-semibold text-gray-700">메모</label>',
                '    <textarea id="relatedReason" class="form-control form-control-solid" rows="3" maxlength="255" placeholder="왜 연결하는지 간단히 적어 둘 수 있습니다."></textarea>',
                '  </div>',
                '</div>'
            ].join("");
        },

        resetPopupSelection: function(): void {
            const popup = Swal.getPopup();
            if (!(popup instanceof HTMLElement)) return;

            const targetIdInput = popup.querySelector("#relatedTargetId");
            const selectedElmt = popup.querySelector("#relatedTargetSelected");
            const resultsElmt = popup.querySelector("#relatedTargetResults");

            if (targetIdInput instanceof HTMLInputElement) targetIdInput.value = "";
            if (selectedElmt instanceof HTMLElement) {
                selectedElmt.className = "rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4";
                selectedElmt.textContent = "아직 선택한 글이 없습니다.";
            }
            if (resultsElmt instanceof HTMLElement) resultsElmt.innerHTML = "";

            dF.RelatedContent.popupSearchMap = {};
        },

        searchTargetsInPopup: function(): void {
            const popup = Swal.getPopup();
            if (!(popup instanceof HTMLElement)) return;

            const targetContentType: string = String((popup.querySelector("#relatedTargetContentType") as HTMLSelectElement | null)?.value ?? "").trim();
            const keyword: string = String((popup.querySelector("#relatedTargetKeyword") as HTMLInputElement | null)?.value ?? "").trim();
            const resultsElmt = popup.querySelector("#relatedTargetResults");

            if (!(resultsElmt instanceof HTMLElement)) return;

            if (keyword.length === 0) {
                resultsElmt.innerHTML = '<div class="rounded border border-dashed border-warning px-4 py-3 text-warning fs-7">검색어를 입력해 주세요.</div>';
                return;
            }

            resultsElmt.innerHTML = [
                '<div class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7">',
                '<span class="spinner-border spinner-border-sm me-2"></span>',
                '검색 중입니다.',
                '</div>'
            ].join("");

            const url: string = targetContentType === "JOURNAL_DREAM" ? Url.JOURNAL_DREAMS : Url.JOURNAL_DIARIES;
            const ajaxData: Record<string, any> = {
                searchKeywords: keyword,
                pageSize: 8,
                sort: "DESC"
            };

            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    resultsElmt.innerHTML = '<div class="rounded border border-dashed border-danger px-4 py-3 text-danger fs-7">검색에 실패했습니다.</div>';
                    return;
                }

                const list: RelatedTargetItem[] = (Array.isArray(res.rsltList) ? res.rsltList : []).map(function(item: Record<string, any>): RelatedTargetItem {
                    return {
                        id: Number(item?.id ?? 0),
                        contentType: String(item?.contentType ?? targetContentType),
                        title: String(item?.title ?? "").trim(),
                        stdrdDt: String(item?.stdrdDt ?? "").trim(),
                        cn: String(item?.cn ?? item?.markdownCn ?? "").trim()
                    };
                }).filter(function(item: RelatedTargetItem): boolean {
                    return Number.isInteger(item.id) && item.id > 0;
                });

                dF.RelatedContent.renderPopupSearchResults(list);
            });
        },

        renderPopupSearchResults: function(list: RelatedTargetItem[]): void {
            const popup = Swal.getPopup();
            if (!(popup instanceof HTMLElement)) return;

            const resultsElmt = popup.querySelector("#relatedTargetResults");
            const selectedId: number = Number((popup.querySelector("#relatedTargetId") as HTMLInputElement | null)?.value ?? 0);
            if (!(resultsElmt instanceof HTMLElement)) return;

            dF.RelatedContent.popupSearchMap = {};

            if (!Array.isArray(list) || list.length === 0) {
                resultsElmt.innerHTML = '<div class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7">검색 결과가 없습니다.</div>';
                return;
            }

            list.forEach(function(item: RelatedTargetItem): void {
                dF.RelatedContent.popupSearchMap[dF.RelatedContent.getTargetKey(item.contentType, item.id)] = item;
            });

            resultsElmt.innerHTML = list.map(function(item: RelatedTargetItem): string {
                const key: string = dF.RelatedContent.getTargetKey(item.contentType, item.id);
                const selectedClass: string = selectedId === item.id ? "border-primary bg-light-primary" : "border-gray-300";
                const title: string = dF.RelatedContent.escapeHtml(item.title || `#${item.id}`);
                const excerpt: string = dF.RelatedContent.escapeHtml(dF.RelatedContent.toPreviewText(item.cn));

                return [
                    `<button type="button" class="btn w-100 text-start rounded border ${selectedClass} px-4 py-3 mb-3 related-target-item" data-related-key="${key}" onclick="dF.RelatedContent.selectTargetByKey('${key}');">`,
                    `  <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-2"><span class="fw-semibold text-gray-900">${title}</span><span class="text-muted fs-8">#${item.id}${item.stdrdDt ? ` | ${dF.RelatedContent.escapeHtml(item.stdrdDt)}` : ""}</span></div>`,
                    `  <div class="text-muted fs-7">${excerpt || "미리보기가 없습니다."}</div>`,
                    '</button>'
                ].join("");
            }).join("");
        },

        selectTargetByKey: function(key: string): void {
            const popup = Swal.getPopup();
            if (!(popup instanceof HTMLElement)) return;

            const item: RelatedTargetItem | undefined = dF.RelatedContent.popupSearchMap[key];
            if (!item) return;

            const targetContentTypeSelect = popup.querySelector("#relatedTargetContentType");
            const targetIdInput = popup.querySelector("#relatedTargetId");
            const selectedElmt = popup.querySelector("#relatedTargetSelected");

            if (targetContentTypeSelect instanceof HTMLSelectElement) targetContentTypeSelect.value = item.contentType;
            if (targetIdInput instanceof HTMLInputElement) targetIdInput.value = String(item.id);
            if (selectedElmt instanceof HTMLElement) {
                selectedElmt.className = "rounded border border-primary bg-light-primary px-4 py-3 text-start mb-4";
                selectedElmt.innerHTML = [
                    `<div class="fw-semibold text-primary mb-1">${dF.RelatedContent.escapeHtml(item.title || `#${item.id}`)}</div>`,
                    `<div class="text-muted fs-7">${dF.RelatedContent.CONTENT_TYPE_LABEL_MAP[item.contentType] ?? item.contentType} #${item.id}${item.stdrdDt ? ` | ${dF.RelatedContent.escapeHtml(item.stdrdDt)}` : ""}</div>`
                ].join("");
            }

            dF.RelatedContent.renderPopupSearchResults(Object.values(dF.RelatedContent.popupSearchMap));
        },

        getTargetKey: function(contentType: string, id: number): string {
            return `${contentType}:${id}`;
        },

        toPreviewText: function(value: string): string {
            const text: string = String(value ?? "")
                .replace(/<[^>]+>/g, " ")
                .replace(/\s+/g, " ")
                .trim();

            if (text.length <= 120) return text;
            return text.substring(0, 120) + "...";
        },

        saveAjax: function(source: RelatedSource, payload: RelatedContentPayload): void {
            const url: string = cF.util.bindUrl(Url.RELATEDS, {
                contentType: source.contentType,
                id: source.id
            });

            cF.ajax.post(url, payload, function(res: AjaxResponse): void {
                const savedItem: RelatedContentItem | null = res.rsltObj && typeof res.rsltObj === "object" ? res.rsltObj as RelatedContentItem : null;

                Swal.fire({ text: res.message }).then(function(): void {
                    if (!res.rslt || !savedItem) return;
                    dF.RelatedContent.upsertItemBySource(source, savedItem);
                });
            });
        },

        deleteAjax: function(relatedContentId: number, btn: HTMLElement): void {
            if (!Number.isInteger(Number(relatedContentId)) || Number(relatedContentId) <= 0) return;

            const section: HTMLElement | null = dF.RelatedContent.getSection(btn);
            const source: RelatedSource | null = dF.RelatedContent.getSource(section);

            Swal.fire({
                text: "관련 글 연결만 제거합니다. 원본 글 자체는 삭제되지 않습니다.",
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.RELATED, { relatedContentId });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (!res.rslt || !source) return;
                        dF.RelatedContent.removeItemBySource(source, relatedContentId);
                    });
                });
            });
        },

        openTarget: function(contentType: string, id: number): void {
            if (!Number.isInteger(Number(id)) || Number(id) <= 0) return;

            if (contentType === "JOURNAL_DIARY" && typeof dF.JournalDiary?.dtlModal === "function") {
                dF.JournalDiary.dtlModal(id);
                return;
            }

            if (contentType === "JOURNAL_DREAM" && typeof dF.JournalDream?.dtlModal === "function") {
                dF.JournalDream.dtlModal(id);
                return;
            }

            Swal.fire({ text: "상세 화면을 열 수 없습니다." });
        },

        escapeHtml: function(value: any): string {
            return String(value ?? "")
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\"/g, "&quot;")
                .replace(/'/g, "&#39;");
        }
    }
})();

document.addEventListener("DOMContentLoaded", function(): void {
    dF.RelatedContent.init();
});
