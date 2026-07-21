<template>
  <!--begin::해석 행-->
  <div
    class="journal-interpretation-item ps-7 d-flex gap-2 py-1"
    :data-id="interpretation.id"
    :data-resolved="isResolved ? 'Y' : 'N'"
  >
    <!--begin::순번-->
    <div class="d-none d-md-flex flex-column align-items-center pt-1 ps-2" style="width:56px; min-width:56px;">
      <span :class="['fw-bold fs-7', isResolved ? 'text-success' : 'text-muted']">#{{ interpretation.sortOrder }}</span>
      <button
        type="button"
        :class="['btn btn-xs px-1 mt-1', { 'is-active': isCollapsed }]"
        :title="isCollapsed ? t('common.expand') : t('common.collapse')"
        @click="toggleInterpretation"
      >
        <i :class="['bi pe-0 fs-8', isCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse']"></i>
      </button>
    </div>
    <!--end::순번-->

    <!--begin::본문 영역-->
    <div class="journal-interpretation-content flex-grow-1">
      <!--begin::제목-->
      <div v-if="interpretation.title" class="fw-semibold fs-7 mb-1">{{ interpretation.title }}</div>
      <!--end::제목-->
      <!--begin::마크다운 본문-->
      <div
        v-if="!isCollapsed && interpretation.markdownContent"
        class="journal-content fs-7 p-2"
        v-html="interpretation.markdownContent"
      ></div>
      <div v-else-if="isCollapsed" class="text-muted fs-8 fst-italic ps-2">{{ t('journal.interpretation.collapsed') }}</div>
      <!--end::마크다운 본문-->
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
    <div class="d-flex flex-row align-items-start pt-1 gap-1" style="min-width:80px;">
      <!--begin::댓글 등록 버튼-->
      <button
        v-if="axisWritable"
        type="button"
        class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
        :title="t('comment.register')"
        @click="openCommentRegist"
      >
        <i class="bi bi-chat-dots fs-8"></i>
      </button>
      <!--end::댓글 등록 버튼-->

      <!--begin::복사 버튼-->
      <button
        type="button"
        class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
        :title="t('common.copy')"
        @click="copyInterpretation"
      >
        <i class="bi bi-copy fs-8"></i>
      </button>
      <!--end::복사 버튼-->

      <!--begin::컨텍스트 메뉴-->
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
          <!--begin::메뉴 헤더-->
          <div class="menu-item px-3">
            <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('common.interpretation') }}</div>
          </div>
          <!--end::메뉴 헤더-->

          <!--begin::수정-->
          <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openModify">
              {{ t('common.edit') }}
              <i class="bi bi-pencil-square fs-8"></i>
            </div>
          </div>
          <!--end::수정-->

          <!--begin::이력 (historyTriggeredAt 없으면 disabled)-->
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div
              :class="['menu-link flex-stack px-3', { 'disabled text-muted': !hasHistory }]"
              @click="hasHistory ? openHistory() : undefined"
            >
              {{ t('journal.interpretation.history') }}
              <i class="bi bi-clock-history fs-8"></i>
            </div>
          </div>
          <!--end::이력-->

          <div v-if="axisWritable" class="separator my-2"></div>

          <!--begin::라이프사이클 서브메뉴-->
          <div v-if="axisWritable" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('common.lifecycle') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <div v-for="lc in lifecycleOptions" :key="'lc-' + lc.key" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-18px h-18px cursor-pointer"
                      type="radio"
                      :name="'interpretation-lc-' + interpretation.id"
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

          <div class="separator my-2"></div>

          <!--begin::상태 서브메뉴 (쓰기 잠금 시 접기만)-->
          <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('common.status') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <div v-if="axisWritable" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-30px h-20px cursor-pointer"
                      type="checkbox"
                      :checked="hasState('IMPRTC')"
                      @click="toggleState('IMPRTC')"
                    />
                    <span class="form-check-label fs-7" :class="hasState('IMPRTC') ? 'text-danger' : 'text-muted'">{{ t('state.important') }}</span>
                  </label>
                </div>
              </div>
              <div class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-30px h-20px cursor-pointer"
                      type="checkbox"
                      :checked="hasState('COLLAPSED')"
                      @click="toggleState('COLLAPSED')"
                    />
                    <span class="form-check-label fs-7" :class="hasState('COLLAPSED') ? 'text-gray-700' : 'text-muted'">{{ t('common.collapse') }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          <!--end::상태 서브메뉴-->

          <div v-if="axisWritable" class="separator my-2"></div>

          <!--begin::삭제-->
          <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3 text-danger" @click="deleteInterpretation">
              {{ t('common.delete') }}
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
  <!--end::해석 행-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError, swalFire, swalAjaxResult } from "@/shared/utils/swal";
import { ref, computed, nextTick } from "vue";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import type { InterpretationItem } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useJournalDayResolved } from "@/features/journal/utils/journalDayResolved";

const props = defineProps<{
  interpretation: InterpretationItem;
  /** 부모 엔트리가 꿈 축이면 true — refContentType 보조 */
  isDream?: boolean;
}>();

const modalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();

