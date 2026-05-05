/**
 * JournalDayTagProfileModalApp.ts
 * 태그 프로필 모달 폼 패치 브리지.
 */

type TagProfilePayload = Record<string, any>;
type TagProfileBridge = {
    mounted?: boolean;
    open?: (payload: TagProfilePayload) => boolean;
    pendingPayload?: TagProfilePayload | null;
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function syncSelectStyle(selector: string, previewSelector?: string): void {
    const selectEl: HTMLSelectElement | null = document.querySelector(selector);
    if (!selectEl) return;

    const selectedOption: HTMLOptionElement | undefined = selectEl.selectedOptions?.[0];
    let textClass: string = selectedOption?.dataset.textClass ?? "";
    if (selector === "#tagTextClassCd" && selectEl.value === "") {
        const catSel: HTMLSelectElement | null = document.querySelector("#tagCategoryTextClassCd");
        const catOpt: HTMLOptionElement | undefined = catSel?.selectedOptions?.[0];
        textClass = catOpt?.dataset.textClass ?? "";
    }
    selectEl.className = `form-select form-select-solid ${textClass}`.trim();

    if (!previewSelector) return;
    const previewEl: HTMLElement | null = document.querySelector(previewSelector);
    if (!previewEl) return;
    previewEl.className = textClass;
}

function bindSelectStyleEvents(): void {
    const selectorPairs: { select: string; preview: string }[] = [
        { select: "#tagCategoryTextClassCd", preview: "#tagCategoryColorPreview" },
        { select: "#tagTextClassCd", preview: "#tagColorPreview" },
    ];
    selectorPairs.forEach(function(pair: { select: string; preview: string }): void {
        const selectEl: HTMLSelectElement | null = document.querySelector(pair.select);
        if (!selectEl) return;
        selectEl.onchange = function(): void {
            syncSelectStyle(pair.select, pair.preview);
        };
        syncSelectStyle(pair.select, pair.preview);
    });

    const catSel: HTMLSelectElement | null = document.querySelector("#tagCategoryTextClassCd");
    if (catSel) {
        catSel.addEventListener("change", function(): void {
            const indSel: HTMLSelectElement | null = document.querySelector("#tagTextClassCd");
            if (indSel && indSel.value === "") syncSelectStyle("#tagTextClassCd", "#tagColorPreview");
        });
    }
}

function applyPayload(model: TagProfilePayload): boolean {
    const formEl: HTMLFormElement | null = document.querySelector("#tagProfileForm");
    const modalEl: HTMLElement | null = document.querySelector("#tag_profile_modal");
    if (!formEl || !modalEl) {
        console.error("[JournalDayTagProfileModalApp] tag_profile form/target not found.");
        return false;
    }

    const getInput = (name: string): HTMLInputElement | null =>
        formEl.querySelector(`input[name='${name}']`) as HTMLInputElement | null;
    const idInput = getInput("id");
    const categoryProfileIdInput = getInput("categoryProfileId");
    const tagIdInput = getInput("tagId");
    const tagCategoryIdInput = getInput("tagCategoryId");
    const contentTypeInput = getInput("contentType");
    if (!idInput || !categoryProfileIdInput || !tagIdInput || !tagCategoryIdInput || !contentTypeInput) {
        console.error("[JournalDayTagProfileModalApp] tag_profile hidden fields not found.");
        return false;
    }

    idInput.value = String(model.id ?? "");
    categoryProfileIdInput.value = String(model.categoryProfileId ?? "");
    tagIdInput.value = String(model.tagId ?? "");
    tagCategoryIdInput.value = String(model.tagCategoryId ?? "");
    contentTypeInput.value = String(model.contentType ?? "");

    const contentTypeLabelEl: HTMLElement | null = document.querySelector("#tagProfileContentTypeLabel");
    const ctgrBadgeEl: HTMLElement | null = document.querySelector("#tagProfileCtgrBadge");
    const tagNmEl: HTMLElement | null = document.querySelector("#tagProfileTagNm");
    if (contentTypeLabelEl) contentTypeLabelEl.textContent = String(model.contentTypeLabel ?? "");
    if (ctgrBadgeEl) {
        const ctgrText: string = String(model.ctgr ?? "");
        ctgrBadgeEl.textContent = `[${ctgrText}]`;
        ctgrBadgeEl.classList.toggle("d-none", cF.util.isEmpty(ctgrText));
    }
    if (tagNmEl) tagNmEl.textContent = String(model.tagNm ?? "");

    const categoryColEl: HTMLElement | null = document.querySelector("#tagCategoryTextClassCol");
    const categorySelectEl: HTMLSelectElement | null = document.querySelector("#tagCategoryTextClassCd");
    const noGuideEl: HTMLElement | null = document.querySelector("#tagCategoryNoGuide");
    const hasTagCategory: boolean = cF.util.isNotEmpty(String(model.tagCategoryId ?? ""));
    if (categoryColEl) {
        categoryColEl.classList.toggle("opacity-50", !hasTagCategory);
        if (!hasTagCategory) {
            categoryColEl.style.filter = "grayscale(1)";
            categoryColEl.style.pointerEvents = "none";
            categoryColEl.setAttribute("aria-disabled", "true");
        } else {
            categoryColEl.style.removeProperty("filter");
            categoryColEl.style.removeProperty("pointer-events");
            categoryColEl.removeAttribute("aria-disabled");
        }
    }
    if (categorySelectEl) {
        categorySelectEl.disabled = !hasTagCategory;
        categorySelectEl.value = String(model.categoryTextClassCd ?? "");
    }
    if (noGuideEl) noGuideEl.classList.toggle("d-none", hasTagCategory);

    const tagTextClassSelectEl: HTMLSelectElement | null = document.querySelector("#tagTextClassCd");
    const tagTextClassDefaultOptEl: HTMLOptionElement | null = document.querySelector("#tagTextClassDefaultOption");
    if (tagTextClassDefaultOptEl) {
        tagTextClassDefaultOptEl.textContent = hasTagCategory
            ? Message.get("txt.attachable.tag.profile.same-as-category")
            : Message.get("txt.attachable.tag.profile.default-no-category");
    }
    if (tagTextClassSelectEl) tagTextClassSelectEl.value = String(model.textClassCd ?? "");

    const categoryPreviewEl: HTMLElement | null = document.querySelector("#tagCategoryColorPreview");
    if (categoryPreviewEl) categoryPreviewEl.textContent = hasTagCategory ? `[${String(model.ctgr ?? "")}]` : "";
    const tagColorPreviewEl: HTMLElement | null = document.querySelector("#tagColorPreview");
    if (tagColorPreviewEl) tagColorPreviewEl.innerHTML = `<span>#</span>${String(model.tagNm ?? "")}`;

    const contentTextareaEl: HTMLTextAreaElement | null = document.querySelector("#tagProfileCn");
    if (contentTextareaEl) contentTextareaEl.value = String(model.content ?? "");

    bindSelectStyleEvents();
    $("#tag_profile_del_btn").toggleClass("d-none", cF.util.isEmpty(model.id));
    (window as any).bootstrap.Modal.getOrCreateInstance(modalEl).show();
    return true;
}

runWhenDomReady(function(): void {
    const rootEl = document.querySelector("#journal_day_tag_profile_app");
    if (!rootEl) {
        console.error("[JournalDayTagProfileModalApp] Vue mount root not found.");
        return;
    }

    const queuedBridge = window.JournalDayTagProfileVueApp as TagProfileBridge | undefined;
    window.JournalDayTagProfileVueApp = {
        mounted: true,
        open: function(payload: TagProfilePayload): boolean {
            return applyPayload(payload);
        },
    };

    if (queuedBridge?.pendingPayload) window.JournalDayTagProfileVueApp.open?.(queuedBridge.pendingPayload);
});

export {};
