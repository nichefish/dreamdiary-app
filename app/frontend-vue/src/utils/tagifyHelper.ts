/**
 * tagifyHelper.ts
 * 레거시 cF.tagify (static/js/common/helper/tagify.ts) 동작을 Vue TagifyEditor 에서 재사용한다.
 *
 * @author nichefish
 */

import type Tagify from "@yaireo/tagify";

export interface TagifyDraft {
  value: string | null;
  ctgr: string | null;
  meta: string | null;
}

export interface TagifyCtgrDom {
  selectContainer: HTMLElement | null;
  select: HTMLSelectElement | null;
  displayContainer: HTMLElement | null;
  display: HTMLInputElement | null;
  inputContainer: HTMLElement | null;
  input: HTMLInputElement | null;
  metaInputContainer?: HTMLElement | null;
  metaInput?: HTMLInputElement | null;
}

export type TagifyInstance = Tagify & {
  committing?: boolean;
  loadingOriginalValues?: boolean;
  draft?: TagifyDraft;
  ctgr?: TagifyCtgrDom;
};

function isNotEmpty(v: unknown): boolean {
  return v !== null && v !== undefined && String(v).trim() !== "";
}

/** 태그 표시 템플릿 (카테고리) */
export function tagTemplate(tagData: { value?: string; data?: { ctgr?: string } }): string {
  const ctgr = isNotEmpty(tagData.data?.ctgr) ? String(tagData.data?.ctgr) : "";
  const ctgrSpan = ctgr !== ""
    ? `<span class="tagify__tag-category text-noti me-1">[${ctgr}]</span>`
    : "";
  return `<tag title="${tagData.value}" contenteditable="false" spellcheck="false" tabindex="-1"
               class="tagify__tag" value="${tagData.value}" data-ctgr="${ctgr}">
            <x title="" class="tagify__tag__removeBtn" role="button" aria-label="remove tag"></x>
            <div>
              ${ctgrSpan}
              <span class="tagify__tag-text">${tagData.value}</span>
            </div>
          </tag>`;
}

/** 메타 태그 표시 템플릿 (카테고리 + 값) */
export function metaTemplate(tagData: { value?: string; data?: { ctgr?: string; value?: string } }): string {
  const ctgr = isNotEmpty(tagData.data?.ctgr) ? String(tagData.data?.ctgr) : "";
  const ctgrSpan = ctgr !== ""
    ? `<span class="tagify__tag-category text-noti me-1">[${ctgr}]</span>`
    : "";
  const meta = isNotEmpty(tagData.data?.value) ? String(tagData.data?.value) : "";
  const idx = meta.indexOf(":");
  const label = idx !== -1 ? meta.slice(0, idx) : "";
  const value = idx !== -1 ? meta.slice(idx + 1) : meta;
  const labelSpan = isNotEmpty(label) ? `<span class="tagify__tag-meta mx-1"> - ${label}</span>` : "";
  const metaSpan = isNotEmpty(meta) ? `<span class="text-dialog">: ${value}</span>` : "";
  return `<tag title="${tagData.value}" contenteditable="false" spellcheck="false" tabindex="-1"
               class="tagify__tag" value="${tagData.value}" data-ctgr="${ctgr}" data-value="${meta}">
            <x title="" class="tagify__tag__removeBtn" role="button" aria-label="remove tag"></x>
            <div>
              ${ctgrSpan}
              <span class="tagify__tag-text">${tagData.value}</span>
              ${labelSpan}${metaSpan}
            </div>
          </tag>`;
}

export const baseTagifyOptions: Record<string, unknown> = {
  whitelist: [],
  maxTags: 21,
  keepInvalidTags: false,
  skipInvalid: true,
  duplicates: false,
  editTags: { clicks: 2, keepInvalid: false },
  transformTag(tagData: { value: string }) {
    tagData.value = tagData.value.replace(/\s+/g, "_");
    return tagData;
  },
};

function toggle(el: HTMLElement | null | undefined, show: boolean): void {
  if (!el) return;
  el.style.display = show ? "block" : "none";
}

function showAndFocus(container: HTMLElement | null | undefined, el?: HTMLElement | null): void {
  if (!container) return;
  container.style.display = "block";
  setTimeout(() => {
    el?.focus?.();
    if (el instanceof HTMLInputElement) el.select();
  }, 0);
}

function focusTagInput(tagify: TagifyInstance): void {
  setTimeout(() => tagify.DOM.input?.focus(), 0);
}

/** 카테고리/메타 입력 취소 (ESC) */
export function cancelTagifyInput(tagify: TagifyInstance): void {
  toggle(tagify.ctgr?.selectContainer, false);
  toggle(tagify.ctgr?.displayContainer, false);
  toggle(tagify.ctgr?.inputContainer, false);
  toggle(tagify.ctgr?.metaInputContainer, false);
  tagify.draft = { value: null, ctgr: null, meta: null };
  focusTagInput(tagify);
}

