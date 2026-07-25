<template>
  <!--begin::결산 상세-->
  <div class="journal-annual-detail-vue">

    <!--begin::로딩-->
    <div v-if="store.detailLoading" class="d-flex justify-content-center py-10">
      <span class="spinner-border text-primary" role="status"></span>
    </div>
    <!--end::로딩-->

    <template v-else-if="store.annualDetail">

      <!--begin::결산 요약 카드-->
      <div id="journal_annual_detail_div" class="card post" style="margin-top: 0 !important;">
        <!--begin::Card header-->
        <div class="card-header py-5">
          <div class="col-2 d-flex align-items-center">
            <i class="bi bi-calendar3 fs-5 me-2"></i>
            <template v-if="store.annualDetail.title">
              <span class="fs-4 me-1">{{ store.annualDetail.title }}</span>
            </template>
            <template v-else>
              <span class="fs-4 fw-bolder me-1">{{ store.annualDetail.yy }}</span>
              <span class="fs-5 me-1">{{ t('journal.closing-by-year') }}</span>
            </template>
          </div>
          <div class="d-flex justify-content-start">
            <div class="d-flex-center text-gray-700 fs-6 me-5">
              <span class="fw-bold me-2">{{ t('journal.annual.diary-count') }}</span>
              <template v-if="store.annualDetail.dreamComptYn === 'Y'">
                <span class="cursor-help">
                  <i class="bi bi-moon-stars-fill fs-4 me-2 text-success"></i>
                  <i class="bi bi-check text-success" style="margin-left:-0.8rem"></i>
                </span>
              </template>
              <template v-else>
                <span><i class="bi bi-moon-stars fs-4 me-2"></i></span>
              </template>
              (<span class="text-info fw-bold mx-1">{{ store.annualDetail.dreamDayCnt }}</span>{{ t('common.unit.day') }}
              /
              <span class="text-info fw-bold mx-1">{{ store.annualDetail.dreamCnt }}</span>{{ t('common.unit.count') }})
            </div>
          </div>
        </div>
        <!--end::Card header-->

        <!--begin::Card body-->
        <div class="card-body py-5">
          <!--begin::SUMMARY-->
          <div class="journal-sumry-item">
            <div class="ms-3 fs-6">{{ t("journal.annual.detail.summary") }}</div>
            <div
              class="journal-content p-2 text-noti"
              v-html="store.annualDetail.markdownContent"
            ></div>
            <!--begin::요약 태그-->
            <!-- TODO: 태그 클릭 → JournalDayTagService 연동 미구현 (Sub-2 범위 외) -->
            <div v-if="hasSummaryTags" class="mt-2 ms-5 mt-3">
              <i class="bi bi-tag"></i>
              <span
                v-for="tag in store.annualDetail.tag?.list"
                :key="String(tag.tagId) + ':' + tag.name"
                class="text-muted pe-1"
              >
                #<span class="border-bottom text-primary fw-lighter opacity-hover">
                  <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                  {{ tag.name }}
                </span>
              </span>
            </div>
            <!--end::요약 태그-->
          </div>
          <!--end::SUMMARY-->

          <!--begin::REVIEWS-->
          <div class="journal-sumry-review-container mt-8">
            <div class="d-flex-align-center ms-3 fs-6 gap-5">
              <span>{{ t("journal.annual.detail.reviews") }}</span>
              <button
                type="button"
                class="btn btn-sm btn-light-primary btn-outlined ps-2 pe-3 py-1 cursor-pointer"
                @click="openReviewRegist"
              >
                <i class="bi bi-plus fs-4 pe-0"></i>
                {{ t('journal.annual.review.register') }}
              </button>
            </div>
            <!--begin::리뷰 행-->
            <div
              v-for="rev in reviewList"
              :key="'rev-' + rev.id"
              class="journal-sumry-review-item ms-7 me-15 ps-3"
              :data-id="rev.id"
            >
              <div class="col">
                <div class="journal-sumry-review-content p-2 text-noti">
                  <div class="journal-content" v-html="rev.markdownContent"></div>
                  <!--begin::리뷰 태그-->
                  <div v-if="hasReviewTags(rev)" class="tags ms-2 mt-3">
                    <i class="bi bi-tag"></i>
                    <span
                      v-for="tag in rev.tag?.list"
                      :key="String(tag.tagId) + ':' + tag.name"
                      class="text-muted pe-1"
                    >
                      #<span class="border-bottom text-primary fw-lighter opacity-hover">
                        <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                        {{ tag.name }}
                      </span>
                    </span>
                  </div>
                  <!--end::리뷰 태그-->
                </div>
              </div>
              <!--begin::리뷰 컨텍스트 버튼-->
              <div class="d-flex gap-1 mt-1">
                <button
                  type="button"
                  class="btn btn-sm btn-icon journal-annual-action-btn"
                  :title="t('journal.annual.review.edit.tooltip')"
                  @click="openReviewModify(rev.id!)"
                >
                  <i class="bi bi-pencil fs-6"></i>
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-icon journal-annual-action-btn journal-annual-action-btn--danger"
                  :title="t('journal.annual.review.delete.tooltip')"
                  @click="deleteReview(rev.id!)"
                >
                  <i class="bi bi-trash fs-6"></i>
                </button>
              </div>
              <!--end::리뷰 컨텍스트 버튼-->
            </div>
            <!--end::리뷰 행-->
          </div>
          <!--end::REVIEWS-->
        </div>
        <!--end::Card body-->
      </div>
      <!--end::결산 요약 카드-->

      <!--begin::탭 네비게이션-->
      <div class="card-header min-h-auto mb-7 mt-4">
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
          <li class="nav-item">
            <a
              class="nav-link px-6"
              :class="{ active: store.activeSection === 'DIARY' }"
              @click.prevent="onTabClick('DIARY')"
              href="#"
            >
              <span class="nav-icon pe-1"><i class="bi bi-book"></i></span>
              <span class="nav-text">{{ t('journal.annual.tab.diary') }}</span>
            </a>
          </li>
          <li class="nav-item">
            <a
              class="nav-link px-6"
              :class="{ active: store.activeSection === 'DREAM' }"
              @click.prevent="onTabClick('DREAM')"
              href="#"
            >
              <span class="nav-icon pe-1"><i class="bi bi-moon-stars"></i></span>
              <span class="nav-text">{{ t('journal.annual.tab.dream') }}</span>
            </a>
          </li>
        </ul>
      </div>
      <!--end::탭 네비게이션-->

      <!--begin::태그 헤더-->
      <div v-if="store.showTagCloud" class="card-header">
        <div id="journal_tag_header" class="mb-6 ms-4 w-100">
          <template v-for="row in tagCloudRows" :key="row.id">
            <div class="row align-items-center mb-4 ms-4 min-h-42px">
              <div class="col-auto d-none d-md-flex ms-4 me-6 text-center fs-6">
                <b>{{ row.label }} :</b>
              </div>
              <div class="col flex-grow-1">
                <span v-if="store.entriesLoading" class="text-muted fs-7">{{ t("journal.annual.detail.loading") }}</span>
                <span v-else-if="row.tags.length === 0" class="text-muted fs-7">-</span>
                <span v-else class="d-flex flex-wrap align-items-center">
                  <button
                    v-for="tag in row.tags"
                    :key="`${row.id}-${String(tag.id ?? tag.tagId)}-${tag.name}`"
                    type="button"
                    class="btn btn-link py-2 me-3 px-0 cursor-pointer opacity-hover text-decoration-none d-inline-flex align-items-center"
                    :title="t('journal.annual.tag.menu.tooltip')"
                    @click.stop="openTagContextMenu($event, tag, row.contentType)"
                  >
                    <span :class="[tag.tagClass, tag.textClass]" class="d-inline-flex align-items-center">
                      <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                      <span>{{ tag.name }}</span>
                    </span>
                    <span class="fs-9 text-noti fw-normal tag-count">{{ tag.contentSize ?? 0 }}</span>
                  </button>
                </span>
              </div>
            </div>
            <div v-if="row.hasSeparator" class="separator"></div>
          </template>
        </div>
      </div>
      <!--end::태그 헤더-->

      <!--begin::연간 스레드 요약 (월간/주간 기간 요약과 동일)-->
      <div v-if="annualThreadQuery" class="card-header">
        <div class="ms-4 w-100">
          <JournalPeriodThreadSummary :query="annualThreadQuery" />
        </div>
      </div>
      <!--end::연간 스레드 요약-->

      <hr class="my-6 text-muted" />

      <!--begin::토글 체크박스-->
      <div class="d-flex align-items-end justify-content-end mb-2">
        <div class="mx-3">
          <label for="toggleImprtc" class="me-2 text-muted cursor-help">{{ t("journal.annual.filter.important") }}</label>
          <input
            type="checkbox"
            id="toggleImprtc"
            class="form-check-input cursor-pointer"
            :checked="store.showImprtc"
            @change="onToggleImprtc"
          />
        </div>
        <div class="mx-3">
          <label for="toggleRefrnc" class="me-2 text-muted cursor-help">{{ t("journal.annual.filter.reference") }}</label>
          <input
            type="checkbox"
            id="toggleRefrnc"
            class="form-check-input cursor-pointer"
            :checked="store.showRefrnc"
            @change="onToggleRefrnc"
          />
        </div>
      </div>
      <!--end::토글 체크박스-->

      <!--begin::엔트리 목록-->
      <div class="card-post p-10">

        <!--begin::로딩-->
        <div v-if="store.entriesLoading" class="d-flex justify-content-center py-6">
          <span class="spinner-border text-primary" role="status"></span>
        </div>
        <!--end::로딩-->

        <template v-else>
          <!--begin::DIARY 목록-->
          <div v-if="store.activeSection === 'DIARY'" id="journal_annual_diary_list_div">
            <div v-if="!store.diaryEntries.length" class="text-center text-muted py-6 fs-7">
              {{ t('journal.annual.diary.empty') }}
            </div>
            <template v-for="group in diaryEntryGroups" :key="'diary-group-' + group.stdrdDt">
              <!--begin::날짜 헤더-->
              <div class="journal-day-header mt-4 mb-1" :data-date="group.stdrdDt">
                <div class="d-flex flex-wrap align-items-center fs-5 fw-bold">
                  <i class="bi bi-calendar3 fs-6 me-1"></i>
                  <span>{{ group.stdrdDt }}</span>
                  <span v-if="group.stdrdDt" class="fs-8 text-gray-600">
                    ({{ getWeekDayStr(group.stdrdDt, t) }})
                  </span>
                </div>
              </div>
              <!--end::날짜 헤더-->
              <!-- data-imprtc/data-refrnc 는 journal.scss 의 $journal-paired-states 셀렉터
                   (.journal-diary-item[data-imprtc="Y"] 등)와 연동해 중요=빨강·참조=노랑 좌측선을 낸다.
                   일자 엔트리(JournalEntryItem)와 동일 계약. -->
              <div
                v-for="entry in group.entries"
                :key="entry.id"
                class="journal-diary-item d-flex gap-2 py-1"
                :data-id="entry.id"
                :data-imprtc="hasEntryState(entry, 'IMPRTC') ? 'Y' : 'N'"
                :data-refrnc="hasEntryState(entry, 'REFRNC') ? 'Y' : 'N'"
                :data-stdrd-dt="entry.stdrdDt"
              >
                <!--begin::본문-->
                <div class="journal-diary-content flex-grow-1 p-2">
                  <!-- 제목 크기는 일자 엔트리(JournalEntryItem)와 동일 계약: 본문 1rem 대비 한 단계 위 fs-5 (변경 전 fs-7) -->
                  <div v-if="entry.title" class="fw-bold fs-5 mb-1">{{ entry.title }}</div>
                  <div
                    class="journal-content p-2 text-noti"
                    v-html="entry.markdownContent"
                  ></div>
                  <!--begin::엔트리 태그 — 저널 일자 JournalEntryItem 과 동일(밑줄 primary, bi-tag 없음, 클릭 메뉴)-->
                  <div v-if="hasEntryTags(entry)" class="d-flex flex-wrap gap-1 mt-1 ps-2">
                    <span
                      v-for="tag in entry.tag?.list"
                      :key="String(tag.tagId)"
                      class="text-muted cursor-pointer pe-1"
                      @click.stop="openTagContextMenu($event, tag, 'JOURNAL_DIARY')"
                    >
                      #<span class="border-bottom text-primary fw-lighter opacity-hover">
                        <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
                      </span>
                    </span>
                  </div>
                  <!--end::엔트리 태그-->
                </div>
                <!--end::본문-->
              </div>
            </template>
          </div>
          <!--end::DIARY 목록-->

          <!--begin::DREAM 목록-->
          <div v-else id="journal_annual_imprtc_dream_list_div">
            <div v-if="!store.dreamEntries.length" class="text-center text-muted py-6 fs-7">
              {{ t('journal.annual.dream.empty') }}
            </div>
            <template v-for="group in dreamEntryGroups" :key="'dream-group-' + group.stdrdDt">
              <!--begin::날짜 헤더-->
              <div class="journal-day-header mt-4 mb-1" :data-date="group.stdrdDt">
                <div class="d-flex flex-wrap align-items-center fs-5 fw-bold">
                  <i class="bi bi-calendar3 fs-6 me-1"></i>
                  <span>{{ group.stdrdDt }}</span>
                  <span v-if="group.stdrdDt" class="fs-8 text-gray-600">
                    ({{ getWeekDayStr(group.stdrdDt, t) }})
                  </span>
                </div>
              </div>
              <!--end::날짜 헤더-->
              <!-- 중요=빨강·참조=노랑 좌측선 (journal.scss $journal-paired-states 연동).
                   꿈은 data-else-dream="Y" 일 때 별도 팔레트를 쓰므로 타인 꿈 여부도 함께 내린다. -->
              <div
                v-for="entry in group.entries"
                :key="entry.id"
                class="journal-dream-item d-flex gap-2 py-1"
                :data-id="entry.id"
                :data-imprtc="hasEntryState(entry, 'IMPRTC') ? 'Y' : 'N'"
                :data-refrnc="hasEntryState(entry, 'REFRNC') ? 'Y' : 'N'"
                :data-else-dream="entry.elseDreamYn === 'Y' ? 'Y' : 'N'"
                :data-stdrd-dt="entry.stdrdDt"
              >
                <!--begin::본문-->
                <div class="journal-dream-content flex-grow-1 p-2">
                  <!-- 제목 크기는 일자 엔트리(JournalEntryItem)와 동일 계약: 본문 1rem 대비 한 단계 위 fs-5 (변경 전 fs-7) -->
                  <div v-if="entry.title" class="fw-bold fs-5 mb-1">{{ entry.title }}</div>
                  <div
                    class="journal-content p-2 text-noti"
                    v-html="entry.markdownContent"
                  ></div>
                  <!--begin::엔트리 태그 — 저널 일자 JournalEntryItem 과 동일(밑줄 primary, bi-tag 없음, 클릭 메뉴)-->
                  <div v-if="hasEntryTags(entry)" class="d-flex flex-wrap gap-1 mt-1 ps-2">
                    <span
                      v-for="tag in entry.tag?.list"
                      :key="String(tag.tagId)"
                      class="text-muted cursor-pointer pe-1"
                      @click.stop="openTagContextMenu($event, tag, 'JOURNAL_DREAM')"
                    >
                      #<span class="border-bottom text-primary fw-lighter opacity-hover">
                        <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
                      </span>
                    </span>
                  </div>
                  <!--end::엔트리 태그-->
                </div>
                <!--end::본문-->
              </div>
            </template>
          </div>
          <!--end::DREAM 목록-->
        </template>

      </div>
      <!--end::엔트리 목록-->

    </template>

    <!--begin::데이터 없음-->
    <div v-else-if="!store.detailLoading" class="text-center text-muted py-10 fs-6">
      {{ t('journal.annual.detail.load.failure') }}
    </div>
    <!--end::데이터 없음-->

  </div>
  <!--end::결산 상세-->
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import { useTagContextMenuStore } from "@/features/journal/stores/tagContextMenu";
import type { AnnualSection, AnnualEntryDto, AnnualTagItem, JournalAnnualReviewDto } from "@/features/journal/stores/journalAnnual";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import JournalPeriodThreadSummary from "@/features/journal/day/components/JournalPeriodThreadSummary.vue";

