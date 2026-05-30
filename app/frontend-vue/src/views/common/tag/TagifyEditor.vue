<template>
  <!--begin::태그 입력 컴포넌트 (Tagify)-->
  <div ref="wrapperRef" class="tagify-editor-wrapper">
    <input
      ref="inputRef"
      type="text"
      class="form-control form-control-solid no-space"
      maxlength="500"
      autocomplete="off"
    />
    <div class="d-flex pt-2 gap-2">
      <div
        :id="idPrefix + '_ctgr_select_div'"
        style="display: none; position: relative;"
      >
        <select
          :id="idPrefix + '_ctgr_select'"
          class="form-select form-select-solid py-2"
        ></select>
      </div>
      <div :id="idPrefix + '_ctgr_div'" style="display: none;">
        <input
          :id="idPrefix + '_ctgr'"
          type="text"
          class="form-control form-control-sm form-control-solid text-noti w-100px"
          :placeholder="ctgrPlaceholder"
          maxlength="500"
        />
      </div>
      <div :id="idPrefix + '_display_div'" style="display: none;">
        <input
          :id="idPrefix + '_display'"
          type="text"
          class="form-control form-control-sm form-control-solid text-dialog fw-bold fs-7 w-100px"
          maxlength="500"
          disabled
        />
      </div>
      <div v-if="metaMode" :id="idPrefix + '_value_div'" style="display: none;">
        <input
          :id="idPrefix + '_value'"
          type="text"
          class="form-control form-control-sm form-control-solid w-200px"
          placeholder="메타 값을 입력합니다"
          maxlength="500"
        />
      </div>
    </div>
  </div>
  <!--end::태그 입력 컴포넌트-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from "vue";
import Tagify from "@yaireo/tagify";
import "@yaireo/tagify/dist/tagify.css";
import {
  baseTagifyOptions,
  tagTemplate,
  metaTemplate,
  bindTagifyAutoComplete,
  bindTagifyCtgrInputPrompt,
  bindTagifyCtgrKeyListener,
  bindTagifyValueKeyListener,
  bindTagifyEscHandler,
  serializeTagifyValue,
  commitTagifyPendingDraft,
  cancelTagifyInput,
  type TagifyInstance,
  type TagifyCtgrDom,
} from "@/utils/tagifyHelper";

