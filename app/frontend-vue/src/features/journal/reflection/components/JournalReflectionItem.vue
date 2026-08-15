<template>
  <!--begin::Reflection 슬림 임베드 (target 엔트리 본문 아래·태그 위)-->
  <div
    class="journal-reflection-embed"
    :class="{ 'align-items-stretch': isCollapsed }"
    :data-id="reflection.id"
    :data-resolved="isResolved ? 'Y' : 'N'"
    :data-lifecycle="lcKey || 'OPEN'"
  >
    <!--begin::본문 (헤더·제목 없이 본문만 흐른다 — target 엔트리와 이어지는 글처럼; 접힘 시 flex-column+세로 중앙으로 (collapsed)를 임베드 세로 중앙에 둔다) -->
    <div class="journal-reflection-content flex-grow-1" :class="{ 'd-flex flex-column justify-content-center': isCollapsed }">
      <div v-if="debugCollapse" class="fs-9 text-danger px-2">
        [R#{{reflection.id}}] isCollapsed={{isCollapsed}} | lcKey={{lcKey}} | signal={{props.forceCollapsedSignal}} | localOvr={{localCollapsedOverride}}
      </div>
      <div
        v-if="!isCollapsed && reflection.markdownContent"
        class="journal-content p-2"
        v-html="reflection.markdownContent"
      ></div>
      <div v-else-if="isCollapsed" class="text-muted fs-8 fst-italic ps-2 d-flex align-items-center">(collapsed)</div>
      <!--begin::댓글 (읽기)-->
      <div v-if="commentList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div v-for="cmt in commentList" :key="cmt.id" class="d-flex align-items-start gap-1">
          <div
            class="fs-8 text-muted ps-2 border-start border-2 border-gray-300 flex-grow-1 min-w-0"
            v-html="cmt.markdownContent || cmt.content || ''"
          ></div>
          <div v-if="canWrite" class="d-flex flex-shrink-0 gap-1">
            <button type="button" class="btn btn-xs btn-icon btn-active-light-primary" :title="t('comment.modify')" @click.stop="onEditComment(cmt.id)">
              <i class="bi bi-pencil fs-9"></i>
            </button>
            <button type="button" class="btn btn-xs btn-icon btn-active-light-danger" :title="t('comment.delete')" @click.stop="onDeleteComment(cmt.id)">
              <i class="bi bi-trash fs-9"></i>
            </button>
          </div>
        </div>
      </div>
      <!--end::댓글-->
    </div>
    <!--end::본문-->

    <!--begin::우측 액션 (엔트리와 동일: 댓글·복사·⋯) — 엔트리 액션과 같은 오른쪽 열-->
    <div class="journal-reflection-embed__actions d-flex flex-row align-items-start gap-1">
      <button
        v-if="canWrite"
        type="button"
        class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
        :title="t('comment.register')"
        @click="openCommentRegist"
      >
        <i class="bi bi-chat-dots fs-8"></i>
      </button>
      <button
        type="button"
        class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
        :title="authStore.isLocalProfile ? t('common.copy') + ' (id ' + reflection.id + ')' : t('common.copy')"
        @click="copyReflection"
      >
        <i class="bi bi-copy fs-8"></i>
      </button>
      <button
        type="button"
        :class="['btn btn-xs btn-icon btn-bg-light btn-active-color-primary', { 'is-active': isCollapsed }]"
        :title="isCollapsed ? t('common.expand') : t('common.collapse')"
        @click="toggleCollapse"
      >
        <i :class="['bi pe-0 fs-8', isCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse']"></i>
      </button>
      <div class="me-0">
        <button
          type="button"
          class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
          data-kt-menu-trigger="click"
          data-kt-menu-placement="bottom-end"
          :title="t('common.menu')"
        >
          <i class="ki-solid ki-dots-horizontal fs-6"></i>
        </button>
        <div
          class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
          data-kt-menu="true"
        >
          <div class="menu-item px-3">
            <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('journal.reflection.label') }}</div>
          </div>

          <div v-if="canWrite" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openEdit">
              {{ t('common.edit') }}
              <i class="bi bi-pencil-square fs-8"></i>
            </div>
          </div>

          <div v-if="canWrite" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="wrapEntireNoti">
              {{ t('journal.reflection.wrap-noti') }}
              <i class="bi bi-highlighter fs-8"></i>
            </div>
          </div>

                    <div class="menu-item px-3 my-1 cursor-pointer">
            <div
              :class="['menu-link flex-stack px-3', { 'disabled text-muted': !hasHistory }]"
              @click="hasHistory ? openHistory() : undefined"
            >
              {{ t('journal.reflection.history') }}
              <i class="bi bi-clock-history fs-8"></i>
            </div>
          </div>

          <div v-if="canWrite" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openRelated">
              {{ t('journal.entry.related-content.add') }}
              <i class="bi bi-link-45deg fs-8"></i>
            </div>
          </div>

          <div
            v-if="canWrite && !isPrimaryContentTargetedReflection(reflection)"
            class="menu-item px-3"
            data-kt-menu-trigger="hover"
            data-kt-menu-placement="right-end"
            @mouseenter="ensureThreadOptions"
          >
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('journal.entry.thread.add') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown py-3" style="width: 280px;">
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3 text-primary" @click="startNewThread">
                  {{ t('journal.entry.thread.new') }}
                  <i class="bi bi-plus-lg fs-8"></i>
                </div>
              </div>
              <div class="separator my-2"></div>
              <div
                class="menu-item px-3"
                data-kt-menu-dismiss="false"
                @click.stop
                @keydown.stop
              >
                <div class="menu-content px-3 py-1 w-100">
                  <input
                    v-model="membershipStore.optionKeyword"
                    type="search"
                    class="form-control form-control-sm"
                    :placeholder="t('journal.thread.filter.keyword.placeholder')"
                    data-kt-menu-dismiss="false"
                    @input="scheduleThreadCandidateSearch"
                  />
                  <select
                    v-model="membershipStore.optionPrefix"
                    class="form-select form-select-sm mt-2"
                    :disabled="membershipStore.prefixesLoading"
                    data-kt-menu-dismiss="false"
                    @change="refreshThreadCandidates"
                  >
                    <option value="">{{ t("journal.thread.filter.all-prefixes") }}</option>
                    <option
                      v-for="item in membershipPrefixItems"
                      :key="'refl-thread-prefix-' + item.id"
                      :value="String(item.id)"
                    >
                      {{ item.name }}
                    </option>
                  </select>
                  <div v-if="membershipStore.prefixError" class="text-danger fs-9 mt-1">
                    {{ membershipStore.prefixError }}
                  </div>
                  <label class="form-check form-check-custom form-check-sm form-check-solid mt-2 cursor-pointer">
                    <input
                      v-model="membershipStore.optionIncludeResolved"
                      class="form-check-input"
                      type="checkbox"
                      data-kt-menu-dismiss="false"
                      @change="refreshThreadCandidates"
                    />
                    <span class="form-check-label fs-8 text-gray-700">
                      {{ t("journal.entry.thread.candidates.include-resolved") }}
                    </span>
                  </label>
                </div>
              </div>
              <div class="separator my-2"></div>
              <div v-if="membershipStore.optionsLoading" class="menu-item px-3">
                <span class="menu-link px-3 text-muted fs-8">{{ t('common.loading') }}</span>
              </div>
              <div v-if="membershipStore.optionsError" class="menu-item px-3">
                <span class="menu-content px-3 text-danger fs-8">{{ membershipStore.optionsError }}</span>
              </div>
              <div
                v-if="!membershipStore.optionsLoading
                  && !membershipStore.optionsError
                  && membershipStore.threadOptions.length === 0"
                class="menu-item px-3"
              >
                <span class="menu-content px-3 text-muted fs-8">
                  {{ hasThreadCandidateFilter
                    ? t('journal.entry.thread.search.empty')
                    : t('journal.entry.thread.empty') }}
                </span>
              </div>
              <template v-if="membershipStore.threadOptions.length > 0">
                <div
                  v-for="opt in membershipStore.threadOptions"
                  :key="'refl-thread-opt-' + opt.id"
                  class="menu-item px-3 my-1 cursor-pointer"
                >
                  <div class="menu-link flex-stack px-3" @click="toggleThread(opt)">
                    <span class="min-w-0">
                      <span class="d-block text-truncate">
                        {{ opt.title || t('journal.entry.thread.untitled') }}
                      </span>
                      <span class="d-block text-muted fs-9">
                        <span v-if="threadPrefixName(opt)">{{ threadPrefixName(opt) }}</span>
                        <span
                          v-if="opt.lifecycleKey && opt.lifecycleKey !== 'OPEN'"
                          :class="[
                            threadPrefixName(opt) ? 'ms-1' : '',
                            opt.lifecycleKey === 'PENDING' ? 'text-gray-600' : 'text-success',
                          ]"
                        >{{ threadLifecycleLabel(opt.lifecycleKey) }}</span>
                      </span>
                    </span>
                    <i v-if="opt.member" class="bi bi-check-lg fs-8 text-success"></i>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <div v-if="canWrite" class="separator my-2"></div>

          <div v-if="canWrite" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('common.lifecycle') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <div v-for="lc in lifecycleOptions" :key="'refl-lc-' + lc.key" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-18px h-18px cursor-pointer"
                      type="radio"
                      :name="'refl-lc-' + reflection.id"
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

          <div v-if="canWrite" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('common.status') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <template v-if="canWrite">
                <div v-for="st in statusOptions" :key="'refl-st-' + st.key" class="menu-item px-3">
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
            </div>
          </div>

          <div v-if="canWrite" class="separator my-2"></div>

          <div v-if="canWrite" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3 text-danger" @click="deleteReflection">
              {{ t('common.delete') }}
              <i class="bi bi-trash text-danger p-0 fs-8"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--end::우측 액션-->
  </div>
  <!--end::Reflection 슬림 임베드-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError, swalFire, swalAjaxResult } from "@/shared/utils/swal";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { ref, computed, onBeforeUnmount, watch } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { isPrimaryContentTargetedReflection } from "@/features/journal/utils/journalReflectionThread";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import {
  useJournalThreadMembershipStore,
  type ThreadOption,
} from "@/features/journal/stores/journalThreadMembership";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import { useJournalAttachableActions } from "@/features/journal/shared/composables/useJournalAttachableActions";
import {
  resolveReflectionCollapsed,
  useJournalReflectionDefaultCollapsed,
} from "@/features/journal/utils/journalReflectionCollapseMode";
import { wrapHtmlWithDoubleParen } from "@/features/journal/utils/wrapDoubleParen";

