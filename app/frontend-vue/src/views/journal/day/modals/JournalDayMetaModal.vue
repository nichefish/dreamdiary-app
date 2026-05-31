<template>
  <!--begin::저널 일자 필터 모달 (메타/태그 다중 AND 검색)-->
  <div ref="modalEl" class="modal fade" id="journal_day_meta_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">
            저널 일자 필터:
            <template v-for="(sm, idx) in selectedMetas" :key="'hd-meta-' + sm.metaId">
              <span class="text-primary">{{ sm.metaName }}</span>
              <span v-if="idx < selectedMetas.length - 1 || selectedTags.length > 0" class="text-muted mx-1">+</span>
            </template>
            <template v-for="(st, idx) in selectedTags" :key="'hd-tag-' + st.tagId">
              <span class="text-success">#{{ st.tagName }}</span>
              <span v-if="idx < selectedTags.length - 1" class="text-muted mx-1">+</span>
            </template>
            <span v-if="payload" class="text-muted fs-7 ms-2">-- {{ dayCount }}개</span>
          </h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="modalStore.filterModalLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <!--begin::필터 일자 목록-->
          <div v-else-if="payload" id="journal_day_meta_div">
            <div class="d-flex flex-column mb-4">

              <!--begin::컨트롤 행 (연도 선택 + 선택 필터 칩)-->
              <div class="d-flex align-items-center gap-4 mb-4 flex-wrap">
                <!--begin::연도 선택기-->
                <div class="d-flex align-items-center gap-2 flex-shrink-0">
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

                <!--begin::선택 필터 칩 목록-->
                <div class="d-flex align-items-center gap-2 flex-wrap">
                  <i class="bi bi-funnel-fill text-primary fs-6 flex-shrink-0"></i>
                  <!--begin::메타 칩-->
                  <span
                    v-for="sm in selectedMetas"
                    :key="'chip-meta-' + sm.metaId"
                    class="badge badge-light-primary rounded-pill d-inline-flex align-items-center gap-2 px-3 py-2 fs-7"
                  >
                    {{ sm.metaName }}
                    <i
                      class="bi bi-x-circle-fill cursor-pointer opacity-75"
                      :title="sm.metaName + ' 제거'"
                      @click.stop="removeMeta(sm.metaId)"
                    ></i>
                  </span>
                  <!--end::메타 칩-->
                  <!--begin::태그 칩-->
                  <span
                    v-for="st in selectedTags"
                    :key="'chip-tag-' + st.tagId"
                    class="badge badge-light-success rounded-pill d-inline-flex align-items-center gap-2 px-3 py-2 fs-7"
                  >
                    #{{ st.tagName }}
                    <i
                      class="bi bi-x-circle-fill cursor-pointer opacity-75"
                      :title="'#' + st.tagName + ' 제거'"
                      @click.stop="removeTag(st.tagId)"
                    ></i>
                  </span>
                  <!--end::태그 칩-->
                </div>
                <!--end::선택 필터 칩 목록-->
              </div>
              <!--end::컨트롤 행-->

              <!--begin::일자 목록-->
              <template
                v-for="(day, index) in filteredList"
                :key="'filter-day-' + index + '-' + (day.stdrdDt ?? '')"
              >
                <!--begin::월 구분 헤더-->
                <div v-if="showMonthHeader(day, index)" class="d-flex-center mt-6 mb-4 fs-5 text-dark">
                  {{ day.yy }}년 {{ day.mnth }}월
                </div>
                <!--end::월 구분 헤더-->

                <!--begin::일자 행 (날짜 | 오른쪽 컨텐츠)-->
                <div class="d-flex align-items-start gap-2 mb-2">
                  <!--begin::날짜 영역 (고정, 세로 상단 정렬)-->
                  <div
                    class="d-flex align-items-center gap-1 flex-shrink-0 pt-1"
                    :class="{ 'text-danger': day.isHolyday }"
                  >
                    <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': day.isHolyday }"></i>
                    {{ day.stdrdDt }}
                    <span class="fs-8" :class="day.isHolyday ? 'text-danger' : 'text-gray-600'">({{ day.journalDateWeekDay }})</span>
                    <button
                      type="button"
                      class="btn btn-icon btn-sm btn-light-primary"
                      title="새 창으로 보기 (일자 뷰)"
                      @click="openDailyView(day.stdrdDt)"
                    ><i class="bi bi-box-arrow-up-right fs-8 p-0"></i></button>
                    <span class="fs-7 ms-1 text-muted" v-html="day.weather"></span>
                  </div>
                  <!--end::날짜 영역-->

                  <!--begin::오른쪽 컨텐츠 (메타 행 + 태그 행)-->
                  <div class="d-flex flex-column gap-1">

                    <!--begin::메타 행 (선택 메타 값 + 비선택 메타 칩)-->
                    <div class="d-flex align-items-center gap-3 flex-wrap">
                      <!--begin::선택된 메타 값-->
                      <div
                        v-for="(metaRow, mIdx) in selectedMetaRows(day)"
                        :key="'sm-' + index + '-' + mIdx"
                      >
                        <span v-if="metaRow.ctgr" class="text-noti pe-1">[{{ metaRow.ctgr }}]</span>
                        {{ metaRow.name }}
                        <span class="text-dialog">: {{ metaRow.value }}{{ metaRow.unit }}</span>
                      </div>
                      <!--end::선택된 메타 값-->

                      <!--begin::비선택 메타 (클릭 시 필터 추가)-->
                      <span
                        v-for="(other, oIdx) in otherMetaRows(day)"
                        :key="'om-' + index + '-' + oIdx"
                        class="badge badge-light rounded-pill cursor-pointer d-inline-flex align-items-center gap-1 px-2 py-1 fs-8"
                        :title="(other.ctgr ? '[' + other.ctgr + '] ' : '') + (other.name ?? '') + ' 필터 추가'"
                        @click="addMeta(String(other.metaId ?? ''), other.name ?? '', other.ctgr)"
                      >
                        <span v-if="other.ctgr" class="text-noti">[{{ other.ctgr }}]</span>
                        {{ other.name }}
                        <span class="text-muted">{{ other.value }}{{ other.unit }}</span>
                        <i class="bi bi-plus-circle text-primary"></i>
                      </span>
                      <!--end::비선택 메타-->
                    </div>
                    <!--end::메타 행-->

                    <!--begin::태그 행-->
                    <div v-if="(day.tag?.list?.length ?? 0) > 0" class="d-flex flex-wrap gap-1">
                      <!--begin::선택된 태그 (굵게, 클릭 시 필터 제거)-->
                      <span
                        v-for="tag in selectedTagItems(day)"
                        :key="'st-tag-' + tag.tagId"
                        class="text-muted cursor-pointer"
                        :title="'#' + (tag.ctgr ? '[' + tag.ctgr + '] ' : '') + tag.name + ' 필터 제거'"
                        @click.stop="removeTag(String(tag.tagId))"
                      >
                        #<span class="border-bottom text-success fw-bold opacity-hover">
                          <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                          {{ tag.name }}
                        </span>
                      </span>
                      <!--end::선택된 태그-->
                      <!--begin::비선택 태그 (클릭 시 필터 추가)-->
                      <span
                        v-for="tag in otherTagItems(day)"
                        :key="'ot-tag-' + tag.tagId"
                        class="text-muted cursor-pointer"
                        :title="'#' + (tag.ctgr ? '[' + tag.ctgr + '] ' : '') + tag.name + ' 필터 추가'"
                        @click.stop="addTag(String(tag.tagId), tag.name, tag.ctgr)"
                      >
                        #<span class="border-bottom text-primary fw-lighter opacity-hover">
                          <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                          {{ tag.name }}
                        </span>
                      </span>
                      <!--end::비선택 태그-->
                    </div>
                    <!--end::태그 행-->

                  </div>
                  <!--end::오른쪽 컨텐츠-->
                </div>
                <!--end::일자 행-->

              </template>
              <!--end::일자 목록-->

            </div>
          </div>
          <!--end::필터 일자 목록-->

          <div v-else class="text-center text-muted py-10">
            데이터가 없습니다.
          </div>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 일자 필터 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useJournalModalStore } from "@/stores/journalModal";