const isDreamInterpretation = computed(
  () =>
    props.isDream === true
    || props.interpretation.refContentType === "JOURNAL_DREAM",
);
const dayResolvedAxis = useJournalDayResolved();
const axisWritable = computed(() =>
  isDreamInterpretation.value
    ? dayResolvedAxis.value.dreamWritable
    : dayResolvedAxis.value.diaryWritable,
);

function guardAxisWrite(): boolean {
  if (axisWritable.value) return true;
  void swalAlert(
    t(isDreamInterpretation.value ? "journal.day.dream-resolved-locked" : "journal.day.diary-resolved-locked"),
  );
  return false;
}
const journalStore = useJournalStore();
const { t } = useLocaleStore();
const route = useRoute();

const lcKey = computed(() => props.interpretation.lifecycle?.lifecycleKey ?? "");
const isResolved = computed(() => lcKey.value === "RESOLVED");
const hasHistory = computed(() => !!props.interpretation.history?.historyTriggeredAt);

const localCollapsedOverride = ref<boolean | null>(null);

const isCollapsed = computed(() => {
  if (localCollapsedOverride.value !== null) return localCollapsedOverride.value;
  if (isResolved.value) return true;
  return hasState("COLLAPSED");
});

function hasState(key: string): boolean {
  return (props.interpretation.state?.list ?? []).some((s) => s.stateKey === key);
}

const commentList = computed(() => props.interpretation.comment?.list ?? []);

/** 라이프사이클 옵션 */
const lifecycleOptions = computed(() => [
  { key: "OPEN",     label: t("journal.interpretation.lifecycle.open"), activeClass: "text-gray-800" },
  { key: "PENDING",  label: t("lifecycle.pending"), activeClass: "text-primary"  },
  { key: "RESOLVED", label: t("status.completed"),  activeClass: "text-success"  },
]);

function toggleInterpretation(): void {
  localCollapsedOverride.value = !isCollapsed.value;
}

/** 해석 수정 모달 열기 */
function openModify(): void {
  if (!guardAxisWrite()) return;
  modalStore.openInterpretationRegist({
    id: props.interpretation.id,
    refId: props.interpretation.refId,
    refContentType: props.interpretation.refContentType,
    stdrdDt: props.interpretation.stdrdDt,
  });
}

/** 댓글 등록 모달 열기 */
function openCommentRegist(): void {
  if (!guardAxisWrite()) return;
  attachableStore.openCommentRegist(props.interpretation.id, "JOURNAL_INTERPRETATION");
}

/** 이력 모달 열기 */
function openHistory(): void {
  void attachableStore.openHistory("JOURNAL_INTERPRETATION", props.interpretation.id, {
    writeLocked: !axisWritable.value,
  });
}

/** fetchDays 완료 후 해당 일자로 스크롤 */
function scrollAfterFetch(): void {
  const dt = props.interpretation.stdrdDt;
  if (!dt) return;
  const afterFetch = () => {
    void nextTick(() => {
      const el = document.getElementById(`journal-day-${dt}`);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  };
  void refreshJournalDaysForRoute(journalStore, route, dt).then(afterFetch);
}

/** 라이프사이클 설정 */
async function setLifecycle(lifecycleKey: string): Promise<void> {
  if (!guardAxisWrite()) return;
  try {
    const res = await axios.put("/api/lifecycles", {
      id: props.interpretation.id,
      contentType: "JOURNAL_INTERPRETATION",
      lifecycleKey,
    });
    if (res.data?.rslt) scrollAfterFetch();
    else void swalFire({ icon: "error", text: res.data?.message ?? t("common.result.failure") });
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

/** 상태 토글 */
async function toggleState(stateKey: string): Promise<void> {
  if (stateKey !== "COLLAPSED" && !guardAxisWrite()) return;
  try {
    const res = await axios.post("/api/states", {
      id: props.interpretation.id,
      contentType: "JOURNAL_INTERPRETATION",
      stateKey,
    });
    if (res.data?.rslt) scrollAfterFetch();
    else void swalFire({ icon: "error", text: res.data?.message ?? t("common.result.failure") });
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

/** 해석 내용 클립보드 복사 */
async function copyInterpretation(): Promise<void> {
  const weekDay = getWeekDayStr(props.interpretation.stdrdDt, t);
  const dateLine = weekDay
    ? `${props.interpretation.stdrdDt} (${weekDay})`
    : (props.interpretation.stdrdDt ?? "");
  const raw = htmlToPlainText(props.interpretation.markdownContent ?? props.interpretation.content ?? "");
  const text = [dateLine, raw].filter(Boolean).join("\n");
  try {
    await navigator.clipboard.writeText(text);
    void swalFire({ icon: "success", text: t("common.copy.success") });
  } catch (error: unknown) {
    console.error("[journal-interpretation] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

/** 해석 삭제 */
async function deleteInterpretation(): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!await swalConfirm(t("journal.interpretation.delete.confirm"))) return;
  try {
    const res = await axios.delete(`/api/journal/interpretation/${props.interpretation.id}`);
    if (res.data?.rslt) {
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: t("common.result.deleted"),
      });
      void refreshJournalDaysForRoute(journalStore, route, props.interpretation.stdrdDt);
    }
    else void swalAjaxResult({
      rslt: false,
      message: res.data?.message,
      failureFallback: t("journal.interpretation.delete.failure"),
    });
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}
</script>