/**
 * target 엔트리 본문과 태그 사이에 슬림 임베드되는 Reflection 한 건.
 * 헤더·제목 없이 본문·댓글만 흐르게 표시해 target 엔트리와 하나의 글처럼 이어지게 하고,
 * 우측 액션(댓글·복사·⋯)은 엔트리 액션과 같은 오른쪽 열에 정렬한다(`.journal-reflection-embed__actions`).
 * same-chapter dedup 으로 1급 행이 숨겨지므로 수정·삭제·이력·라이프사이클·상태(중요/참조)는
 * 이 임베드 컨텍스트 메뉴에서 수행한다. ⋯ 메뉴의 「전체 (( ))」는 저장 본문 각 `<p>`/`<li>`에 Markdown `((...))` 마커를 멱등적으로 씌운다. Reflection→Reflection 중첩 등록 메뉴는 두지 않는다(형제·독립이 기본, REFLECTION_ONE_TYPE §3.1). 일기·꿈·노트를 target으로 둔 Reflection은 스레드 소속 추가를 두지 않는다.
 * 접기(COLLAPSED)는 임베드가 본문을 항상 보이므로 메뉴에 두지 않는다.
 * Reflection 은 완결축 밖이므로 쓰기 가드는 소유권(`isCreatedBy`)이다. 본문 글자 크기는 일기 엔트리와 같다.
 */
