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
      <select class="form-select form-select-sm" :value="activeYy" @change="onYyChange">
        <option v-for="y in yyOptions" :key="y" :value="y">{{ y }}</option>
      </select>

      <div class="separator"></div>

      <div class="d-flex flex-column gap-2">
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showTagCloud"
            @change="toggleTagCloud"
          />
          <span class="form-check-label text-muted fs-7">{{ t("journal.annual.filter.tagcloud") }}</span>
        </label>
      </div>

      <div class="d-flex flex-column gap-2">
        <div class="text-gray-900 fs-6 fw-bold">{{ t("journal.annual.filter.entry") }}</div>

        <div>
          <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.annual.filter.diary-keywords") }}</div>
          <div class="input-group input-group-sm">
            <input
              v-model="store.diaryKeyword"
              type="text"
              class="form-control form-control-sm"
              :placeholder="t('journal.annual.filter.diary-keyword.placeholder')"
              maxlength="200"
              @keyup.enter="applyEntryFilters"
            />
            <button type="button" class="btn btn-sm btn-icon btn-light" :title="t('journal.annual.filter.diary-keyword.apply.tooltip')" @click="applyEntryFilters">
              <i class="bi bi-funnel fs-7"></i>
            </button>
          </div>
        </div>

        <div>
          <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.annual.filter.dream-keywords") }}</div>
          <div class="input-group input-group-sm">
            <input
              v-model="store.dreamKeyword"
              type="text"
              class="form-control form-control-sm"
              :placeholder="t('journal.annual.filter.dream-keyword.placeholder')"
              maxlength="200"
              @keyup.enter="applyEntryFilters"
            />
            <button type="button" class="btn btn-sm btn-icon btn-light" :title="t('journal.annual.filter.dream-keyword.apply.tooltip')" @click="applyEntryFilters">
              <i class="bi bi-funnel fs-7"></i>
            </button>
          </div>
        </div>

        <button type="button" class="btn btn-sm btn-light w-100" @click="clearEntryFilters">
          {{ t('journal.annual.filter.reset') }}
        </button>
      </div>

      <div class="separator"></div>

      <button type="button" class="btn btn-sm btn-light-primary w-100" @click="store.openRegist()">
        <i class="bi bi-plus-circle me-1"></i>
        {{ t('journal.annual.register') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const emit = defineEmits<{ close: [] }>();
const store = useJournalAnnualStore();
const { t } = useLocaleStore();
const route = useRoute();
const router = useRouter();

const currentYear = new Date().getFullYear();
const yyOptions = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);
const activeYy = computed(() => {
  const routeYy = Number(route.params.yy);
  if (Number.isFinite(routeYy) && routeYy > 0) return routeYy;
  return store.annualDetail?.yy ?? currentYear;
});

function onYyChange(e: Event) {
  const nextYy = Number((e.target as HTMLSelectElement).value);
  if (!Number.isFinite(nextYy) || nextYy <= 0) return;
  store.filterYy = nextYy;
  void router.push({ name: "annual-detail", params: { yy: String(nextYy) } });
}

function toggleTagCloud() {
  void store.toggleTagCloud(activeYy.value);
}

function applyEntryFilters() {
  void store.applyEntryFilters(activeYy.value);
}

function clearEntryFilters() {
  void store.clearEntryFilters(activeYy.value);
}
</script>
