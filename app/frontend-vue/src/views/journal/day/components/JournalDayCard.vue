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
            @click="openDetail"
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
        <!--begin::챕터 등록 버튼-->
        <button
          v-if="showDiaries"
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
          @click="openChapterRegist"
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
          @click="openDreamRegist"
        >
          <i class="bi bi-moon-stars fs-4 pe-1"></i>
          저널 꿈 등록
        </button>
        <!--end::꿈 등록 버튼-->
        <!--begin::컨텍스트 메뉴 (⋯)-->
        <div v-if="day.id" class="me-0 d-flex align-items-center">
          <button
            type="button"
            class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
            title="메뉴"
          >
            <i class="ki-solid ki-dots-horizontal fs-2x"></i>
          </button>
          <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
            <div class="menu-item px-3">
              <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">저널 일자</div>
            </div>
            <!--begin::주간 뷰로 이동-->
            <div class="menu-item px-3 my-1">
              <div class="menu-link flex-stack px-3" @click="gotoWeekly">
                주간 뷰로 이동
                <i class="bi bi-calendar-week fs-8"></i>
              </div>
            </div>
            <!--end::주간 뷰로 이동-->
            <!--begin::새 창으로 열기 (일자 뷰)-->
            <div class="menu-item px-3 my-1">
              <div class="menu-link flex-stack px-3" @click="openDayView">
                새 창으로 열기 (일자 뷰)
                <i class="bi bi-box-arrow-up-right fs-8"></i>
              </div>
            </div>
            <!--end::새 창으로 열기 (일자 뷰)-->
            <div class="separator my-2"></div>
            <!--begin::수정-->
            <div class="menu-item px-3 my-1">
              <div class="menu-link flex-stack px-3" @click="openRegist">
                수정
                <i class="bi bi-pencil-square fs-8"></i>
              </div>
            </div>
            <!--end::수정-->
            <!--begin::상태 서브메뉴-->
            <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
              <a href="#" class="menu-link px-3" @click.prevent>
                <span class="menu-title">상태</span>
                <span class="menu-arrow"></span>
              </a>
              <div class="menu-sub menu-sub-dropdown w-175px py-4">
                <div class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input class="form-check-input w-30px h-20px cursor-pointer" type="checkbox" :checked="hasState('IMPRTC')" />
                      <span class="form-check-label text-muted fs-7">중요</span>
                    </label>
                  </div>
                </div>
                <div class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input class="form-check-input w-30px h-20px cursor-pointer" type="checkbox" :checked="hasState('COLLAPSED')" @click.prevent="toggleCollapsed" />
                      <span class="form-check-label text-muted fs-7">접힘</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            <!--end::상태 서브메뉴-->
            <div class="separator my-2"></div>
            <!--begin::삭제-->
            <div class="menu-item px-3 my-1">
              <div class="menu-link flex-stack px-3 text-danger" @click="deleteDay">
                삭제
                <i class="bi bi-trash text-danger p-0 fs-8"></i>
              </div>
            </div>
            <!--end::삭제-->
          </div>
        </div>
        <!--end::컨텍스트 메뉴-->
        <!--begin::메타 메뉴 버튼-->
        <div v-if="hasMeta" class="me-0 d-flex align-items-center">
          <button
            class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
            title="메타"
          >
            <i class="bi bi-bar-chart"></i>
          </button>
          <div
            class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold min-w-250px py-3"
            data-kt-menu="true"
          >
            <div class="menu-item px-3">
              <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">메타</div>
            </div>
            <div
              v-for="meta in metaList"
              :key="'meta-menu-' + meta.metaId"
              class="menu-item px-3"
            >
              <button
                type="button"
                class="menu-link w-100 border-0 bg-transparent px-3 text-start d-flex align-items-center"
                @click="openMetaModal(meta.metaId, meta.name)"
              >
                <span v-if="meta.ctgr" class="text-noti pe-1">[{{ meta.ctgr }}]</span>
                <span>{{ meta.name }}</span>
                <span class="text-dialog ms-1">: {{ meta.value }}{{ meta.unit }}</span>
              </button>
            </div>
          </div>
        </div>
        <!--end::메타 메뉴 버튼-->
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
            @click.stop="openTagContextMenu($event, tag)"
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
      <template v-if="showDreams && hasDream">
        <!--begin::꿈 섹션 헤더-->
        <div class="d-flex align-items-center mt-2">
          <div class="d-flex-align-center text-gray-700 fs-6 ps-1 ps-md-5 me-5 fw-bolder">
            <span class="me-2">꿈</span>
            <i class="bi bi-moon-stars fs-4"></i>
          </div>
          <div class="col-3 d-none d-md-flex align-items-center gap-2">
            <!--begin::꿈 등록 버튼-->
            <button
              type="button"
              class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
              title="저널 꿈 등록"
              @click="openDreamRegist"
            >
              <i class="bi bi-moon-stars fs-4 pe-1"></i>
              저널 꿈 등록
            </button>
            <!--end::꿈 등록 버튼-->
            <!--begin::복사 버튼-->
            <button
              type="button"
              class="btn btn-sm btn-light-primary btn-outlined ms-2 px-3 cursor-pointer"
              title="복사"
              @click="copyDreams"
            >
              <i class="bi bi-copy p-0"></i>
            </button>
            <!--end::복사 버튼-->
            <!--begin::TXT보내기 버튼-->
            <button
              type="button"
              class="btn btn-sm btn-outline btn-light-primary ps-3 pe-2"
              title="TXT보내기"
              @click="exportDreams"
            >
              <i class="fas fa-download"></i>
            </button>
            <!--end::TXT보내기 버튼-->
            <!--begin::접힘 토글 버튼 (클라이언트 DOM만, 서버 상태 무변경)-->
            <button
              type="button"
              class="btn btn-sm btn-secondary ms-2 px-3 toggle-chapter-btn"
              @click="toggleDreams"
            >
              <i class="bi pe-0" :class="dreamsCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse'"></i>
            </button>
            <!--end::접힘 토글 버튼-->
          </div>
        </div>
        <!--end::꿈 섹션 헤더-->
        <!--begin::꿈 엔트리 목록-->
        <div class="journal-chapter-item">
          <div :class="['journal-chapter-content', { collapsed: dreamsCollapsed }]">
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
          </div>
        </div>
        <!--end::꿈 엔트리 목록-->
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
import { swalConfirm, swalAlert } from "@/utils/swal";
import { isAuthExpiredError } from "@/utils/authError";
import { ref, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import type { JournalDayDto } from "@/stores/journal";
import { getWeekDayStr } from "@/utils/journalDate";
import { useJournalModalStore } from "@/stores/journalModal";
import { useJournalStore } from "@/stores/journal";
import { useTagContextMenuStore } from "@/stores/tagContextMenu";
import JournalChapterItem from "../../chapter/components/JournalChapterItem.vue";
import JournalEntryItem from "../../entry/components/JournalEntryItem.vue";

const props = defineProps<{
  day: JournalDayDto;
  showDiaries?: boolean;
  showDreams?: boolean;
}>();

const router = useRouter();
const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const tagContextMenuStore = useTagContextMenuStore();

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

/** 일자 상태 보유 여부 확인 */
function hasState(stateKey: string): boolean {
  return (props.day.state?.list ?? []).some((s: { stateKey?: string }) => s.stateKey === stateKey);
}

/** 주간 뷰로 이동 */
function gotoWeekly(): void {
  void router.push({ name: "journal-weekly", query: { stdrdDt: props.day.stdrdDt } });
}

/** 일자 뷰(daily) 새 창으로 열기 — features 지정으로 탭이 아닌 새 창 강제 */
function openDayView(): void {
  const base = import.meta.env.BASE_URL.replace(/\/$/, "");
  const w = Math.min(1200, window.screen.availWidth);
  const h = Math.min(900, window.screen.availHeight);
  window.open(`${base}/journal/daily?stdrdDt=${props.day.stdrdDt}`, "_blank", `width=${w},height=${h},left=100,top=60`);
}

/** fetchDays 완료 후 해당 일자로 스크롤 */
function scrollAfterFetch(): void {
  const dt = props.day.stdrdDt;
  if (!dt) return;
  void journalStore.fetchDays().then(() => {
    void nextTick(() => {
      const el = document.getElementById(`journal-day-${dt}`);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  });
}

/** 접힘 상태 토글 (서버 반영 후 목록 갱신) */
async function toggleCollapsed(): Promise<void> {
  if (!props.day.id) return;
  try {
    await axios.post("/api/states", {
      id: props.day.id,
      contentType: "JOURNAL_DAY",
      stateKey: "COLLAPSED",
    });
    scrollAfterFetch();
  } catch {
    console.error("[JournalDayCard] toggleCollapsed 실패");
  }
}

/** 일자 삭제 */
async function deleteDay(): Promise<void> {
  if (!props.day.id) return;
  if (!await swalConfirm("삭제하시겠습니까?")) return;
  try {
    const res = await axios.delete(`/api/journal/day/${props.day.id}`);
    if (res.data?.rslt) {
      scrollAfterFetch();
    } else {
      void swalAlert(res.data?.message ?? "삭제에 실패했습니다.");
    }
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return;
    void swalAlert("요청 처리 중 오류가 발생했습니다.");
  }
}

/** 상세 모달 열기 */
function openDetail() {
  if (props.day.id) void modalStore.openDayDetail(props.day.id);
}

/** 수정 모달 열기 */
function openRegist() {
  modalStore.openDayRegist({
    id: props.day.id,
    journalDate: props.day.journalDate ?? props.day.stdrdDt,
    journalDatePrecision: props.day.journalDatePrecision,
    weather: props.day.weather,
  });
}


/** 챕터 등록 모달 열기 */
function openChapterRegist() {
  if (!props.day.id) return;
  modalStore.openChapterRegist({
    journalDayId: props.day.id,
    stdrdDt: props.day.stdrdDt,
    journalDateWeekDay: props.day.journalDateWeekDay,
  });
}

/** 꿈 섹션 접힘 여부 (클라이언트 DOM만, 서버 상태 무변경) */
const dreamsCollapsed = ref(false);

/** 꿈 섹션 접힘 토글 */
function toggleDreams(): void {
  dreamsCollapsed.value = !dreamsCollapsed.value;
}

/** HTML을 일반 텍스트로 변환한다 (클립보드 복사용). */
function htmlToPlainText(html: string): string {
  return html
    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n------\n")
    .replace(/<\s*br\s*\/?>/gi, "\n")
    .replace(/<\s*\/?p[^>]*>/gi, "\n")
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .split("\n").map((l) => l.trim()).join("\n")
    .replace(/\n+/g, "\n")
    .trim();
}

/** 꿈 엔트리 전체를 클립보드에 복사한다. 레거시 형식: 날짜(요일)\n#순번\n본문 */
async function copyDreams(): Promise<void> {
  const lines: string[] = [];
  const weekDay = props.day.journalDateWeekDay ?? getWeekDayStr(props.day.stdrdDt);
  const dateLine = weekDay
    ? `${props.day.stdrdDt} (${weekDay})`
    : (props.day.stdrdDt ?? "");
  if (dateLine) lines.push(dateLine);
  const allDreams = [...journalDreamList.value, ...journalElseDreamList.value];
  for (const entry of allDreams) {
    const sortNum = entry.sortOrder != null ? "#" + String(entry.sortOrder) : "";
    /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
    const raw = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
    if (sortNum) lines.push(sortNum);
    if (raw) lines.push(raw);
    lines.push("");
  }
  const text = lines.join("\n").trim();
  try {
    await navigator.clipboard.writeText(text);
    void swalAlert("클립보드에 복사되었습니다.");
  } catch {
    void swalAlert("복사에 실패했습니다.");
  }
}

/** 꿈 엔트리를 TXT로 내보낸다. */
function exportDreams(): void {
  if (!props.day.id) return;
  window.location.href = `/api/journal/entries/export?journalDayId=${props.day.id}&type=DREAM`;
}

/** 꿈 엔트리 등록 모달 열기 */
function openDreamRegist() {
  if (!props.day.id) return;
  void modalStore.openDreamEntryRegist({
    journalDayId: props.day.id,
    stdrdDt: props.day.stdrdDt ?? "",
    journalDateWeekDay: props.day.journalDateWeekDay,
  });
}

/** 일자 태그 컨텍스트 메뉴 열기 */
function openTagContextMenu(event: MouseEvent, tag: { tagId: number | string; name: string; ctgr?: string }) {
  tagContextMenuStore.open(event, {
    tagId: tag.tagId,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType: "JOURNAL_DAY",
  });
}

/** 메타 컨텐츠 목록 모달 열기 */
function openMetaModal(metaId?: number | string, metaName?: string): void {
  if (!metaId) return;
  void modalStore.openMetaModal(metaId, undefined, metaName);
}
</script>