const props = withDefaults(defineProps<{
  reflection: JournalEntryDto;
  /**
   * 상위 엔트리의 수동 접힘 전달.
   * "expand"=강제 펼침, "collapse"=강제 접힘, null/undefined=엔트리 미개입(리플렉션 자체 lifecycle 따름).
   * boolean 이 아니라 string 인 이유: Vue 3 Boolean casting 이 absent prop 을 false 로 만들어
   * lifecycle 자동 접힘을 무효화하기 때문이다.
   */
  forceCollapsedSignal?: "expand" | "collapse" | null;
}>(), {
  forceCollapsedSignal: null,
});

const { t } = useLocaleStore();
const modalStore = useJournalModalStore();
const authStore = useAuthStore();
const attachableStore = useAttachableModalStore();
const membershipStore = useJournalThreadMembershipStore();
const threadStore = useJournalThreadStore();
const journalStore = useJournalStore();
const route = useRoute();

const contentType = computed(() => props.reflection.contentType ?? "JOURNAL_REFLECTION");
/** 본인 작성 Reflection 만 쓰기 가능. isCreatedBy 미전달 시 잠그지 않는다. */
const canWrite = computed(() => props.reflection.isCreatedBy !== false);

/** localStorage("debug_collapse") が true のとき接힘 메타정보를 표시한다. */
const debugCollapse = computed(() => localStorage.getItem("debug_collapse") === "true");

