<template>
  <!--begin::저널 메타 페이지-->
  <div class="journal-day-meta-page">
    <JournalDayViewToolbar />

    <!--begin::카드-->
    <div class="card post" style="margin-top: 0 !important;">
      <!--begin::카드 헤더-->
      <div class="card-header min-h-auto mb-7">
        <div id="journal_meta_header" class="mb-6 ms-4 w-100">
          <div class="row align-items-center mb-4 ms-4 min-h-42px">
            <div class="col-auto d-none d-md-flex text-center fs-6">
              <b>메타 : </b>
            </div>
            <!--begin::메타 헤더 목록-->
            <div class="col flex-grow-1">
              <!--begin::로딩-->
              <span v-if="store.metaLoading" class="spinner-border spinner-border-sm text-primary" role="status"></span>
              <!--end::로딩-->
              <div v-else class="journal-day-meta-header-list-vue">
                <template v-if="store.metaList.length > 0">
                  <span
                    v-for="item in store.metaList"
                    :key="'meta-h-' + item.id"
                    class="text-muted cursor-pointer pe-1"
                    @click="store.selectMeta(item)"
                  >
                    #
                    <span class="border-bottom text-primary fw-lighter opacity-hover">
                      <span v-if="item.ctgr" class="fs-7 text-noti">[{{ item.ctgr }}]</span>
                      {{ item.name }}
                    </span>
                  </span>
                </template>
                <template v-else>-</template>
              </div>
            </div>
            <!--end::메타 헤더 목록-->
            <div class="col-auto d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
            <div class="col-auto d-none d-md-flex ms-4 me-20 text-center fs-6 gap-3">&nbsp;</div>
          </div>
        </div>
      </div>
      <!--end::카드 헤더-->

      <!--begin::카드 바디-->
      <div class="card-body">
        <!--begin::설정 스트립-->
        <div class="d-flex-align-center mb-2 ps-2 min-h-42px">
          <div class="journal-day-meta-config-strip-vue d-flex-align-center flex-wrap gap-2">
            <template v-if="store.selectedMeta">
              <span class="text-muted fs-4 pe-1">
                #
                <span class="text-dark fw-bold opacity-hover">
                  <span v-if="store.selectedMeta.ctgr" class="fs-7 text-noti">[{{ store.selectedMeta.ctgr }}]</span>
                  {{ store.selectedMeta.name }}
                </span>
              </span>
              <div class="col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 me-3 w-10px">&nbsp;</div>
              <div class="gap-1 d-flex align-items-center">
                <!--TODO: 메타 설정 모달 미구현-->
                <button
                  type="button"
                  class="btn btn-sm btn-icon btn-outline btn-bg-light btn-active-color-primary"
                  title="메타 설정"
                >
                  <i class="bi bi-gear"></i>
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-icon btn-outline btn-bg-light btn-active-color-primary"
                  title="메타 컨텐츠 목록"
                  @click="openMetaModal"
                >
                  <i class="bi bi-bar-chart"></i>
                </button>
              </div>
            </template>
          </div>
        </div>
        <!--end::설정 스트립-->

        <!--begin::그래프 영역-->
        <div
          id="journal_day_meta_graph_div"
          class="border border-1 border-primary d-flex-align-center"
          style="min-height: 200px;"
        >
          <!--TODO: 메타 그래프 구현 예정-->
        </div>
        <!--end::그래프 영역-->
      </div>
      <!--end::카드 바디-->
    </div>
    <!--end::카드-->

  </div>
  <!--end::저널 메타 페이지-->
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useJournalStore } from "@/stores/journal";
import { useJournalModalStore } from "@/stores/journalModal";
import JournalDayViewToolbar from "./components/JournalDayViewToolbar.vue";

const store = useJournalStore();
const modalStore = useJournalModalStore();

onMounted(() => {
  void store.fetchMetas();
});

/** 선택된 메타의 컨텐츠 목록 모달을 연다. */
function openMetaModal() {
  if (!store.selectedMeta?.id) return;
  void modalStore.openMetaModal(store.selectedMeta.id);
}
</script>
