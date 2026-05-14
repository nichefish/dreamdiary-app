<template>
  <!--begin::태그 입력 컴포넌트 (Tagify)-->
  <div class="tagify-editor-wrapper">
    <!--begin::tagify 기반 input (Tagify 가 이 엘리먼트에 마운트됨)-->
    <input ref="inputRef" type="text" />
    <!--end::tagify 기반 input-->

    <!--begin::카테고리 선택 프롬프트 (ctgrMapUrl 사용 시, 태그 추가 후 표시)-->
    <div v-if="draft" class="mt-2 p-3 rounded border bg-light-subtle">
      <div class="mb-2 fs-8 text-muted">
        태그 <strong class="text-gray-800">{{ draft.value }}</strong>의 카테고리를 선택하세요
      </div>
      <!--begin::미리 정의된 카테고리 selectbox-->
      <div v-if="!draft.showInput" class="mb-1">
        <select class="form-select form-select-sm" @change="onSelectPredefined">
          <option value="">-- 선택 --</option>
          <option value="__custom__">직접입력</option>
          <option v-for="c in draft.predefined" :key="c" :value="c">{{ c }}</option>
        </select>
        <button type="button" class="btn btn-sm btn-light mt-1" @click="cancelDraft">취소</button>
      </div>
      <!--end::미리 정의된 카테고리 selectbox-->
      <!--begin::카테고리 직접 입력-->
      <div v-else class="d-flex gap-1 align-items-center">
        <input
          ref="ctgrInputRef"
          v-model="draft.ctgr"
          type="text"
          class="form-control form-control-sm"
          placeholder="카테고리를 입력하세요"
          @keydown.enter.prevent="commitDraft"
          @keydown.tab.prevent="commitDraft"
          @keydown.esc.prevent="cancelDraft"
        />
        <button type="button" class="btn btn-sm btn-primary" @click="commitDraft">추가</button>
        <button type="button" class="btn btn-sm btn-light" @click="cancelDraft">취소</button>
      </div>
      <!--end::카테고리 직접 입력-->
    </div>
    <!--end::카테고리 선택 프롬프트-->
  </div>
  <!--end::태그 입력 컴포넌트-->
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from "vue";
import Tagify from "@yaireo/tagify";
import "@yaireo/tagify/dist/tagify.css";
import axios from "axios";

interface Props {
  /** 태그 JSON 문자열 (v-model). Tagify 직렬화 형식: [{"value":"tagname","data":{"ctgr":"cat"}},...] */
  modelValue?: string;
  /** 카테고리 맵 API URL. 지정 시 카테고리 선택 UI 활성화. */
  ctgrMapUrl?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  ctgrMapUrl: "",
});

const emit = defineEmits<{
  /** v-model 업데이트: Tagify 직렬화 JSON 문자열 */
  "update:modelValue": [value: string];
}>();

const inputRef = ref<HTMLInputElement | null>(null);
const ctgrInputRef = ref<HTMLInputElement | null>(null);

let tagifyInst: any = null;
/** 태그명 → 허용 카테고리 목록 매핑 (서버 조회) */
let ctgrMap: Record<string, string[]> = {};
/** 내부 변경 중 change 이벤트 무시 플래그 */
let suppressChange = false;

/** 카테고리 선택 임시 상태 */
interface Draft {
  /** 선택 중인 태그 이름 */
  value: string;
  /** 직접 입력 카테고리 값 */
  ctgr: string;
  /** 서버 정의 카테고리 목록 */
  predefined: string[];
  /** 직접 입력 UI 표시 여부 */
  showInput: boolean;
}
const draft = ref<Draft | null>(null);

/** 태그 표시 템플릿: 카테고리가 있으면 [카테고리] 형식으로 표시 */
function tagTemplate(tagData: any): string {
  const ctgr: string = tagData.data?.ctgr ?? "";
  const ctgrSpan: string = ctgr
    ? `<span class="tagify__tag-category text-noti me-1">[${ctgr}]</span>`
    : "";
  return `<tag title="${tagData.value}" contenteditable="false" spellcheck="false" tabindex="-1"
               class="tagify__tag" value="${tagData.value}" data-ctgr="${ctgr}">
            <x title="" class="tagify__tag__removeBtn" role="button" aria-label="remove tag"></x>
            <div>
              ${ctgrSpan}<span class="tagify__tag-text">${tagData.value}</span>
            </div>
          </tag>`;
}