const lcKey = computed(() => props.reflection.lifecycle?.lifecycleKey ?? "");
const isResolved = computed(() => lcKey.value === "RESOLVED");
/** 클라이언트 임시 접힘 오버라이드. null=상위 신호 따름, true/false=강제. */
const localCollapsedOverride = ref<boolean | null>(null);
/** 저널 일자 aside 토글. provide 없는 검색·스레드에서는 항상 false(기존 계약). */
const reflectionDefaultCollapsed = useJournalReflectionDefaultCollapsed();

/**
 * 부모 엔트리의 forceCollapsed 가 바뀌면 리플렉션의 로컬 오버라이드를 초기화한다.
 * 부모를 접었다 펼쳤을 때, 이전에 리플렉션을 직접 토글한 상태가 남아 있으면
 * 부모 신호를 무시하는 문제를 방지한다.
 * 신규 등록 일회성 접힘 ID가 이 인스턴스를 가리키면 챕터 펼침→expand signal 로 덮지 않는다.
 */
watch(() => props.forceCollapsedSignal, () => {
  if (
    modalStore.reflectionCreatedCollapseId != null
    && String(modalStore.reflectionCreatedCollapseId) === String(props.reflection.id)
  ) {
    return;
  }
  localCollapsedOverride.value = null;
});
/**
 * 기본 접힘 모드 토글 전환 시 수동 펼침/접힘을 지워 모드 기본 분기가 다시 적용되게 한다.
 */
watch(reflectionDefaultCollapsed, () => {
  localCollapsedOverride.value = null;
});
/**
 * 신규 Reflection 등록 직후: 로컬 접힘을 심어 부모 expand signal·챕터 일회성 펼침보다 우선한다.
 * 서버 lifecycle(PENDING)과 별도로, 등록 직후 화면에서만 일회성으로 적용한다. 수정 저장에는 쓰지 않는다.
 */
watch(
  () => modalStore.reflectionCreatedCollapseId,
  (reflectionId) => {
    if (reflectionId == null || props.reflection.id == null) return;
    if (String(reflectionId) !== String(props.reflection.id)) return;
    localCollapsedOverride.value = true;
    console.info("[JournalReflectionItem] 신규 등록 Reflection을 접힘", { reflectionId });
    modalStore.clearReflectionCreatedCollapse(reflectionId);
  },
  { immediate: true },
);
/**
 * 접힘 우선순위: 로컬 토글 > 엔트리 signal > lifecycle(RESOLVED/PENDING) 자동
 * > 기본 접힘 모드(일자 aside) > 서버 COLLAPSED.
 * signal="expand" 이면 lifecycle·모드 기본 접힘도 무시한다.
 * 신규 등록 일회성 접힘은 로컬 토글(true)로 적용되어 signal보다 앞선다.
 */