/** 최종 태그 확정 추가 */
export function commitTagifyTag(
  tagify: TagifyInstance,
  value: string,
  ctgr: string,
  meta: string | null,
): void {
  tagify.committing = true;
  tagify.addTags([{ value, data: { ctgr, value: meta } }]);
  toggle(tagify.ctgr?.selectContainer, false);
  toggle(tagify.ctgr?.displayContainer, false);
  toggle(tagify.ctgr?.inputContainer, false);
  toggle(tagify.ctgr?.metaInputContainer, false);
  tagify.draft = { value: null, ctgr: null, meta: null };
  focusTagInput(tagify);
}

/** Tagify value 배열을 v-model JSON 문자열로 직렬화 */
export function serializeTagifyValue(tagify: TagifyInstance | null): string {
  if (!tagify) return "";
  return JSON.stringify(
    (tagify.value as Array<{ value: string; data?: Record<string, unknown> }>).map((t) => ({
      value: t.value,
      ...(t.data ? { data: t.data } : {}),
    })),
  );
}

/** 태그 자동완성 (ctgrMap 키 prefix 필터). whitelist 가 비면 dropdown 을 닫는다. */
export function bindTagifyAutoComplete(tagify: TagifyInstance, ctgrMap: Record<string, string[]>): void {
  tagify.on("input", (e: { detail: { value: string } }) => {
    const val = e.detail.value ?? "";
    tagify.settings.whitelist = Object.keys(ctgrMap).filter((tag) => tag.startsWith(val));
    if ((tagify.settings.whitelist as string[]).length > 0) {
      tagify.dropdown.show(val);
    } else {
      tagify.dropdown.hide();
    }
  });
}

/**
 * 태그 추가 시 카테고리(·메타) 입력 프롬프트.
 * ctgrMap에 없거나 카테고리 목록이 비면 태그를 제거한다 (레거시와 동일).
 */
export function bindTagifyCtgrInputPrompt(
  tagify: TagifyInstance,
  ctgrMap: Record<string, string[]>,
  options: { hasValueInput?: boolean; onCommitted?: () => void } = {},
): void {
  const { hasValueInput = false, onCommitted } = options;
  tagify.committing = false;

  tagify.on("add", (e: { detail: { data: { value: string }; tag: unknown } }) => {
    if (tagify.loadingOriginalValues) return;

    if (tagify.committing) {
      tagify.committing = false;
      onCommitted?.();
      return;
    }

    const addedTag = e.detail.data;
    tagify.draft = { value: addedTag.value, ctgr: null, meta: null };

    if (tagify.ctgr?.input) tagify.ctgr.input.value = "";
    toggle(tagify.ctgr?.displayContainer, true);
    if (tagify.ctgr?.display) tagify.ctgr.display.value = tagify.draft.value ?? "";
    toggle(tagify.ctgr?.selectContainer, false);

    const filteredCtgr = (ctgrMap[tagify.draft.value ?? ""] ?? []).filter(Boolean);

    if (filteredCtgr.length === 0) {
      /* ctgrMap에 없는 태그: 텍스트 입력으로 카테고리 직접 입력 */
      tagify.removeTags(e.detail.tag);
      showAndFocus(tagify.ctgr?.inputContainer, tagify.ctgr?.input);
      if (hasValueInput) toggle(tagify.ctgr?.metaInputContainer, true);
      return;
    }

    /* ctgrMap에 있는 태그: select로 카테고리 선택 (텍스트 입력은 "직접입력" 선택 시에만 표시) */
    toggle(tagify.ctgr?.inputContainer, false);
    if (tagify.ctgr?.select) {
      tagify.ctgr.select.innerHTML =
        '<option value="custom">직접입력</option>' +
        filteredCtgr.map((item) => `<option value="${item}">${item}</option>`).join("");
      tagify.ctgr.select.size = filteredCtgr.length + 1;
    }
    showAndFocus(tagify.ctgr?.selectContainer, tagify.ctgr?.select);

    if (tagify.ctgr?.select) {
      tagify.ctgr.select.onchange = () => {
        tagify.draft!.ctgr = tagify.ctgr!.select!.value;
        if (tagify.draft!.ctgr === "custom") {
          if (tagify.ctgr?.input) tagify.ctgr.input.value = "";
          showAndFocus(tagify.ctgr?.inputContainer, tagify.ctgr?.input);
        } else if (hasValueInput) {
          if (tagify.ctgr?.input) tagify.ctgr.input.value = tagify.draft!.ctgr ?? "";
          if (tagify.ctgr?.metaInput) tagify.ctgr.metaInput.value = "";
          showAndFocus(tagify.ctgr?.metaInputContainer, tagify.ctgr?.metaInput);
        } else {
          const { value, ctgr } = tagify.draft!;
          if (!value || !ctgr) {
            cancelTagifyInput(tagify);
            return;
          }
          commitTagifyTag(tagify, value, ctgr, null);
          onCommitted?.();
        }
      };
      /* 카테고리 select ESC → 입력 취소 */
      tagify.ctgr.select.addEventListener("keydown", (event: KeyboardEvent) => {
        if (event.key !== "Escape") return;
        event.preventDefault();
        cancelTagifyInput(tagify);
      });
    }

    tagify.removeTags(e.detail.tag);
  });
}

