<template>
  <!--begin::결산 목록-->
  <div class="journal-annual-list-vue">

    <!--begin::총 집계 카드-->
    <div class="card post mb-4">
      <div class="card-body">
        <div class="d-flex-between fs-5">
          <div class="d-flex fs-5">
            <div class="text-gray-700 d-flex-center me-5">
              <span class="fw-bold me-2">총 꿈 기록</span>
              <i class="bi bi-moon-stars fs-4 me-2"></i>
              <template v-if="store.totalLoading">
                <span class="spinner-border spinner-border-sm text-primary" role="status"></span>
              </template>
              <template v-else-if="store.totalAnnual">
                (<span class="text-info fw-bold mx-1">{{ store.totalAnnual.dreamDayCnt ?? 0 }}</span>일
                /
                <span class="text-info fw-bold mx-1">{{ store.totalAnnual.dreamCnt ?? 0 }}</span>건)
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--end::총 집계 카드-->

    <!--begin::로딩-->
    <div v-if="store.loading" class="d-flex justify-content-center py-10">
      <span class="spinner-border text-primary" role="status"></span>
    </div>
    <!--end::로딩-->

    <!--begin::에러-->
    <div v-else-if="store.error" class="alert alert-danger">{{ store.error }}</div>
    <!--end::에러-->

    <!--begin::빈 목록-->
    <div v-else-if="!store.annualList.length" class="text-center text-muted py-10 fs-6">
      결산 데이터가 없습니다.
    </div>
    <!--end::빈 목록-->

    <!--begin::결산 카드 목록-->
    <template v-else>
      <div v-for="annual in store.annualList" :key="annual.yy ?? annual.id" class="card post mb-3">
        <div class="card-body py-5">
          <div class="row journal-sumry align-items-center">

            <!--begin::연도 + 제목 링크-->
            <div class="col-2 d-flex align-items-center">
              <i class="bi bi-calendar3 fs-6 me-2"></i>
              <a
                class="cursor-pointer text-underline-dotted text-dark"
                @click="gotoDetail(annual.yy!)"
              >
                <template v-if="annual.title">
                  <span class="fs-5 me-1">{{ annual.title }}</span>
                </template>
                <template v-else>
                  <span class="fs-5 fw-bolder me-0">{{ annual.yy }}</span>
                  년 결산
                </template>
                <i class="bi bi-pencil-square fs-4 ms-1"></i>
              </a>
            </div>
            <!--end::연도 + 제목 링크-->

            <!--begin::꿈 통계 + 본문 + 태그-->
            <div class="col fs-5">
              <div class="d-flex justify-content-start">
                <div class="text-gray-700 d-flex-center me-5">
                  <span class="fw-bold me-2">꿈</span>
                  <template v-if="annual.dreamComptYn === 'Y'">
                    <span class="cursor-help">
                      <i class="bi bi-moon-stars-fill fs-4 me-2 text-success"></i>
                      <i class="bi bi-check text-success" style="margin-left:-0.8rem"></i>
                    </span>
                  </template>
                  <template v-else>
                    <span><i class="bi bi-moon-stars fs-4 me-2"></i></span>
                  </template>
                  (<span class="text-info fw-bold mx-1">{{ annual.dreamDayCnt }}</span>일
                  /
                  <span class="text-info fw-bold mx-1">{{ annual.dreamCnt }}</span>건)
                </div>
              </div>
              <!--begin::마크다운 본문-->
              <div class="mt-2">
                <div
                  class="fs-6 fw-normal text-gray-800 ps-2 pt-3 text-noti"
                  v-html="annual.markdownContent"
                ></div>
              </div>
              <!--end::마크다운 본문-->
              <!--begin::태그 목록-->
              <!-- TODO: 태그 클릭 → JournalDayTagService 연동 미구현 (Sub-2 범위 외) -->
              <div v-if="hasTags(annual)" class="mt-2 ms-5 mt-3">
                <i class="bi bi-tag"></i>
                <span
                  v-for="tag in annual.tag?.list"
                  :key="(tag.tagId as number | string) + ':' + tag.name"
                  class="text-muted pe-1"
                >
                  #<span class="border-bottom text-primary fw-lighter opacity-hover">
                    <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                    {{ tag.name }}
                  </span>
                </span>
              </div>
              <!--end::태그 목록-->
            </div>
            <!--end::꿈 통계 + 본문 + 태그-->

            <!--begin::구분선-->
            <div class="col-1 ms-4 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
            <!--end::구분선-->

            <!--begin::컨텍스트 버튼-->
            <div class="col-1 py-3 d-none d-md-flex-between w-75px ps-2 gap-1">
              <button
                type="button"
                class="btn btn-sm btn-icon btn-light-primary"
                title="수정"
                @click="openModify(annual.yy!)"
              >
                <i class="bi bi-pencil fs-5"></i>
              </button>
              <button
                type="button"
                class="btn btn-sm btn-icon btn-light"
                title="상세"
                @click="gotoDetail(annual.yy!)"
              >
                <i class="bi bi-arrow-right fs-5"></i>
              </button>
            </div>
            <!--end::컨텍스트 버튼-->

          </div>
        </div>
      </div>
    </template>
    <!--end::결산 카드 목록-->

  </div>
  <!--end::결산 목록-->
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useJournalAnnualStore } from "@/stores/journalAnnual";
import type { JournalAnnualDto } from "@/stores/journalAnnual";

const router = useRouter();
const store = useJournalAnnualStore();

onMounted(() => {
  void store.fetchList();
  void store.fetchTotal();
});

/** 결산 상세 페이지로 이동한다. */
function gotoDetail(yy: number) {
  void router.push({ name: "annual-detail", params: { yy: String(yy) } });
}

/** 결산 수정 모달을 연다. */
function openModify(yy: number) {
  void store.openModify(yy);
}

/** 태그 보유 여부 확인 */
function hasTags(annual: JournalAnnualDto): boolean {
  return Array.isArray(annual.tag?.list) && annual.tag!.list!.length > 0;
}
</script>