const isCollapsed = computed(() => resolveReflectionCollapsed({
  localOverride: localCollapsedOverride.value,
  forceSignal: props.forceCollapsedSignal,
  lifecycleKey: lcKey.value,
  serverCollapsed: hasState("COLLAPSED"),
  defaultCollapsed: reflectionDefaultCollapsed.value,
}));
/** 임베드 자리에서 접힘/펼침만 로컬 토글한다(서버 상태 무변경). */
function toggleCollapse(): void {
  localCollapsedOverride.value = !isCollapsed.value;
}
const hasHistory = computed(() => !!props.reflection.history?.historyTriggeredAt);
const commentList = computed(() => props.reflection.comment?.list ?? []);

const lifecycleOptions = computed(() => [
  { key: "OPEN", label: t("journal.reflection.lifecycle.open"), activeClass: "text-gray-800" },
  { key: "PENDING", label: t("lifecycle.pending"), activeClass: "text-gray-600" },
  { key: "RESOLVED", label: t("status.completed"), activeClass: "text-success" },
]);

const statusOptions = computed(() => [
  { key: "IMPRTC", label: t("state.important"), activeClass: "text-danger" },
  { key: "REFRNC", label: t("state.reference"), activeClass: "text-warning" },
]);

const membershipPrefixItems = computed(() => membershipStore.prefixOptions);
const hasThreadCandidateFilter = computed(() =>
  membershipStore.optionKeyword.trim() !== "" || membershipStore.optionPrefix !== "",
);

let threadCandidateSearchTimer: ReturnType<typeof setTimeout> | undefined;

function hasState(key: string): boolean {
  return (props.reflection.state?.list ?? []).some((s) => s.stateKey === key);
}

/** 소유권 없을 때 쓰기 액션을 막는다. */
function guardWrite(): boolean {
  if (canWrite.value) return true;
  void swalAlert(t("journal.chapter.owner-only"));
  return false;
}

/** 공유 composable: setLifecycle, toggleState, cacheContext */
const reflectionRef = computed(() => props.reflection);
const {
  setLifecycle,
  toggleState,
} = useJournalAttachableActions({
  entry: reflectionRef,
  contentType,
  guardWrite,
  onSuccess: () => refreshHost(),
  t,
});

/** 액션 성공 후 현재 표시 호스트를 재조회한다. 호출자가 await 할 수 있도록 재조회 promise 를 반환한다. */
function refreshHost(stdrdDt = props.reflection.stdrdDt): Promise<unknown> {
  return refreshJournalEntryHostForRoute(journalStore, threadStore, route, stdrdDt);
}

/** 임베드에서 Reflection 수정 모달을 연다. 목록에 실린 target 을 함께 넘겨 상세 누락 시에도 태그 UI를 숨긴다. */
function openEdit(): void {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;
  void modalStore.openReflectionRegist({
    id: props.reflection.id,
    refId: props.reflection.refId,
    refContentType: props.reflection.refContentType,
    journalDayId: props.reflection.journalDayId,
    journalChapterId: props.reflection.journalChapterId,
    stdrdDt: props.reflection.stdrdDt,
  });
}

function openCommentRegist(): void {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;
  attachableStore.openCommentRegist(props.reflection.id, contentType.value);
}

/** 인라인 댓글 수정 — 기존 CommentRegistModal 수정 모드를 재사용한다. */
function onEditComment(id: number): void {
  if (!guardWrite()) return;
  void attachableStore.openCommentModify(id);
}

/** 인라인 댓글 삭제 — 확인 후 삭제하고 호스트를 재조회한다. */
async function onDeleteComment(id: number): Promise<void> {
  if (!guardWrite()) return;
  if (!await swalConfirm(t("comment.delete.confirm"))) return;
  try {
    if (await attachableStore.deleteComment(id)) {
      await swalAlert(t("common.result.deleted"));
      void refreshHost();
    }
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("common.result.failure"));
  }
}