const route = useRoute();
const store = useJournalAnnualStore();
const { t } = useLocaleStore();
const tagContextMenuStore = useTagContextMenuStore();

const yy = computed(() => Number(route.params.yy));

/** 연간 스레드 기간 요약 조회 조건. yy가 유효할 때만 컴포넌트가 조회·렌더한다. */
const annualThreadQuery = computed(() =>
  Number.isFinite(yy.value) && yy.value > 0
    ? { viewType: "ANNUAL" as const, yy: yy.value }
    : null,
);

const hasSummaryTags = computed(() =>
  Array.isArray(store.annualDetail?.tag?.list) && store.annualDetail!.tag!.list!.length > 0
);

const reviewList = computed<JournalAnnualReviewDto[]>(() =>
  Array.isArray(store.annualDetail?.journalAnnualReviewList)
    ? store.annualDetail!.journalAnnualReviewList!
    : []
);

interface AnnualEntryGroup {
  stdrdDt: string;
  entries: AnnualEntryDto[];
}

function groupAnnualEntriesByDate(entries: AnnualEntryDto[]): AnnualEntryGroup[] {
  const groups: AnnualEntryGroup[] = [];
  const groupMap = new Map<string, AnnualEntryGroup>();

  entries.forEach((entry) => {
    const stdrdDt = entry.stdrdDt ?? "";
    let group = groupMap.get(stdrdDt);
    if (!group) {
      group = { stdrdDt, entries: [] };
      groupMap.set(stdrdDt, group);
      groups.push(group);
    }
    group.entries.push(entry);
  });

  return groups;
}

