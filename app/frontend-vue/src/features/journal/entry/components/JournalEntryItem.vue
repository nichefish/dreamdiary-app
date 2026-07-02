<template>
  <!--begin::엔트리 행-->
  <div
    :id="domId ?? (entry.id ? 'journal-entry-' + entry.id : undefined)"
    :class="[itemClass, { 'is-collapsed': isCollapsed }, 'd-flex gap-2 py-1']"
    :data-id="entry.id"
    :data-imprtc="hasState('IMPRTC') ? 'Y' : 'N'"
    :data-refrnc="hasState('REFRNC') ? 'Y' : 'N'"
    :data-resolved="isResolved ? 'Y' : 'N'"
    :data-else-dream="isElseDream ? 'Y' : 'N'"
    :data-stdrd-dt="entry.stdrdDt"
    :data-yy="entryCacheYy"
    :data-mnth="entryCacheMnth"
  >
    <!--begin::순번-->
    <div class="d-none d-md-flex flex-column align-items-center pt-1 ps-2" style="width:56px; min-width:56px;">
      <span :class="['fw-bold fs-7', isResolved ? 'text-success' : 'text-muted']">#{{ entry.sortOrder }}</span>
      <span v-if="lcKey === 'PENDING'" class="badge badge-light-warning fs-8 mt-1">PEND</span>
      <!--begin::클라이언트 임시 접힘/펼침 버튼-->
      <button
        type="button"
        :class="['btn btn-xs px-1 mt-1', { 'is-active': isCollapsed }]"
        :title="isCollapsed ? '펼치기' : '접기'"
        @click="toggleEntry"
      >
        <i :class="['bi pe-0 fs-8', isCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse']"></i>
      </button>
      <!--end::클라이언트 임시 접힘/펼침 버튼-->
    </div>
    <!--end::순번-->

    <!--begin::본문 영역-->
    <div :class="[contentClass, 'flex-grow-1']">
      <!--begin::꿈 상태 배지 (꿈 엔트리 전용)-->
      <div v-if="isDream" class="d-flex align-items-center gap-1 mb-1 flex-wrap">
        <span v-if="hasState('NHTMR')" class="badge badge-light-danger">!악몽</span>
        <span v-if="hasState('HALLUC')" class="badge badge-light-secondary">!환각/현시</span>
        <span v-if="entry.title" class="fw-bold fs-7">{{ entry.title }}</span>
      </div>
      <!--end::꿈 상태 배지-->

      <!--begin::마크다운 본문-->
      <div
        v-if="!isCollapsed && entry.markdownContent"
        class="journal-content p-2"
        v-html="entry.markdownContent"
      ></div>
      <div v-else-if="isCollapsed" class="text-muted fs-8 fst-italic ps-2">collapsed</div>
      <!--end::마크다운 본문-->

      <!--begin::엔트리 태그-->
      <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 mt-1 ps-2">
        <span
          v-for="tag in tagList"
          :key="tag.tagId"
          class="text-muted cursor-pointer pe-1"
          @click.stop="openTagContextMenu($event, tag)"
        >
          #<span class="border-bottom text-primary fw-lighter opacity-hover">
            <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
          </span>
        </span>
      </div>
      <!--end::엔트리 태그-->

      <!--begin::꿈 태그 프로필-->
      <div v-if="!isCollapsed && dreamTagProfileList.length > 0" class="journal-dream-tag-profiles">
        <div
          v-for="tag in dreamTagProfileList"
          :key="'profile-' + tag.tagId"
          class="journal-dream-tag-profile"
        >
          <span class="journal-dream-tag-profile__tag">#{{ tag.name }}</span>
          <span class="journal-dream-tag-profile__divider"></span>
          <span class="journal-dream-tag-profile__content">{{ tag.profileContent }}</span>
        </div>
      </div>
      <!--end::꿈 태그 프로필-->

      <!--begin::관련글-->
      <div v-if="relatedList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="rel in relatedList"
          :key="rel.id"
          class="d-flex align-items-center gap-2 p-2 bg-light rounded fs-8 text-muted"
        >
          <i class="bi bi-link-45deg"></i>
          <span v-if="rel.contentType" class="badge badge-light-secondary">{{ rel.contentType }}</span>
          <span v-if="rel.refTitle">{{ rel.refTitle }}</span>
          <span v-if="rel.relReason" class="fst-italic">({{ rel.relReason }})</span>
        </div>
      </div>
      <!--end::관련글-->

      <!--begin::댓글-->
      <div v-if="commentList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="cmt in commentList"
          :key="cmt.id"
          class="fs-8 text-muted ps-2 border-start border-2 border-gray-300"
        >
          {{ cmt.content }}
        </div>
      </div>
      <!--end::댓글-->
    </div>
    <!--end::본문 영역-->

    <!--begin::우측 액션 영역-->
    <div v-if="entry.id" class="journal-entry-actions d-flex flex-row align-items-start pt-1 gap-1">
      <!--begin::댓글 등록 버튼-->
      <button
        type="button"
        class="btn btn-xs btn-icon journal-entry-action-btn"
        title="댓글 등록"
        @click="openCommentRegist"
      >
        <i class="bi bi-chat-dots fs-8"></i>
      </button>
      <!--end::댓글 등록 버튼-->

      <!--begin::복사 버튼-->
      <button
        type="button"
        class="btn btn-xs btn-icon journal-entry-action-btn"
        title="복사"
        @click="copyEntry"
      >
        <i class="bi bi-copy fs-8"></i>
      </button>
      <!--end::복사 버튼-->

      <!--begin::컨텍스트 메뉴-->
      <div class="me-0">
        <button
          type="button"
          class="btn btn-xs btn-icon journal-entry-action-btn"
          data-kt-menu-trigger="click"
          data-kt-menu-placement="bottom-end"
          title="메뉴"
        >
          <i class="ki-solid ki-dots-horizontal fs-6"></i>
        </button>
        <div
          class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
          data-kt-menu="true"
        >
          <!--begin::메뉴 헤더-->
          <div class="menu-item px-3">
            <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ contentLabel }}</div>
          </div>
          <!--end::메뉴 헤더-->

          <!--begin::수정-->
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openModify">
              수정
              <i class="bi bi-pencil-square fs-8"></i>
            </div>
          </div>
          <!--end::수정-->

          <!--begin::해석 등록-->
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openInterpretationRegist">
              해석 등록
              <i class="bi bi-lightbulb fs-8"></i>
            </div>
          </div>
          <!--end::해석 등록-->

          <!--begin::이력 (historyTriggeredAt 없으면 disabled)-->
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div
              :class="['menu-link flex-stack px-3', { 'disabled text-muted': !hasHistory }]"
              @click="hasHistory ? openHistory() : undefined"
            >
              이력
              <i class="bi bi-clock-history fs-8"></i>
            </div>
          </div>
          <!--end::이력-->

          <!--begin::관련 글 추가 (다른 사람 꿈 제외)-->
          <div v-if="!hasDreamerName(entry)" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openRelated">
              관련 글 추가
              <i class="bi bi-link-45deg fs-8"></i>
            </div>
          </div>
          <!--end::관련 글 추가-->

          <div class="separator my-2"></div>

          <!--begin::라이프사이클 서브메뉴-->
          <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">라이프사이클</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <div v-for="lc in lifecycleOptions" :key="'lc-' + lc.key" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-18px h-18px cursor-pointer"
                      type="radio"
                      :name="'entry-lc-' + entry.id"
                      :value="lc.key"
                      :checked="lcKey === lc.key"
                      @click="setLifecycle(lc.key)"
                    />
                    <span class="form-check-label fs-7" :class="lcKey === lc.key ? lc.activeClass : 'text-muted'">{{ lc.label }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          <!--end::라이프사이클 서브메뉴-->

          <!--begin::상태 서브메뉴-->
          <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">상태</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <!--begin::중요/참조 토글-->
              <div v-for="st in statusOptions" :key="'st-' + st.key" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-30px h-20px cursor-pointer"
                      type="checkbox"
                      :checked="hasState(st.key)"
                      @click="toggleState(st.key)"
                    />
                    <span class="form-check-label fs-7" :class="hasState(st.key) ? st.activeClass : 'text-muted'">{{ st.label }}</span>
                  </label>
                </div>
              </div>
              <!--end::중요/참조 토글-->

              <!--begin::악몽/환각 토글 (꿈 전용)-->
              <template v-if="isDream">
                <div v-for="st in dreamStatusOptions" :key="'dst-' + st.key" class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input
                        class="form-check-input w-30px h-20px cursor-pointer"
                        type="checkbox"
                        :checked="hasState(st.key)"
                        @click="toggleState(st.key)"
                      />
                      <span class="form-check-label fs-7" :class="hasState(st.key) ? st.activeClass : 'text-muted'">{{ st.label }}</span>
                    </label>
                  </div>
                </div>
              </template>
              <!--end::악몽/환각 토글-->

              <!--begin::접기 토글-->
              <div class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-30px h-20px cursor-pointer"
                      type="checkbox"
                      :checked="hasState('COLLAPSED')"
                      @click="toggleState('COLLAPSED')"
                    />
                    <span class="form-check-label fs-7" :class="hasState('COLLAPSED') ? 'text-gray-700' : 'text-muted'">접기</span>
                  </label>
                </div>
              </div>
              <!--end::접기 토글-->
            </div>
          </div>
          <!--end::상태 서브메뉴-->

          <div class="separator my-2"></div>

          <!--begin::삭제-->
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3 text-danger" @click="deleteEntry">
              삭제
              <i class="bi bi-trash text-danger p-0 fs-8"></i>
            </div>
          </div>
          <!--end::삭제-->
        </div>
      </div>
      <!--end::컨텍스트 메뉴-->
    </div>
    <!--end::우측 액션 영역-->
  </div>
  <!--end::엔트리 행-->

  <!--begin::해석 목록-->
  <JournalInterpretationItem
    v-for="interp in interpretationList"
    :key="interp.id"
    :interpretation="interp"
  />
  <!--end::해석 목록-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError } from "@/shared/utils/swal";