interface Props {
  /** 태그 JSON 문자열 (v-model). Tagify 직렬화 형식 */
  modelValue?: string;
  /** 카테고리 맵 데이터. 지정 시 initWithCtgr / initMeta 동작. 호출자가 세션 캐시 후 주입 */
  ctgrMap?: Record<string, string[]> | null;
  /** true 이면 initMeta (카테고리 + 메타 값 2단계 입력) */
  metaMode?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  ctgrMap: null,
  metaMode: false,
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const wrapperRef = ref<HTMLElement | null>(null);
const inputRef = ref<HTMLInputElement | null>(null);

/** DOM id 접두사 (레거시 tag_* / meta_* 구분) */
const idPrefix = computed(() => (props.metaMode ? "meta" : "tag"));

const ctgrPlaceholder = computed(() =>
  props.metaMode ? "메타 카테고리를 입력합니다" : "카테고리를 입력하세요",
);

let tagifyInst: TagifyInstance | null = null;
let suppressChange = false;

function loadOriginalValues(value: string): void {
  if (!tagifyInst) return;
  if (tagifyInst.draft?.value) cancelTagifyInput(tagifyInst);
  suppressChange = true;
  tagifyInst.loadingOriginalValues = true;
  try {
    tagifyInst.loadOriginalValues(value);
  } finally {
    tagifyInst.loadingOriginalValues = false;
    suppressChange = false;
  }
}

function resolveCtgrDom(): TagifyCtgrDom {
  const scope = wrapperRef.value;
  const p = idPrefix.value;
  return {
    selectContainer: scope?.querySelector(`#${p}_ctgr_select_div`) ?? null,
    select: scope?.querySelector(`#${p}_ctgr_select`) as HTMLSelectElement | null,
    displayContainer: scope?.querySelector(`#${p}_display_div`) ?? null,
    display: scope?.querySelector(`#${p}_display`) as HTMLInputElement | null,
    inputContainer: scope?.querySelector(`#${p}_ctgr_div`) ?? null,
    input: scope?.querySelector(`#${p}_ctgr`) as HTMLInputElement | null,
    metaInputContainer: props.metaMode
      ? (scope?.querySelector(`#${p}_value_div`) ?? null)
      : null,
    metaInput: props.metaMode
      ? (scope?.querySelector(`#${p}_value`) as HTMLInputElement | null)
      : null,
  };
}

function emitValue(): void {
  if (!tagifyInst || suppressChange) return;
  emit("update:modelValue", serializeTagifyValue(tagifyInst));
}


function destroyTagify(): void {
  tagifyInst?.destroy();
  tagifyInst = null;
}

function initTagify(): void {
  if (!inputRef.value) return;
  destroyTagify();

  const ctgrMapData = props.ctgrMap ?? {};
  const useCtgr = props.ctgrMap != null;

  tagifyInst = new Tagify(inputRef.value, {
    ...baseTagifyOptions,
    templates: { tag: props.metaMode ? metaTemplate : tagTemplate },
    /* ctgr 모드: 어떤 태그명이든 add 이벤트까지 도달해야 ctgr 프롬프트가 열린다.
       skipInvalid: true 이면 whitelist 에 없는 태그가 add 전에 차단되어 프롬프트가 열리지 않는다. */
    ...(useCtgr ? { duplicates: true, skipInvalid: false } : {}),
  }) as TagifyInstance;

  tagifyInst.draft = { value: null, ctgr: null, meta: null };

  if (useCtgr) {
    tagifyInst.ctgr = resolveCtgrDom();
    bindTagifyAutoComplete(tagifyInst, ctgrMapData);
    bindTagifyCtgrInputPrompt(tagifyInst, ctgrMapData, {
      hasValueInput: props.metaMode,
      onCommitted: emitValue,
    });
    bindTagifyCtgrKeyListener(tagifyInst, {
      hasValueInput: props.metaMode,
      onCommitted: emitValue,
    });
    if (props.metaMode) {
      bindTagifyValueKeyListener(tagifyInst, { onCommitted: emitValue });
    }
  }

  /* pending(draft) 상태 및 일반 타이핑 중 ESC 클리어 */
  bindTagifyEscHandler(tagifyInst);

  tagifyInst.on("change", () => emitValue());

  if (props.modelValue) loadOriginalValues(props.modelValue);
}

/** 저장 전 작성 중 draft 확정 (메타 모달 등) */
function commitPendingDraft(): boolean {
  if (!tagifyInst) return false;
  const ok = commitTagifyPendingDraft(tagifyInst);
  if (ok) emitValue();
  return ok;
}

function hasPendingDraft(): boolean {
  return !!tagifyInst?.draft?.value;
}

function cancelDraft(): void {
  if (tagifyInst) cancelTagifyInput(tagifyInst);
}

defineExpose({ commitPendingDraft, hasPendingDraft, cancelDraft });

onMounted(async () => {
  await nextTick();
  initTagify();
});

onBeforeUnmount(() => {
  destroyTagify();
});

watch(
  () => props.modelValue,
  (newVal) => {
    if (!tagifyInst) return;
    /* ctgr 프롬프트 진행 중 외부 값 반영 차단: draft 가 살아있는 동안 loadOriginalValues 를 호출하면
       cancelTagifyInput 이 호출되어 카테고리 입력 흐름이 끊긴다. */
    if (tagifyInst.draft?.value) return;
    const cur = serializeTagifyValue(tagifyInst);
    if (cur === (newVal ?? "")) return;
    loadOriginalValues(newVal ?? "");
  },
);

watch(
  () => props.ctgrMap,
  async () => {
    await nextTick();
    initTagify();
  },
);

watch(
  () => props.metaMode,
  async () => {
    await nextTick();
    initTagify();
  },
);
</script>