import type { JournalDayDto, MetaContentItem, TagItem } from "@/stores/journal";

const modalStore = useJournalModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const payload = computed(() => modalStore.filterModalPayload);

/** 선택된 메타 필터 */
interface SelectedMeta {
  metaId: string;
  metaName: string;
}
const selectedMetas = ref<SelectedMeta[]>([]);

/** 선택된 태그 필터 */
interface SelectedTag {
  tagId: string;
  tagName: string;
}
const selectedTags = ref<SelectedTag[]>([]);

/**
 * payload 객체 참조가 바뀔 때마다(신규 오픈·연도 변경) 실행된다.
 * 필터가 비어 있으면(신규 오픈) 시드로 초기화하고,
 * 필터가 남아 있으면(연도 변경 재조회) 기존 필터를 그대로 유지한다.
 */
watch(
  () => payload.value,
  (p) => {
    if (!p) return;
    // 연도 변경 재조회 시에는 기존 필터(사용자가 추가한 메타·태그)를 보존한다.
    if (selectedMetas.value.length > 0 || selectedTags.value.length > 0) return;
    if (p.seedType === "meta") {
      /** ctgr 을 list 에서 탐색하여 displayName 구성 */
      let ctgr: string | undefined = p.seedCtgr;
      if (!ctgr) {
        for (const day of p.list ?? []) {
          const match = (day.meta?.list ?? []).find((r) => String(r.metaId ?? "") === p.seedId);
          if (match?.ctgr) { ctgr = match.ctgr; break; }
        }
      }
      const displayName = ctgr ? `[${ctgr}] ${p.seedName}` : p.seedName;
      selectedMetas.value = [{ metaId: p.seedId, metaName: displayName }];
      selectedTags.value = [];
    } else {
      const displayName = p.seedCtgr ? `[${p.seedCtgr}] ${p.seedName}` : p.seedName;
      selectedTags.value = [{ tagId: p.seedId, tagName: displayName }];
      selectedMetas.value = [];
    }
  },
  { immediate: true }
);