import { ref, computed, nextTick } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useTagContextMenuStore } from "@/features/journal/stores/tagContextMenu";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr, getWeekStartDateStr } from "@/features/journal/utils/journalDate";
import { hasDreamerName } from "@/features/journal/utils/journalDream";
import JournalInterpretationItem from "../../interpretation/components/JournalInterpretationItem.vue";

const props = defineProps<{
  entry: JournalEntryDto;
  isDream?: boolean;
  /** 챕터 토글이 전파하는 강제 접힘 여부. null=챕터 미개입, true/false=챕터 강제 */
  forceCollapsed?: boolean | null;
  /** Parent-provided DOM id, used by popup/search contexts that render the same entry component. */
  domId?: string;
}>();

const modalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();
const tagContextMenuStore = useTagContextMenuStore();
const journalStore = useJournalStore();
const route = useRoute();

interface JournalCacheContext {
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
}

/** 엔트리 타입별 외부 item 클래스 (journal.scss 의 data-* 셀렉터 연동) */
const itemClass = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return 'journal-dream-item';
  if (props.entry.contentType === 'JOURNAL_NOTE') return 'journal-note-item';
  return 'journal-diary-item';
});

/** 엔트리 타입별 내부 content 클래스 (텍스트 색상·left-border 스타일 연동) */
const contentClass = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return 'journal-dream-content';
  if (props.entry.contentType === 'JOURNAL_NOTE') return 'journal-note-content';
  return 'journal-diary-content';
});

