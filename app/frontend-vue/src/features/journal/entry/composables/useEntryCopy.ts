/**
 * 엔트리 복사 텍스트·딥링크 조립과 클립보드 결과 처리를 캡슐화한 composable.
 *
 * 본문 복사는 저널 공통 3단계 범위 계약(full/no-pending/body)을 따르고,
 * 링크 복사는 해당 엔트리의 일간뷰 위치를 가리키는 절대 URL을 만든다.
 */
import { computed, type ComputedRef } from "vue";
import { joinAppBasePath } from "@/shared/utils/appPath";
import { swalFire } from "@/shared/utils/swal";
import { useAuthStore } from "@/shared/auth/stores/auth";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import {
  appendReflectionsToCopyText,
  type CopyReflectionMode,
  copySuccessKey,
  JOURNAL_COPY_LINE_BREAK,
} from "@/features/journal/utils/journalCopyReflection";

export interface UseEntryCopyOptions {
  /** 현재 Primary 엔트리 reactive 참조. */
  entry: ComputedRef<JournalEntryDto>;
  /** 현재 엔트리를 target으로 하는 리플렉션 목록. */
  reflections: ComputedRef<JournalEntryDto[]>;
  /** 현재 locale의 번역 함수. */
  t: (key: string) => string;
}

/**
 * 엔트리의 복사 tooltip과 본문·딥링크 복사 액션을 제공한다.
 *
 * 반환값은 `JournalEntryItem` 복사 split 버튼과 링크 복사 메뉴에서 직접 사용한다.
 */
export function useEntryCopy(options: UseEntryCopyOptions) {
  const { entry, reflections, t } = options;
  const authStore = useAuthStore();

  /** 복사(해석 포함) 버튼 tooltip. 리플렉션이 있으면 "해석 포함"을 명시하고, 로컬 프로필은 id 를 덧붙인다. */
  const copyIncludeTitle = computed(() => {
    const base = reflections.value.length > 0 ? t("journal.copy.full.tooltip") : t("common.copy");
    return authStore.isLocalProfile ? `${base} (id ${entry.value.id})` : base;
  });

  /** 엔트리 내용을 클립보드에 복사한다. 형식: 날짜(요일) → 본문 → 그 엔트리를 문(target) 리플렉션 본문(원문·해석은 한 몸으로 함께 복사). */
  async function copyEntry(mode: CopyReflectionMode = "full"): Promise<void> {
    const weekDay = getWeekDayStr(entry.value.stdrdDt, t);
    const dateLine = weekDay
      ? `${entry.value.stdrdDt} (${weekDay})`
      : (entry.value.stdrdDt ?? "");
    /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
    const raw = htmlToPlainText(entry.value.content ?? entry.value.markdownContent ?? "");
    const baseText = [dateLine, raw].filter(Boolean).join(JOURNAL_COPY_LINE_BREAK);
    /* 공통 formatter가 모드별 리플렉션 포함 여부와 본문 사이의 CRLF 빈 줄 경계를 함께 보장한다. */
    const text = appendReflectionsToCopyText(baseText, reflections.value, mode);
    try {
      await navigator.clipboard.writeText(text);
      /* 성공 토스트는 복사 범위를 명시한다: 전체/보류 제외/본문만, 리플렉션이 없으면 공용 문구. */
      const successKey = copySuccessKey(mode, reflections.value.length > 0);
      void swalFire({ icon: "success", text: t(successKey) });
    } catch (error: unknown) {
      console.error("[journal-entry-copy] clipboard copy failed", {
        entryId: entry.value.id,
        mode,
      }, error);
      void swalFire({ icon: "error", text: t("common.copy.failure") });
    }
  }

  /**
   * 이 엔트리로 가는 링크를 클립보드에 복사한다.
   * 외부(메신저·메모 등)에서 클릭하면 앱의 해당 일자 일간뷰(journal-daily-tab)로 진입하고,
   * entryId 로 이 엔트리(#journal-entry-{id})까지 스크롤한다. 절대 URL(origin + BASE_URL) 을 만든다.
   */
  async function copyEntryLink(): Promise<void> {
    const stdrdDt = entry.value.stdrdDt;
    if (!stdrdDt || entry.value.id == null) {
      console.warn("[journal-entry-copy] link copy skipped", {
        entryId: entry.value.id,
        stdrdDt,
        reason: "missing-entry-axis",
      });
      return;
    }
    const path = joinAppBasePath(`/journal/daily?stdrdDt=${encodeURIComponent(stdrdDt)}&entryId=${entry.value.id}`);
    const url = `${window.location.origin}${path}`;
    try {
      await navigator.clipboard.writeText(url);
      void swalFire({ icon: "success", text: t("common.copy.success") });
    } catch (error: unknown) {
      console.error("[journal-entry-copy] link copy failed", {
        entryId: entry.value.id,
        stdrdDt,
      }, error);
      void swalFire({ icon: "error", text: t("common.copy.failure") });
    }
  }

  return {
    copyIncludeTitle,
    copyEntry,
    copyEntryLink,
  };
}