/** 카테고리 맵 서버 조회 */
async function fetchCtgrMap(): Promise<void> {
  if (!props.ctgrMapUrl) return;
  try {
    const res = await axios.get(props.ctgrMapUrl);
    if (res.data?.rslt) ctgrMap = res.data.rsltObj ?? {};
  } catch {
    /** 조회 실패 시 빈 맵으로 진행 */
    ctgrMap = {};
  }
}

/** Tagify 현재 태그 목록을 직렬화 */
function serializeValue(): string {
  if (!tagifyInst) return "";
  return JSON.stringify(
    (tagifyInst.value as any[]).map((t: any) => ({
      value: t.value,
      ...(t.data ? { data: t.data } : {}),
    }))
  );
}

/** Tagify 초기화 */
function initTagify(): void {
  if (!inputRef.value) return;

  tagifyInst = new Tagify(inputRef.value, {
    whitelist: [],
    maxTags: 21,
    keepInvalidTags: false,
    skipInvalid: true,
    duplicates: false,
    editTags: { clicks: 2, keepInvalid: false },
    transformTag(tagData: any): void {
      tagData.value = tagData.value.replace(/\s+/g, "_");
    },
    templates: { tag: tagTemplate },
  });

  if (props.ctgrMapUrl) {
    /** 입력값 기반 자동완성: ctgrMap 키 목록 필터링 */
    tagifyInst.on("input", (e: any) => {
      const val: string = e.detail.value ?? "";
      tagifyInst.settings.whitelist = Object.keys(ctgrMap).filter((t) => t.startsWith(val));
      tagifyInst.dropdown.show(val);
    });

    /** 태그 추가 시 카테고리 선택 프롬프트 */
    tagifyInst.on("add", (e: any) => {
      if (suppressChange) return;
      const tagVal: string = e.detail.data.value;
      const predefined: string[] = (ctgrMap[tagVal] ?? []).filter(Boolean);

      /** ctgrMap에 없는 태그 → 거부 */
      if (predefined.length === 0) {
        suppressChange = true;
        tagifyInst.removeTags(e.detail.tag);
        suppressChange = false;
        return;
      }

      /** 임시 태그 제거 → 카테고리 선택 UI 표시 */
      suppressChange = true;
      tagifyInst.removeTags(e.detail.tag);
      suppressChange = false;

      draft.value = { value: tagVal, ctgr: "", predefined, showInput: false };
    });
  }

  /** change: 태그 목록 변경 시 v-model emit */
  tagifyInst.on("change", () => {
    if (suppressChange) return;
    emit("update:modelValue", serializeValue());
  });

  /** 초기값 로드 */
  if (props.modelValue) {
    suppressChange = true;
    tagifyInst.loadOriginalValues(props.modelValue);
    suppressChange = false;
  }
}

/** 미리 정의된 카테고리 selectbox 선택 */
function onSelectPredefined(e: Event): void {
  const val = (e.target as HTMLSelectElement).value;
  if (!val) return;
  if (val === "__custom__") {
    draft.value!.showInput = true;
    nextTick(() => ctgrInputRef.value?.focus());
  } else {
    draft.value!.ctgr = val;
    commitDraft();
  }
}

/** 카테고리 입력 확정 → 태그 최종 추가 */
function commitDraft(): void {
  if (!draft.value) return;
  const ctgr = draft.value.ctgr.trim();
  if (!ctgr) {
    cancelDraft();
    return;
  }
  const { value } = draft.value;
  draft.value = null;

  suppressChange = true;
  tagifyInst.addTags([{ value, data: { ctgr } }]);
  suppressChange = false;

  emit("update:modelValue", serializeValue());
  nextTick(() => (tagifyInst?.DOM?.input as HTMLElement)?.focus());
}

/** 카테고리 입력 취소 → draft 초기화 */
function cancelDraft(): void {
  draft.value = null;
  nextTick(() => (tagifyInst?.DOM?.input as HTMLElement)?.focus());
}

onMounted(async () => {
  await fetchCtgrMap();
  initTagify();
});

onBeforeUnmount(() => {
  tagifyInst?.destroy();
  tagifyInst = null;
});

/** 부모에서 modelValue 변경 시 Tagify 동기화 */
watch(
  () => props.modelValue,
  (newVal) => {
    if (!tagifyInst) return;
    const cur = serializeValue();
    if (cur === newVal) return;
    suppressChange = true;
    tagifyInst.loadOriginalValues(newVal ?? "");
    suppressChange = false;
  }
);

/** ctgrMapUrl 변경 시 ctgrMap 재조회 */
watch(
  () => props.ctgrMapUrl,
  async (newUrl) => {
    if (!newUrl) {
      ctgrMap = {};
      return;
    }
    await fetchCtgrMap();
  }
);
</script>