/** 메뉴 헤더에 표시할 컨텐츠 유형 레이블 */
const contentLabel = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return '꿈';
  return '일기';
});

const lcKey = computed(() => props.entry.lifecycle?.lifecycleKey ?? "");
const isResolved = computed(() => lcKey.value === "RESOLVED");
/** 지정 꿈꾼(타인 꿈) — journal.scss 좌측 회색 이중선·RESOLVED 색상과 별도 */
const isElseDream = computed(() => {
  if (!(props.isDream || props.entry.contentType === "JOURNAL_DREAM")) return false;
  return hasDreamerName(props.entry);
});
const hasHistory = computed(() => !!props.entry.history?.historyTriggeredAt);

/** 클라이언트 임시 접힘 오버라이드. null=서버 상태 따름, true=강제 접힘, false=강제 펼침 */
const localCollapsedOverride = ref<boolean | null>(null);

/** 서버 상태(COLLAPSED) + 클라이언트 임시 오버라이드를 합산한 최종 접힘 여부. RESOLVED 시 자동 접힘.
 * 우선순위: 엔트리 자체 토글 > 챕터 강제(forceCollapsed) > RESOLVED 자동 접힘 > 서버 COLLAPSED */
const isCollapsed = computed(() => {
  if (localCollapsedOverride.value !== null) return localCollapsedOverride.value;
  if (props.forceCollapsed !== null && props.forceCollapsed !== undefined) return props.forceCollapsed;
  if (isResolved.value) return true;
  return hasState("COLLAPSED");
});

