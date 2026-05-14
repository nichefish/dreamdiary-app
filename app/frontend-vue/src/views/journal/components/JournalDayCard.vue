<template>
  <!--begin::저널 일자 카드-->
  <div class="journal-day" :id="'journal-day-' + day.stdrdDt" :data-stdrd-dt="day.stdrdDt">

    <!--begin::헤더-->
    <div class="journal-day-header" :data-date="day.stdrdDt">
      <div class="col-12 col-md-1 d-flex flex-wrap align-items-center fs-5 fw-bold">
        <div :class="{ 'text-danger': day.isHolyday }" style="column-gap: .25rem">
          <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': day.isHolyday }"></i>
          <!--begin::날짜 (클릭 → 상세 모달)-->
          <span
            v-if="day.id"
            class="cursor-pointer opacity-hover text-decoration-underline"
            @click="openDtl"
          >{{ day.stdrdDt }}</span>
          <span v-else>{{ day.stdrdDt }}</span>
          <!--end::날짜-->
          <span class="fs-8" :class="day.isHolyday ? 'text-danger' : 'text-gray-600'">({{ day.journalDateWeekDay }})</span>
          <span v-if="day.journalDatePrecision === 'APPROXIMATE'" class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
          <span v-if="day.journalDatePrecision === 'UNKNOWN'" class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
          <span class="fs-7 ms-4 text-muted" v-html="day.weather"></span>
          <div v-if="day.holydayNm" class="w-100 ps-5 fs-6 fw-normal text-truncate">{{ day.holydayNm }}</div>
        </div>
      </div>
      <div class="col-3 d-none d-md-flex align-items-center gap-2">
        <!--begin::일자 수정 버튼-->
        <button
          v-if="day.id"
          type="button"
          class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
          title="저널 일자 수정"
          @click="openReg"
        >
          <i class="bi bi-pencil fs-5"></i>
        </button>
        <!--end::일자 수정 버튼-->
        <!--begin::챕터 등록 버튼 (TODO: 챕터 등록 모달 연결)-->
        <button
          v-if="showDiaries"
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
          @click="openChapterReg"
        >
          <i class="bi bi-list-columns-reverse fs-4 pe-1"></i>
          저널 챕터 등록
        </button>
        <!--end::챕터 등록 버튼-->
        <!--begin::꿈 등록 버튼-->
        <button
          v-if="showDreams"
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
          @click="openDreamReg"
        >
          <i class="bi bi-moon-stars fs-4 pe-1"></i>
          저널 꿈 등록
        </button>
        <!--end::꿈 등록 버튼-->
        <!--begin::메타 툴팁 버튼-->
        <button
          v-if="hasMeta"
          class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
          data-bs-toggle="tooltip"
          data-bs-placement="top"
          data-bs-dismiss="click"
          data-bs-html="true"
          data-bs-custom-class="meta-tooltip"
          data-bs-sanitize="false"
          :title="metaTooltipHtml"
        >
          <i class="bi bi-bar-chart"></i>
        </button>
        <!--end::메타 툴팁 버튼-->
      </div>
    </div>
    <!--end::헤더-->

    <!--begin::태그 행-->
    <div v-if="hasVisibleTags" class="row">
      <div class="col-1 d-none d-md-flex"></div>
      <div class="col">
        <div class="ms-5 mt-3">
          <i class="bi bi-tag"></i>
          <span
            v-for="tag in tagList"
            :key="tag.tagId + ':' + tag.name"
            class="text-muted cursor-pointer pe-1"
            @click="openTagDtl(tag)"
          >
            #<span class="border-bottom text-primary fw-lighter opacity-hover">
              <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
              {{ tag.name }}
            </span>
          </span>
        </div>
      </div>
    </div>
    <!--end::태그 행-->

    <!--begin::본문-->
    <div class="journal-day-content row p-5">
      <!--begin::일기 챕터 목록-->
      <template v-if="showDiaries">
        <div v-if="hiddenChapterCtgrList.length > 0" class="d-flex align-items-center mb-3">
          <div class="d-flex flex-wrap align-items-center gap-2 ps-1 ps-md-5">
            <span class="badge badge-light-warning text-warning fw-semibold">CHAPTER FILTER</span>
            <span class="text-muted fs-7">숨겨진 카테고리:</span>
            <span
              v-for="ctgr in hiddenChapterCtgrList"
              :key="ctgr.categoryCode"
              class="badge badge-light-secondary text-muted"
            >{{ ctgr.categoryName }} {{ ctgr.categoryCode }}</span>
          </div>
        </div>
        <JournalChapterItem
          v-for="chapter in journalChapterList"
          :key="'chapter-' + chapter.id"
          :chapter="chapter"
        />
      </template>
      <!--end::일기 챕터 목록-->

      <!--begin::꿈 목록-->
      <template v-if="showDreams">
        <JournalEntryItem
          v-for="dream in journalDreamList"
          :key="'dream-' + dream.id"
          :entry="dream"
          :is-dream="true"
        />
        <JournalEntryItem
          v-for="dream in journalElseDreamList"
          :key="'else-dream-' + dream.id"
          :entry="dream"
          :is-dream="true"
        />
      </template>
      <template v-else-if="hasDream">
        <div class="d-flex align-items-center mt-2">
          <div class="col ps-1 ps-md-5">
            <span class="badge badge-light-secondary text-muted fw-normal">
              <i class="bi bi-moon-stars me-1"></i>
              꿈 숨김
            </span>
          </div>
        </div>
      </template>
      <!--end::꿈 목록-->
    </div>
    <!--end::본문-->

  </div>
  <!--end::저널 일자 카드-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { JournalDayDto } from "@/stores/journal";
