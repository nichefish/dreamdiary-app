<template>
  <!--begin::저널 스레드 등록/수정 공용 폼-->
  <div v-if="store.registLoading" class="d-flex justify-content-center py-10">
    <span class="spinner-border text-primary" role="status"></span>
  </div>

  <form v-else-if="model" id="journalThreadRegistForm" class="form" @submit.prevent>
    <input type="hidden" name="id" :value="model.id ?? ''" />
    <input type="hidden" name="contentType" value="JOURNAL_THREAD" />

    <!--begin::카테고리-->
    <div class="row d-flex mb-8">
      <div class="col-12">
        <label class="d-flex align-items-center mb-2">
          <span class="text-gray-700 fs-6 fw-bolder">{{ t("journal.thread.category.label") }}</span>
        </label>
        <select
          name="categoryCode"
          v-model="model.categoryCode"
          class="form-select form-select-solid"
        >
          <option value="">{{ t("common.category.select") }}</option>
          <option
            v-for="category in store.categoryOptions"
            :key="category.code"
            :value="category.code"
          >{{ category.codeName }}</option>
        </select>
        <div v-if="store.categoryError" class="text-danger fs-8 mt-2">
          {{ store.categoryError }}
        </div>
      </div>
    </div>
    <!--end::카테고리-->

    <!--begin::제목-->
    <div class="row d-flex mb-8">
      <div class="col-12">
        <label class="d-flex align-items-center mb-2">
          <span class="text-gray-700 fs-6 fw-bolder">{{ t("common.title") }}</span>
        </label>
        <input
          name="title"
          v-model="model.title"
          class="form-control form-control-solid"
          :placeholder="t('journal.thread.title.placeholder')"
          maxlength="100"
          autocomplete="off"
        />
      </div>
    </div>
    <!--end::제목-->

    <!--begin::본문-->
    <div class="row d-flex mb-8">
      <div class="col-12">
        <label class="d-flex align-items-center mb-2">
          <span class="text-gray-700 fs-6 fw-bolder">{{ t("common.content") }}</span>
        </label>
        <RichEditor v-model="model.content" />
      </div>
    </div>
    <!--end::본문-->
  </form>
  <!--end::저널 스레드 등록/수정 공용 폼-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalThreadStore();
const { t } = useLocaleStore();

/** 모달과 독립 편집 페이지는 같은 등록/수정 모델을 직접 편집한다. */
const model = computed(() => store.registModel);
</script>