function hasState(key: string): boolean {
  return (props.entry.state?.list ?? []).some((s) => s.stateKey === key);
}

const tagList = computed(() => props.entry.tag?.list ?? []);
/** 변경 전: 태그 프로필은 설정 모달에서만 보였음. 변경 후: 꿈 엔트리에서만 본문 아래에 프로필을 표시. */
const dreamTagProfileList = computed(() => {
  if (!(props.isDream || props.entry.contentType === "JOURNAL_DREAM")) return [];
  return tagList.value.filter((tag) => typeof tag.profileContent === "string" && tag.profileContent.trim() !== "");
});
const relatedList = computed(() => props.entry.relatedContentList ?? []);
const commentList = computed(() => props.entry.comment?.list ?? []);
const interpretationList = computed(() => props.entry.journalInterpretationList ?? []);

function parseCacheNumber(value: unknown): number | undefined {
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
}

function resolveEntryYy(): number | undefined {
  const stdrdDt = props.entry.stdrdDt?.trim();
  if (stdrdDt && stdrdDt.length >= 4) return parseCacheNumber(stdrdDt.slice(0, 4));
  return parseCacheNumber(journalStore.yy);
}

function resolveEntryMnth(): number | undefined {
  const stdrdDt = props.entry.stdrdDt?.trim();
  if (stdrdDt && stdrdDt.length >= 7) return parseCacheNumber(stdrdDt.slice(5, 7));
  return parseCacheNumber(journalStore.mnth);
}

function resolveEntryWeekStartDt(): string | undefined {
  const weekStartDt = journalStore.weekStartDt?.trim();
  if (weekStartDt) return weekStartDt;
  const stdrdDt = props.entry.stdrdDt?.trim();
  return stdrdDt ? getWeekStartDateStr(stdrdDt) : undefined;
}

const entryCacheYy = computed(() => resolveEntryYy());
const entryCacheMnth = computed(() => resolveEntryMnth());

function resolveJournalCacheContext(): JournalCacheContext {
  const cacheContext: JournalCacheContext = {};
  const yy = entryCacheYy.value;
  const mnth = entryCacheMnth.value;
  const weekStartDt = resolveEntryWeekStartDt();
  if (yy != null) cacheContext.yy = yy;
  if (mnth != null) cacheContext.mnth = mnth;
  if (weekStartDt) cacheContext.weekStartDt = weekStartDt;
  if (Object.keys(cacheContext).length === 0) {
    console.warn("[journal] missing cache context for entry state update", {
      id: props.entry.id,
      contentType: props.entry.contentType,
      stdrdDt: props.entry.stdrdDt,
    });
  }
  return cacheContext;
}

/** 라이프사이클 옵션 (OPEN/PENDING/RESOLVED) */
const lifecycleOptions = [
  { key: "OPEN", label: "진행 중", activeClass: "text-gray-800" },
  { key: "PENDING", label: "보류", activeClass: "text-primary" },
  { key: "RESOLVED", label: "완료", activeClass: "text-success" },
];

/** 상태 옵션 (중요/참조) */
const statusOptions = [
  { key: "IMPRTC", label: "중요", activeClass: "text-danger" },
  { key: "REFRNC", label: "참조", activeClass: "text-warning" },
];

/** 꿈 전용 상태 옵션 (악몽/환각) */
const dreamStatusOptions = [
  { key: "NHTMR", label: "악몽", activeClass: "text-info" },
  { key: "HALLUC", label: "환각/현시", activeClass: "text-gray-700" },
];

/** 태그 클릭 컨텍스트 메뉴 열기 */
function openTagContextMenu(event: MouseEvent, tag: { tagId: number | string; name: string; ctgr?: string }): void {
  tagContextMenuStore.open(event, {
    tagId: tag.tagId,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType: props.entry.contentType ?? "",
  });
}