const diaryEntryGroups = computed(() => groupAnnualEntriesByDate(store.diaryEntries));
const dreamEntryGroups = computed(() => groupAnnualEntriesByDate(store.dreamEntries));

const tagCloudRows = computed(() =>
  store.activeSection === "DIARY"
    ? [
        {
          id: "journal_annual_day_tag_header",
          label: t("journal.annual.tag.day"),
          tags: store.tagRows.DAY,
          hasSeparator: true,
          contentType: "JOURNAL_DAY",
        },
        {
          id: "journal_annual_diary_tag_header",
          label: t("journal.annual.tag.diary"),
          tags: store.tagRows.DIARY,
          hasSeparator: false,
          contentType: "JOURNAL_DIARY",
        },
      ]
    : [
        {
          id: "journal_annual_dream_tag_header",
          label: t("journal.annual.tag.dream"),
          tags: store.tagRows.DREAM,
          hasSeparator: false,
          contentType: "JOURNAL_DREAM",
        },
      ]
);

async function loadAnnualDetail(targetYy: number) {
  if (!Number.isFinite(targetYy) || targetYy <= 0) return;
  store.filterYy = targetYy;
  await store.fetchDetail(targetYy);
  store.activeSection = "DIARY";
  void store.fetchEntries(targetYy, "DIARY");
  void store.fetchTagRows(targetYy, "DIARY");
}

