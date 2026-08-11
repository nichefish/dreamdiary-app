<template>
  <!--begin::태그 입력 컴포넌트 (Tagify)-->
  <div v-if="runtimeLoading" class="d-flex justify-content-center py-5" aria-live="polite">
    <span class="spinner-border spinner-border-sm text-primary" role="status"></span>
  </div>
  <div v-else-if="runtimeReady" ref="wrapperRef" class="tagify-editor-wrapper">
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
          maxlength="30"
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
          :placeholder="t('tagify.meta.value.placeholder')"
          maxlength="100"
        />
      </div>
    </div>
  </div>
  <!--end::태그 입력 컴포넌트-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import {
  loadTagifyRuntime,
  type TagifyRuntimeConstructor,
} from "@/shared/ui/tag/tagifyRuntimeLoader";
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
} from "@/shared/utils/tagifyHelper";

interface Props {
  /** 태그 JSON 문자열 (v-model). Tagify 직렬화 형식 */
  modelValue?: string;
  /** 카테고리 맵 데이터. 지정 시 initWithCategoryMap / initMeta 동작. 호출자가 세션 캐시 후 주입 */
  categoryMap?: Record<string, string[]> | null;
  /** true 이면 initMeta (카테고리 + 메타 값 2단계 입력) */
  metaMode?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  categoryMap: null,
  metaMode: false,
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();
const localeStore = useLocaleStore();
const { t } = localeStore;

const wrapperRef = ref<HTMLElement | null>(null);
const inputRef = ref<HTMLInputElement | null>(null);
const runtimeLoading = ref(true);
const runtimeReady = ref(false);

/** DOM id 접두사 (레거시 tag_* / meta_* 구분) */
const idPrefix = computed(() => (props.metaMode ? "meta" : "tag"));

const ctgrPlaceholder = computed(() =>
  props.metaMode ? t("tagify.meta.category.placeholder") : t("tagify.category.placeholder"),
);

let tagifyInst: TagifyInstance | null = null;
let tagifyConstructor: TagifyRuntimeConstructor | null = null;
let suppressChange = false;
let mounted = true;

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

/** locale catalog 변경 시 Tagify를 재생성하지 않고 이미 렌더된 접근성·선택지 레이블만 갱신한다. */
function refreshLocalizedDomLabels(): void {
  const scope = wrapperRef.value;
  if (!scope) return;
  const removeTagAriaLabel = t("tagify.remove-tag.aria-label");
  scope.querySelectorAll<HTMLElement>(".tagify__tag__removeBtn").forEach((element) => {
    element.setAttribute("aria-label", removeTagAriaLabel);
  });
  const customCategoryOption = scope.querySelector<HTMLOptionElement>('option[value="custom"]');
  if (customCategoryOption) customCategoryOption.text = t("tagify.category.custom");
}


function destroyTagify(): void {
  tagifyInst?.destroy();
  tagifyInst = null;
}

function initTagify(): void {
  if (!inputRef.value || !tagifyConstructor) return;
  destroyTagify();

  const categoryMapData = props.categoryMap ?? {};
  const useCategoryMap = props.categoryMap != null;

  tagifyInst = new tagifyConstructor(inputRef.value, {
    ...baseTagifyOptions,
    templates: {
      tag: (tagData: Parameters<typeof metaTemplate>[0]) =>
        props.metaMode
          ? metaTemplate(tagData, t("tagify.remove-tag.aria-label"))
          : tagTemplate(tagData, t("tagify.remove-tag.aria-label")),
    },
    /* ctgr 모드: 어떤 태그명이든 add 이벤트까지 도달해야 ctgr 프롬프트가 열린다.
       skipInvalid: true 이면 whitelist 에 없는 태그가 add 전에 차단되어 프롬프트가 열리지 않는다. */
    ...(useCategoryMap ? { duplicates: true, skipInvalid: false } : {}),
    dropdown: {
      appendTarget: document.body,
    },
  }) as TagifyInstance;

  tagifyInst.draft = { value: null, ctgr: null, meta: null };

  if (useCategoryMap) {
    tagifyInst.ctgr = resolveCtgrDom();
    bindTagifyAutoComplete(tagifyInst, categoryMapData);
    bindTagifyCtgrInputPrompt(tagifyInst, categoryMapData, {
      hasValueInput: props.metaMode,
      onCommitted: emitValue,
      getCustomCategoryLabel: () => t("tagify.category.custom"),
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
  try {
    tagifyConstructor = await loadTagifyRuntime();
    if (!mounted) return;
    runtimeReady.value = true;
    runtimeLoading.value = false;
    await nextTick();
    if (mounted) initTagify();
  } catch {
    // 공유 loader가 전역 런타임 상태와 콘솔에 실패 원인을 기록한다.
    if (mounted) runtimeLoading.value = false;
  }
});

onBeforeUnmount(() => {
  mounted = false;
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
  () => props.categoryMap,
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

watch(
  () => localeStore.catalog,
  async () => {
    await nextTick();
    refreshLocalizedDomLabels();
  },
);
</script>