/** 클라이언트 전용 임시 접힘/펼침 토글 (서버 상태 무변경) */
function toggleEntry(): void {
  localCollapsedOverride.value = !isCollapsed.value;
}
/** HTML 마크업을 제거하고 평문으로 변환한다 (복사 시 사용). */
function htmlToPlainText(html: string): string {
  return html
    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n------\n")
    .replace(/<\s*br\s*\/?>/gi, "\n")
    .replace(/<\s*\/?p[^>]*>/gi, "\n")   /* <p> 와 </p> 모두 줄바꿈으로 — 레거시 동일 */
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .split("\n").map((l) => l.trim()).join("\n")   /* 각 줄 앞뒤 공백 제거 */
    .replace(/\n+/g, "\n")                         /* 연속 빈줄 → 단일 줄바꿈 */
    .trim();
}

/** 엔트리 내용을 클립보드에 복사한다. 레거시 copy() 와 동일 형식: 날짜(요일)\n마크다운 원문 */
async function copyEntry(): Promise<void> {
  const weekDay = getWeekDayStr(props.entry.stdrdDt);
  const dateLine = weekDay
    ? `${props.entry.stdrdDt} (${weekDay})`
    : (props.entry.stdrdDt ?? "");
  /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
  const raw = htmlToPlainText(props.entry.content ?? props.entry.markdownContent ?? "");
  const text = [dateLine, raw].filter(Boolean).join("\n");
  try {
    await navigator.clipboard.writeText(text);
    void swalAlert("클립보드에 복사되었습니다.");
  } catch {
    void swalAlert("복사에 실패했습니다.");
  }
}

/** 엔트리 수정 모달 열기 */
function openModify() {
  if (!props.entry.id) return;
  void modalStore.openEntryModify(props.entry.id);
}

/** 댓글 등록 모달 열기 */
function openCommentRegist() {
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openCommentRegist(props.entry.id, props.entry.contentType);
}

/** 이력 모달 열기 */
function openHistory() {
  if (!props.entry.id || !props.entry.contentType) return;
  void attachableStore.openHistory(props.entry.contentType, props.entry.id);
}

/** 관련 글 추가 모달 열기 */
function openRelated() {
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openRelated(props.entry.contentType, props.entry.id);
}

/** 해석 등록 모달 열기 */
function openInterpretationRegist() {
  if (!props.entry.id || !props.entry.contentType) return;
  modalStore.openInterpretationRegist({
    refId: props.entry.id,
    refContentType: props.entry.contentType,
    stdrdDt: props.entry.stdrdDt,
  });
}

/** fetchDays 완료 후 해당 일자로 스크롤 */
function scrollAfterFetch(stdrdDt = props.entry.stdrdDt): void {
  const dt = stdrdDt;
  if (!dt) return;
  const afterFetch = () => {
    void nextTick(() => {
      const el = document.getElementById(`journal-day-${dt}`);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  };

  void refreshJournalDaysForRoute(journalStore, route, dt).then(afterFetch);
}

/** 라이프사이클 설정 (PUT /api/lifecycles) */
async function setLifecycle(lifecycleKey: string): Promise<void> {
  if (!props.entry.id || !props.entry.contentType) return;
  try {
    const res = await axios.put("/api/lifecycles", {
      id: props.entry.id,
      contentType: props.entry.contentType,
      lifecycleKey,
      cacheContext: resolveJournalCacheContext(),
    });
    if (res.data?.rslt) {
      scrollAfterFetch();
    } else {
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

/** 상태 토글 (POST /api/states) */
async function toggleState(stateKey: string): Promise<void> {
  if (!props.entry.id || !props.entry.contentType) return;
  try {
    const res = await axios.post("/api/states", {
      id: props.entry.id,
      contentType: props.entry.contentType,
      stateKey,
      cacheContext: resolveJournalCacheContext(),
    });
    if (res.data?.rslt) {
      scrollAfterFetch();
    } else {
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

/** 엔트리 삭제 (DELETE /api/journal/entry/{id}) */
async function deleteEntry(): Promise<void> {
  if (!props.entry.id) return;
  const stdrdDt = props.entry.stdrdDt;
  const confirmed = await swalConfirm("삭제하시겠습니까?");
  if (!confirmed) return;
  try {
    const res = await axios.delete(`/api/journal/entry/${props.entry.id}`);
    if (res.data?.rslt) {
      await swalAlert(res.data?.message ?? "삭제되었습니다.");
      scrollAfterFetch(stdrdDt);
    } else {
      void swalAlert(res.data?.message ?? "삭제에 실패했습니다.");
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}
</script>