/**
 * 선택된 메타·태그를 모두 보유한 날짜만 필터링한다(AND 조건).
 * 선택 필터가 하나도 없으면 빈 목록을 반환한다.
 */
const filteredList = computed(() => {
  const list = payload.value?.list ?? [];
  const metaIds = selectedMetas.value.map((m) => m.metaId);
  const tagIds = selectedTags.value.map((t) => t.tagId);
  if (metaIds.length === 0 && tagIds.length === 0) return [];
  return list.filter((day) => {
    const dayMetaIds = (day.meta?.list ?? []).map((r) => String(r.metaId ?? ""));
    const dayTagIds = (day.tag?.list ?? []).map((t) => String(t.tagId));
    return metaIds.every((id) => dayMetaIds.includes(id))
        && tagIds.every((id) => dayTagIds.includes(id));
  });
});

/** 현재 필터링된 일자 수 */
const dayCount = computed(() => filteredList.value.length);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeDayFilterModal();
      // 모달이 닫힐 때 필터 상태를 초기화한다.
      // 다음 오픈 시 payload watch 가 신규 시드로 재초기화할 수 있도록 비워 둔다.
      selectedMetas.value = [];
      selectedTags.value = [];
    });
  }
});

watch(
  () => modalStore.filterModalOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close(): void {
  modalStore.closeDayFilterModal();
}

/** 연도 변경 시 해당 연도 데이터를 재조회한다. */
function onYyChange(event: Event): void {
  const target = event.target as HTMLSelectElement | null;
  if (!target || !payload.value) return;
  void modalStore.openDayFilterModal(
    { type: payload.value.seedType, id: payload.value.seedId, name: payload.value.seedName, ctgr: payload.value.seedCtgr },
    target.value
  );
}

/** 선택 메타 필터에 항목을 추가한다. 이미 선택된 metaId면 무시한다. */
function addMeta(metaId: string, metaName: string, ctgr?: string): void {
  if (!selectedMetas.value.some((m) => m.metaId === metaId)) {
    const displayName = ctgr ? `[${ctgr}] ${metaName}` : metaName;
    selectedMetas.value = [...selectedMetas.value, { metaId, metaName: displayName }];
  }
}

/** 선택 메타 필터에서 항목을 제거한다. */
function removeMeta(metaId: string): void {
  selectedMetas.value = selectedMetas.value.filter((m) => m.metaId !== metaId);
}

/** 선택 태그 필터에 항목을 추가한다. 이미 선택된 tagId면 무시한다. */
function addTag(tagId: string, tagName: string, ctgr?: string): void {
  if (!selectedTags.value.some((t) => t.tagId === tagId)) {
    const displayName = ctgr ? `[${ctgr}] ${tagName}` : tagName;
    selectedTags.value = [...selectedTags.value, { tagId, tagName: displayName }];
  }
}

/** 선택 태그 필터에서 항목을 제거한다. */
function removeTag(tagId: string): void {
  selectedTags.value = selectedTags.value.filter((t) => t.tagId !== tagId);
}

/** 월 구분 헤더를 표시할 경계인지 확인한다. */
function showMonthHeader(day: JournalDayDto, index: number): boolean {
  if (index === 0) return true;
  const prev = filteredList.value[index - 1];
  return prev?.mnth !== day.mnth || prev?.yy !== day.yy;
}

/**
 * 해당 일자에서 선택된 메타 ID와 일치하는 메타 행을 반환한다.
 * selectedMetas 배열 순서를 유지하여 행마다 표시 순서가 일관되도록 한다.
 */
function selectedMetaRows(day: JournalDayDto): MetaContentItem[] {
  const metaList = day.meta?.list ?? [];
  return selectedMetas.value
    .map((m) => metaList.find((r) => String(r.metaId ?? "") === m.metaId))
    .filter((r): r is MetaContentItem => r !== undefined);
}

/** 해당 일자에서 선택되지 않은 나머지 메타 행을 반환한다. */
function otherMetaRows(day: JournalDayDto): MetaContentItem[] {
  const ids = selectedMetas.value.map((m) => m.metaId);
  return (day.meta?.list ?? []).filter((r) => !ids.includes(String(r.metaId ?? "")));
}

/** 해당 일자에서 선택된 태그 ID와 일치하는 태그를 반환한다. */
function selectedTagItems(day: JournalDayDto): TagItem[] {
  const ids = selectedTags.value.map((t) => t.tagId);
  return (day.tag?.list ?? []).filter((t) => ids.includes(String(t.tagId)));
}

/** 해당 일자에서 선택되지 않은 나머지 태그를 반환한다. */
function otherTagItems(day: JournalDayDto): TagItem[] {
  const ids = selectedTags.value.map((t) => t.tagId);
  return (day.tag?.list ?? []).filter((t) => !ids.includes(String(t.tagId)));
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