/** 카테고리 입력 키보드 (Tab/Enter/Escape) */
export function bindTagifyCtgrKeyListener(
  tagify: TagifyInstance,
  options: { hasValueInput?: boolean; onCommitted?: () => void } = {},
): void {
  const { hasValueInput = false, onCommitted } = options;
  if (!tagify.ctgr?.input) return;

  tagify.ctgr.input.addEventListener("keydown", (event: KeyboardEvent) => {
    switch (event.key) {
      case "Escape":
        event.preventDefault();
        cancelTagifyInput(tagify);
        return;
      case "Tab":
      case "Enter":
        event.preventDefault();
        if (tagify.draft) tagify.draft.ctgr = tagify.ctgr!.input!.value;
        if (hasValueInput) {
          showAndFocus(tagify.ctgr?.metaInputContainer, tagify.ctgr?.metaInput);
          return;
        }
        if (tagify.draft) {
          const { value, ctgr, meta } = tagify.draft;
          if (value) {
            commitTagifyTag(tagify, value, ctgr ?? "", meta);
            onCommitted?.();
          } else {
            cancelTagifyInput(tagify);
          }
        }
    }
  });
}

/** 메타 값 입력 키보드 */
export function bindTagifyValueKeyListener(
  tagify: TagifyInstance,
  options: { onCommitted?: () => void } = {},
): void {
  const { onCommitted } = options;
  if (!tagify.ctgr?.metaInput) return;

  tagify.ctgr.metaInput.addEventListener("keydown", (event: KeyboardEvent) => {
    switch (event.key) {
      case "Escape":
        event.preventDefault();
        cancelTagifyInput(tagify);
        return;
      case "Tab":
      case "Enter": {
        event.preventDefault();
        if (tagify.draft) tagify.draft.meta = tagify.ctgr!.metaInput!.value;
        const { value, ctgr, meta } = tagify.draft ?? {};
        if (!isNotEmpty(meta)) {
          cancelTagifyInput(tagify);
          return;
        }
        if (value && ctgr) {
          commitTagifyTag(tagify, value, ctgr, meta ?? null);
          onCommitted?.();
        }
      }
    }
  });
}

/**
 * Tagify 메인 입력창 ESC 핸들러.
 * - draft(pending) 상태: 카테고리·메타 프롬프트 전체 취소.
 * - 일반 타이핑 중: 입력 텍스트 클리어.
 */
export function bindTagifyEscHandler(tagify: TagifyInstance): void {
  if (!tagify.DOM?.input) return;
  tagify.DOM.input.addEventListener("keydown", (event: KeyboardEvent) => {
    if (event.key !== "Escape") return;
    if (tagify.draft?.value) {
      /* pending(draft) 상태 → 프롬프트 취소 */
      event.preventDefault();
      cancelTagifyInput(tagify);
    } else if (tagify.DOM.input.textContent) {
      /* 일반 타이핑 중 → 텍스트 클리어 (input 이벤트로 Tagify 내부 상태도 동기화) */
      event.preventDefault();
      tagify.DOM.input.textContent = "";
      tagify.DOM.input.dispatchEvent(new Event("input", { bubbles: true }));
    }
  });
}

/**
 * 저장 전 작성 중인 메타 draft 를 확정한다.
 * 레거시 저널 일자 등록 regAjax 의 metaTagify.draft 처리와 동일.
 */
export function commitTagifyPendingDraft(tagify: TagifyInstance | null): boolean {
  if (!tagify?.draft?.value) return false;
  const metaVal = tagify.ctgr?.metaInput?.value?.trim() ?? tagify.draft.meta?.trim() ?? "";
  const { value, ctgr } = tagify.draft;
  if (!value || !metaVal) return false;
  const resolvedCtgr = ctgr ?? tagify.ctgr?.input?.value?.trim() ?? "";
  if (!resolvedCtgr) return false;
  commitTagifyTag(tagify, value, resolvedCtgr, metaVal);
  return true;
}