function openHistory(): void {
  if (!props.reflection.id) return;
  void attachableStore.openHistory(contentType.value, props.reflection.id, {
    writeLocked: !canWrite.value,
  });
}

function openRelated(): void {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;
  attachableStore.openRelated(contentType.value, props.reflection.id);
}

function ensureThreadOptions(): void {
  if (!props.reflection.id) return;
  void membershipStore.openThreadOptions(props.reflection.id);
}

function scheduleThreadCandidateSearch(): void {
  if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
  const entryId = props.reflection.id;
  if (!entryId) return;
  threadCandidateSearchTimer = setTimeout(() => {
    if (membershipStore.candidateEntryId === entryId) {
      void membershipStore.fetchThreadOptions(entryId);
    }
  }, 250);
}

function refreshThreadCandidates(): void {
  if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
  const entryId = props.reflection.id;
  if (!entryId || membershipStore.candidateEntryId !== entryId) return;
  void membershipStore.fetchThreadOptions(entryId);
}

function threadLifecycleLabel(lifecycleKey: string): string {
  if (lifecycleKey === "PENDING") return t("lifecycle.pending");
  if (lifecycleKey === "RESOLVED") return t("status.completed");
  return t("journal.entry.lifecycle.open");
}

function threadPrefixName(option: ThreadOption): string {
  return option.prefix?.name ?? "";
}

async function toggleThread(option: ThreadOption): Promise<void> {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;
  const entryId = props.reflection.id;
  const ok = option.member
    ? await membershipStore.removeFromThread(option.id, entryId)
    : await membershipStore.addToThread(option.id, entryId);
  if (ok) {
    if (membershipStore.candidateEntryId === entryId) {
      await membershipStore.fetchThreadOptions(entryId);
    }
    void threadStore.refreshPeriodSummary();
    refreshHost();
  }
}

/**
 * 말머리·제목을 받아 새 스레드를 만들고 이 Reflection 을 소속시킨다.
 * 서브메뉴 말머리 필터가 있으면 생성 폼의 기본값으로 쓴다.
 */
async function startNewThread(): Promise<void> {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;
  await membershipStore.fetchPrefixOptions();
  const defaultPrefix = membershipStore.optionPrefix;
  const prefixOptionsHtml = membershipStore.prefixOptions
    .map((item) => {
      const id = String(item.id);
      const selected = id === defaultPrefix ? " selected" : "";
      const name = escapeHtmlAttr(item.name ?? "");
      return `<option value="${id}"${selected}>${name}</option>`;
    })
    .join("");
  const result = await swalFire({
    title: t("journal.entry.thread.new"),
    html: [
      `<div class="text-start">`,
      `<label class="form-label fs-7 mb-1">${escapeHtmlAttr(t("journal.thread.prefix.label"))}</label>`,
      `<select id="swal-refl-thread-prefix" class="form-select form-select-sm mb-3">`,
      `<option value="">${escapeHtmlAttr(t("journal.thread.prefix.select"))}</option>`,
      prefixOptionsHtml,
      `</select>`,
      `<label class="form-label fs-7 mb-1">${escapeHtmlAttr(t("journal.entry.thread.new.prompt"))}</label>`,
      `<input id="swal-refl-thread-title" class="form-control form-control-sm" maxlength="200"`,
      ` placeholder="${escapeHtmlAttr(t("journal.entry.thread.new.placeholder"))}" />`,
      `</div>`,
    ].join(""),
    focusConfirm: false,
    showCancelButton: true,
    confirmButtonText: t("common.save"),
    cancelButtonText: t("common.cancel"),
    didOpen: () => {
      const titleEl = document.getElementById("swal-refl-thread-title") as HTMLInputElement | null;
      titleEl?.focus();
    },
    preConfirm: () => {
      const titleEl = document.getElementById("swal-refl-thread-title") as HTMLInputElement | null;
      const prefixEl = document.getElementById("swal-refl-thread-prefix") as HTMLSelectElement | null;
      const title = titleEl?.value?.trim() ?? "";
      if (!title) {
        Swal.showValidationMessage(t("journal.entry.thread.new.required"));
        return false;
      }
      const prefixRaw = prefixEl?.value ?? "";
      const prefixId = prefixRaw ? Number(prefixRaw) : null;
      return { title, prefixId: Number.isFinite(prefixId as number) ? prefixId : null };
    },
  });
  if (!result.isConfirmed || !result.value || typeof result.value !== "object") return;
  const created = result.value as { title: string; prefixId: number | null };
  const entryId = props.reflection.id;
  const ok = await membershipStore.createThreadAndAdd(created.title, entryId, created.prefixId);
  if (ok) {
    if (membershipStore.candidateEntryId === entryId) {
      await membershipStore.fetchThreadOptions(entryId);
    }
    void threadStore.refreshPeriodSummary();
    refreshHost();
  }
}