onMounted(async () => {
  await loadAnnualDetail(yy.value);
  return;
  await store.fetchDetail(yy.value);
  /* 초기 탭: DIARY — 엔트리 + 태그 행 로드 */
  store.activeSection = "DIARY";
  void store.fetchEntries(yy.value, "DIARY");
  void store.fetchTagRows(yy.value, "DIARY");
});

/** 탭 클릭 시 섹션 전환 + 엔트리/태그 재조회 */
watch(yy, (nextYy, prevYy) => {
  if (nextYy === prevYy) return;
  void loadAnnualDetail(nextYy);
});

async function onTabClick(section: AnnualSection) {
  if (store.activeSection === section) return;
  await store.setSection(section, yy.value);
}

/** IMPORTANT 토글 */
async function onToggleImprtc() {
  await store.toggleImprtc(yy.value);
}

/** REFERENCE 토글 */
async function onToggleRefrnc() {
  await store.toggleRefrnc(yy.value);
}

/** 리뷰 등록 모달 열기 */
function openReviewRegist() {
  const annualId = store.annualDetail?.id;
  if (!annualId) return;
  store.openReviewRegist(annualId, yy.value);
}

/** 리뷰 수정 모달 열기 */
function openReviewModify(id: number) {
  void store.openReviewModify(id);
}

/** 리뷰 삭제 */
function deleteReview(id: number) {
  void store.deleteReview(id, yy.value);
}

function openTagContextMenu(event: MouseEvent, tag: AnnualTagItem, contentType: string) {
  tagContextMenuStore.open(event, {
    tagId: tag.id ?? tag.tagId,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType,
  });
}

/** 리뷰 태그 보유 여부 */
function hasReviewTags(rev: JournalAnnualReviewDto): boolean {
  return Array.isArray(rev.tag?.list) && rev.tag!.list!.length > 0;
}

/** 엔트리 태그 보유 여부 */
function hasEntryTags(entry: AnnualEntryDto): boolean {
  return Array.isArray(entry.tag?.list) && entry.tag!.list!.length > 0;
}

/**
 * 엔트리 상태 보유 여부 (IMPRTC=중요, REFRNC=참조).
 * 일자 엔트리(JournalEntryItem.hasState)와 같은 판정으로, journal.scss 의
 * data-imprtc/data-refrnc 셀렉터에 넘길 값을 만든다.
 */
function hasEntryState(entry: AnnualEntryDto, stateKey: string): boolean {
  return (entry.state?.list ?? []).some((s) => s?.stateKey === stateKey);
}
</script>
