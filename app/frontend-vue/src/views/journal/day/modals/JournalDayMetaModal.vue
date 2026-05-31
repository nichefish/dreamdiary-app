<template>
  <!--begin::저널 일자 메타 조회 모달-->
  <div ref="modalEl" class="modal fade" id="journal_day_meta_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">
            저널 일자 메타:
            <span v-if="payload" class="text-primary">{{ payload.metaName }}</span>
            <span v-if="payload" class="text-muted fs-7 ms-2">-- {{ dayCount }}개</span>
          </h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="modalStore.metaModalLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <!--begin::메타 일자 목록-->
          <div v-else-if="payload" id="journal_day_meta_div">
            <div class="d-flex flex-column mb-4">

              <!--begin::연도 선택기-->
              <div class="d-flex justify-content-start align-items-center gap-3 mb-4">
                <label for="journal_day_meta_yy" class="form-label mb-0 fw-bold">연도</label>
                <select
                  id="journal_day_meta_yy"
                  class="form-select form-select-sm w-auto"
                  :value="payload.yy"
                  @change="onYyChange"
                >
                  <option
                    v-for="opt in payload.yearOptions"
                    :key="'yy-' + opt.value"
                    :value="String(opt.value)"
                  >{{ opt.label }}</option>
                </select>
              </div>
              <!--end::연도 선택기-->

              <!--begin::일자 목록-->
              <template
                v-for="(day, index) in payload.list"
                :key="'meta-day-' + index + '-' + (day.stdrdDt ?? '')"
              >
                <!--begin::월 구분 헤더-->
                <div v-if="showMonthHeader(day, index)" class="d-flex-center mt-6 mb-4 fs-5 text-dark">
                  {{ day.yy }}년 {{ day.mnth }}월
                </div>
                <!--end::월 구분 헤더-->

                <div class="d-flex align-items-center gap-2 mb-2">
                  <!--begin::일자-->
                  <div class="d-flex align-items-center gap-1" :class="{ 'text-danger': day.isHolyday }">
                    <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': day.isHolyday }"></i>
                    {{ day.stdrdDt }}
                    <span class="fs-8" :class="day.isHolyday ? 'text-danger' : 'text-gray-600'">({{ day.journalDateWeekDay }})</span>
                    <!--begin::새 창으로 보기-->
                    <button
                      type="button"
                      class="btn btn-icon btn-sm btn-light-primary"
                      title="새 창으로 보기 (일자 뷰)"
                      @click="openDailyView(day.stdrdDt)"
                    ><i class="bi bi-box-arrow-up-right fs-8 p-0"></i></button>
                    <!--end::새 창으로 보기-->
                    <span class="fs-7 ms-1 text-muted" v-html="day.weather"></span>
                  </div>
                  <!--end::일자-->

                  <!--begin::메타 값-->
                  <template
                    v-for="(metaRow, mIdx) in matchingMetaRows(day)"
                    :key="'mm-' + index + '-' + mIdx"
                  >
                    <div>
                      <span v-if="metaRow.ctgr" class="text-noti pe-1">[{{ metaRow.ctgr }}]</span>
                      {{ metaRow.name }}
                      <span class="text-dialog">: {{ metaRow.value }}{{ metaRow.unit }}</span>
                    </div>
                  </template>
                  <!--end::메타 값-->

                  <!--begin::태그 목록-->
                  <div v-if="tagList(day).length > 0" class="ms-2">
                    <span
                      v-for="tag in tagList(day)"
                      :key="String(tag.tagId) + ':' + String(tag.name)"
                      class="text-muted cursor-pointer pe-1"
                    >
                      #
                      <span class="border-bottom text-primary fw-lighter opacity-hover">
                        <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                        {{ tag.name }}
                      </span>
                    </span>
                  </div>
                  <!--end::태그 목록-->
                </div>
              </template>
              <!--end::일자 목록-->

            </div>
          </div>
          <!--end::메타 일자 목록-->

          <div v-else class="text-muted text-center py-10">조회된 데이터가 없습니다.</div>
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
  <!--end::저널 일자 메타 조회 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useJournalModalStore } from "@/stores/journalModal";
import type { JournalDayDto, MetaContentItem, TagItem } from "@/stores/journal";

const modalStore = useJournalModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const payload = computed(() => modalStore.metaModalPayload);
const dayCount = computed(() => payload.value?.list?.length ?? 0);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    /* bootstrap 이벤트로 store와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeMetaModal();
    });
  }
});

watch(
  () => modalStore.metaModalOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  modalStore.closeMetaModal();
}

/** 연도 변경 시 해당 연도 데이터를 재조회한다. */
function onYyChange(event: Event) {
  const target = event.target as HTMLSelectElement | null;
  if (!target || !payload.value) return;
  void modalStore.openMetaModal(payload.value.metaId, target.value, payload.value.metaName);
}

/** 월이 바뀌는 경계인지 확인한다. */
function showMonthHeader(day: JournalDayDto, index: number): boolean {
  if (!payload.value) return false;
  const list = payload.value.list;
  if (index === 0) return true;
  const prev = list[index - 1];
  return String(day?.mnth ?? "") !== String(prev?.mnth ?? "");
}

/** 해당 일자에서 현재 선택된 메타 ID와 일치하는 메타 행을 반환한다. */
function matchingMetaRows(day: JournalDayDto): MetaContentItem[] {
  if (!payload.value) return [];
  const targetId = String(payload.value.metaId ?? "");
  const metaList: MetaContentItem[] = day?.meta?.list ?? [];
  return metaList.filter((row) => String(row?.metaId ?? "") === targetId);
}

/** 해당 일자의 태그 목록을 반환한다. */
function tagList(day: JournalDayDto): TagItem[] {
  return day?.tag?.list ?? [];
}

/** 일자 뷰를 새 창으로 연다. */
function openDailyView(stdrdDt: string | undefined): void {
  if (!stdrdDt) return;
  const base = import.meta.env.BASE_URL.replace(/\/$/, "");
  const w = Math.min(1200, window.screen.availWidth);
  const h = Math.min(900, window.screen.availHeight);
  window.open(`${base}/journal/daily?stdrdDt=${stdrdDt}`, "_blank", `width=${w},height=${h},left=100,top=60`);
}
</script>