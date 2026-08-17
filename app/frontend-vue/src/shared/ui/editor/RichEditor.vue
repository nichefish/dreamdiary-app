<template>
  <!--begin::리치 에디터 지연 로딩 경계-->
  <div v-if="loading" class="d-flex justify-content-center py-10" aria-live="polite">
    <span class="spinner-border text-primary" role="status"></span>
  </div>
  <component
    :is="editorRuntime"
    v-else-if="editorRuntime"
    :model-value="modelValue"
    :height="height"
    :placeholder="placeholder"
    :enable-templates="enableTemplates"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <!--end::리치 에디터 지연 로딩 경계-->
</template>

<script setup lang="ts">
import { markRaw, onBeforeUnmount, onMounted, ref, shallowRef } from "vue";
import {
  loadRichEditorRuntime,
  type RichEditorRuntimeComponent,
} from "./richEditorRuntimeLoader";

interface Props {
  /** 에디터 콘텐츠 (v-model). undefined 는 빈 문자열로 정규화. */
  modelValue?: string;
  /** 에디터 높이 (px). 기본값 540. */
  height?: number;
  placeholder?: string;
  /** 템플릿 삽입 드롭다운 노출 여부. 기본값 false. 저널 엔트리 작성 에디터에서만 켠다. */
  enableTemplates?: boolean;
}

withDefaults(defineProps<Props>(), {
  modelValue: "",
  height: 540,
  placeholder: undefined,
  enableTemplates: false,
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const editorRuntime = shallowRef<RichEditorRuntimeComponent | null>(null);
const loading = ref(true);
let mounted = true;

onMounted(async () => {
  try {
    const component = await loadRichEditorRuntime();
    if (mounted) editorRuntime.value = markRaw(component);
  } catch {
    // 공유 loader가 전역 런타임 상태와 콘솔에 실패 원인을 기록한다.
  } finally {
    if (mounted) loading.value = false;
  }
});

onBeforeUnmount(() => {
  mounted = false;
});
</script>