import { useJournalModalStore } from "@/stores/journalModal";
import JournalChapterItem from "./JournalChapterItem.vue";
import JournalEntryItem from "./JournalEntryItem.vue";

const props = defineProps<{
  day: JournalDayDto;
  showDiaries?: boolean;
  showDreams?: boolean;
}>();

const modalStore = useJournalModalStore();

const tagList = computed(() => props.day.tag?.list ?? []);
const metaList = computed(() => props.day.meta?.list ?? []);
const journalChapterList = computed(() => props.day.journalChapterList ?? []);
const journalDreamList = computed(() => props.day.journalDreamList ?? []);
const journalElseDreamList = computed(() => props.day.journalElseDreamList ?? []);
const hiddenChapterCtgrList = computed(() => props.day.hiddenChapterCtgrList ?? []);

const hasVisibleTags = computed(() => tagList.value.length > 0);
const hasMeta = computed(() => metaList.value.length > 0);
const hasDream = computed(() =>
  props.day.hasDream === true ||
  journalDreamList.value.length + journalElseDreamList.value.length > 0
);

/** 상세 모달 열기 */
function openDtl() {
  if (props.day.id) void modalStore.openDayDtl(props.day.id);
}

/** 수정 모달 열기 */
function openReg() {
  modalStore.openDayReg({
    id: props.day.id,
    journalDate: props.day.journalDate ?? props.day.stdrdDt,
    journalDatePrecision: props.day.journalDatePrecision,
    weather: props.day.weather,
  });
}


/** 챕터 등록 모달 열기 */
function openChapterReg() {
  if (!props.day.id) return;
  modalStore.openChapterReg({
    journalDayId: props.day.id,
    stdrdDt: props.day.stdrdDt,
    journalDateWeekDay: props.day.journalDateWeekDay,
  });
}

/** 꿈 엔트리 등록 모달 열기 */
function openDreamReg() {
  if (!props.day.id) return;
  void modalStore.openDreamEntryReg({
    journalDayId: props.day.id,
    stdrdDt: props.day.stdrdDt ?? "",
    journalDateWeekDay: props.day.journalDateWeekDay,
  });
}

/** 태그 상세 모달 열기 */
function openTagDtl(tag: { tagId: number; name: string }) {
  void modalStore.openTagDtl(tag.tagId, tag.name);
}

/** HTML 특수문자를 이스케이프한다. */
function escapeHtml(value: string | number | undefined | null): string {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

/** 메타 툴팁 HTML 생성 — Bootstrap tooltip data-bs-html="true" 용. */
const metaTooltipHtml = computed(() =>
  metaList.value.map((meta) => {
    const metaId = escapeHtml(meta.metaId);
    const category = meta.ctgr
      ? `<span class='text-noti pe-1'>[${escapeHtml(meta.ctgr)}]</span>`
      : "";
    const value = `${escapeHtml(meta.value)}${escapeHtml(meta.unit)}`;
    return (
      `<div id='meta-id-${metaId}' class='cursor-pointer btn btn-sm btn-bg-light btn-active-color-primary meta-item' data-meta-id='${metaId}'>` +
      `${category} ${escapeHtml(meta.name)}: <span class='text-dialog'>${value}</span>` +
      `</div>`
    );
  }).join("")
);
</script>