<template>
  <div class="journal-annual-aside card card-reset card-p-0 p-5">
    <div class="d-flex justify-content-end mb-2">
      <button type="button" class="btn btn-sm btn-icon btn-light" :title="t('journal.annual.filter.panel-close.tooltip')" @click="emit('close')">
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <div class="card-header min-h-auto mb-5 px-0 border-0">
      <h3 class="card-title text-gray-900 fw-bold fs-3 mb-0">
        <i class="bi bi-filter fs-2 me-1"></i> {{ t("journal.annual.filter.title") }}
      </h3>
    </div>

    <div class="card-body p-0 d-flex flex-column gap-3">
      <select class="form-select form-select-sm" :value="store.filterYy ?? ''" @change="onYyChange">
        <option value="">{{ t('journal.annual.filter.all') }}</option>
        <option v-for="y in yyOptions" :key="y" :value="y">{{ y }}</option>
      </select>

      <div class="d-flex flex-column gap-2">
        <div class="text-gray-900 fs-6 fw-bold">{{ t("journal.annual.filter.summary") }}</div>
        <div>
          <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.annual.filter.keywords") }}</div>
          <div class="input-group input-group-sm">
            <input
              v-model="store.listKeyword"
              type="text"
              class="form-control form-control-sm"
              :placeholder="t('journal.annual.filter.keyword.placeholder')"
              maxlength="200"
              @keyup.enter="applyListFilters"
            />
            <button type="button" class="btn btn-sm btn-icon btn-light" :title="t('journal.annual.filter.apply.tooltip')" @click="applyListFilters">
              <i class="bi bi-funnel fs-7"></i>
            </button>
          </div>
        </div>

        <button type="button" class="btn btn-sm btn-light w-100" @click="clearListFilters">
          {{ t('journal.annual.filter.reset') }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const emit = defineEmits<{ close: [] }>();
const store = useJournalAnnualStore();
const { t } = useLocaleStore();

const currentYear = new Date().getFullYear();
const yyOptions = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

function onYyChange(e: Event) {
  const val = (e.target as HTMLSelectElement).value;
  store.setFilterYy(val === "" ? null : Number(val));
}

function applyListFilters() {
  void store.applyListFilters();
}

function clearListFilters() {
  void store.clearListFilters();
}

</script>
