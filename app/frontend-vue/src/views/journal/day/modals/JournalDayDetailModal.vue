<template>
  <!--begin::저널 일자 상세 모달-->
  <div ref="modalEl" class="modal fade" id="journal_day_detail_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl" style="--bs-modal-width: min(95vw, 1400px);">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">저널 일자 조회</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">

          <!--begin::로딩-->
          <div v-if="modalStore.dayDetailLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <!--begin::내용-->
          <template v-else-if="day">
            <!--begin::날짜 헤더-->
            <div class="journal-day-header mb-4">
              <span class="fs-5 fw-bold" :class="{ 'text-danger': day.isHolyday }">
                <i class="bi bi-calendar3 fs-6 me-1"></i>
                {{ day.stdrdDt }}
                <span class="fs-8" :class="day.isHolyday ? 'text-danger' : 'text-gray-600'">
                  ({{ day.journalDateWeekDay }})
                </span>
                <span v-if="day.journalDatePrecision === 'APPROXIMATE'" class="badge badge-light-primary ms-2">APPROXIMATE</span>
                <span v-if="day.journalDatePrecision === 'UNKNOWN'" class="badge badge-light-primary ms-2">UNKNOWN</span>
                <span class="fs-7 ms-4 text-muted" v-html="day.weather"></span>
              </span>
              <span v-if="day.holydayNm" class="ms-3 fs-6 fw-normal text-muted">{{ day.holydayNm }}</span>
            </div>
            <!--end::날짜 헤더-->

            <!--begin::태그-->
            <div v-if="tagList.length > 0" class="mb-3 ms-2">
              <i class="bi bi-tag"></i>
              <span
                v-for="tag in tagList"
                :key="tag.tagId + ':' + tag.name"
                class="text-muted pe-1"
              >
                #<span class="border-bottom text-primary fw-lighter">
                  <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                  {{ tag.name }}
                </span>
              </span>
            </div>
            <!--end::태그-->

            <!--begin::챕터 목록-->
            <div class="journal-day-content row p-3">
              <template v-if="journalChapterList.length > 0">
                <JournalChapterItem
                  v-for="chapter in journalChapterList"
                  :key="'dtl-chapter-' + chapter.id"
                  :chapter="chapter"
                  entry-dom-id-prefix="journal-day-dtl-entry-"
                />
              </template>
              <!--begin::꿈 목록-->
              <JournalEntryItem
                v-for="dream in journalDreamList"
                :key="'dtl-dream-' + dream.id"
                :dom-id="dream.id ? 'journal-day-dtl-entry-' + dream.id : undefined"
                :entry="dream"
                :is-dream="true"
              />
              <JournalEntryItem
                v-for="dream in journalElseDreamList"
                :key="'dtl-else-dream-' + dream.id"
                :dom-id="dream.id ? 'journal-day-dtl-entry-' + dream.id : undefined"
                :entry="dream"
                :is-dream="true"
              />
              <!--end::꿈 목록-->
              <div v-if="journalChapterList.length === 0 && journalDreamList.length === 0 && journalElseDreamList.length === 0"
                   class="text-muted py-5 text-center">
                내용이 없습니다.
              </div>
            </div>
            <!--end::챕터 목록-->
          </template>
          <!--end::내용-->

          <div v-else class="text-muted py-10 text-center">데이터를 불러오지 못했습니다.</div>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 일자 상세 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useJournalModalStore } from "@/stores/journalModal";
import JournalChapterItem from "../../chapter/components/JournalChapterItem.vue";
import JournalEntryItem from "../../entry/components/JournalEntryItem.vue";

const modalStore = useJournalModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const day = computed(() => modalStore.dayDetailData);
const tagList = computed(() => day.value?.tag?.list ?? []);
const journalChapterList = computed(() => day.value?.journalChapterList ?? []);
const journalDreamList = computed(() => day.value?.journalDreamList ?? []);
const journalElseDreamList = computed(() => day.value?.journalElseDreamList ?? []);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeDayDetail();
    });
  }
});

watch(
  () => modalStore.dayDetailOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  modalStore.closeDayDetail();
}
</script>