/** Swal HTML 옵션·라벨용 최소 이스케이프 */
function escapeHtmlAttr(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/** Reflection 삭제 (DELETE /api/journal/reflection/{id}) */
async function deleteReflection(): Promise<void> {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;
  const stdrdDt = props.reflection.stdrdDt;
  const confirmed = await swalConfirm(t("journal.reflection.delete.confirm"));
  if (!confirmed) return;
  try {
    const res = await axios.delete(`/api/journal/reflection/${props.reflection.id}`);
    if (res.data?.rslt) {
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: t("common.result.deleted"),
      });
      void threadStore.refreshPeriodSummary();
      refreshHost(stdrdDt);
    } else {
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("journal.reflection.delete.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}


/**
 * 본문 전체에 Markdown `((...))`(md-text-noti) 를 멱등 적용한다.
 * 변경이 없으면 API 를 호출하지 않는다. 목록에 content 가 없으면 상세를 조회한다.
 * 저장은 Reflection modify 경로를 쓴다.
 */
async function wrapEntireNoti(): Promise<void> {
  if (!guardWrite()) return;
  if (!props.reflection.id) return;

  let raw = props.reflection.content ?? "";
  if (!raw.trim()) {
    try {
      const detail = await axios.get(`/api/journal/reflection/${props.reflection.id}`);
      raw = detail.data?.obj?.content ?? "";
    } catch (e: unknown) {
      void swalRequestError(e);
      return;
    }
  }
  if (!raw.trim()) {
    void swalFire({ icon: "info", text: t("journal.reflection.wrap-noti.empty") });
    return;
  }

  const { html, changed } = wrapHtmlWithDoubleParen(raw);
  if (!changed) {
    void swalFire({ icon: "info", text: t("journal.reflection.wrap-noti.already") });
    return;
  }

  const confirmed = await swalConfirm(t("journal.reflection.wrap-noti.confirm"));
  if (!confirmed) return;

  try {
    const formData = new FormData();
    formData.append("id", String(props.reflection.id));
    formData.append("contentType", contentType.value);
    if (props.reflection.refId != null) formData.append("refId", String(props.reflection.refId));
    if (props.reflection.refContentType) formData.append("refContentType", props.reflection.refContentType);
    formData.append("title", props.reflection.title ?? "");
    formData.append("content", html);

    const res = await axios.post(`/api/journal/reflection/${props.reflection.id}`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (res.data?.rslt) {
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: t("journal.reflection.wrap-noti.done"),
      });
      refreshHost();
    } else {
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("journal.reflection.wrap-noti.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

async function copyReflection(): Promise<void> {
  const weekDay = getWeekDayStr(props.reflection.stdrdDt, t);
  const dateLine = weekDay
    ? `${props.reflection.stdrdDt} (${weekDay})`
    : (props.reflection.stdrdDt ?? "");
  const raw = htmlToPlainText(props.reflection.markdownContent ?? props.reflection.content ?? "");
  const text = [dateLine, raw].filter(Boolean).join("\n");
  try {
    await navigator.clipboard.writeText(text);
    void swalFire({ icon: "success", text: t("common.copy.success") });
  } catch (error: unknown) {
    console.error("[journal-reflection] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

onBeforeUnmount(() => {
  if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
});
</script>
