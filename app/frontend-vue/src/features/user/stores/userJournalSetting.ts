import { ref } from "vue";
import { defineStore } from "pinia";
import { apiGet, apiPut, unwrapObj } from "@/shared/api/client";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

export const JOURNAL_DEFAULT_ENTRY_VIEWS = ["DAILY", "WEEKLY", "MONTHLY"] as const;
export type JournalDefaultEntryView = (typeof JOURNAL_DEFAULT_ENTRY_VIEWS)[number];

interface JournalUserSetting {
  defaultEntryView: JournalDefaultEntryView;
}

const DEFAULT_ENTRY_VIEW: JournalDefaultEntryView = "DAILY";

const ENTRY_VIEW_ROUTE_NAMES: Record<JournalDefaultEntryView, string> = {
  DAILY: "journal-daily-tab",
  WEEKLY: "journal-weekly",
  MONTHLY: "journal-monthly",
};

/** 서버가 반환한 저널 기본 진입 화면을 대응하는 Vue route 이름으로 변환한다. */
export function resolveJournalDefaultEntryRouteName(view: JournalDefaultEntryView): string {
  return ENTRY_VIEW_ROUTE_NAMES[view];
}

function parseDefaultEntryView(value: unknown, failureMessage: string): JournalDefaultEntryView {
  if (typeof value === "string" && JOURNAL_DEFAULT_ENTRY_VIEWS.includes(value as JournalDefaultEntryView)) {
    return value as JournalDefaultEntryView;
  }
  throw new Error(failureMessage);
}

/** 로그인 사용자의 저널 기본 진입 화면 조회·저장을 관리한다. */
export const useUserJournalSettingStore = defineStore("userJournalSetting", () => {
  const { t } = useLocaleStore();
  const defaultEntryView = ref<JournalDefaultEntryView>(DEFAULT_ENTRY_VIEW);
  const loading = ref(false);
  const saving = ref(false);
  const loaded = ref(false);
  const errorMessage = ref("");

  /** 서버 확정 설정을 조회한다. 미설정 사용자의 DAILY 기본값도 서버 응답으로 받는다. */
  async function fetchSetting(): Promise<JournalDefaultEntryView> {
    loading.value = true;
    errorMessage.value = "";
    try {
      const setting = unwrapObj<JournalUserSetting>(
        await apiGet<JournalUserSetting>("/api/journal/settings/me"),
        t("user.my.journal.load.failure"),
      );
      const resolved = parseDefaultEntryView(
        setting?.defaultEntryView,
        t("user.my.journal.value.invalid"),
      );
      defaultEntryView.value = resolved;
      loaded.value = true;
      console.info("[user-journal-setting] setting loaded", { defaultEntryView: resolved });
      return resolved;
    } catch (error) {
      loaded.value = false;
      errorMessage.value = error instanceof Error ? error.message : t("user.my.journal.load.failure");
      console.error("[user-journal-setting] setting load failed", error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /** 선택값을 저장하고 응답에 포함된 서버 확정값으로 로컬 상태를 갱신한다. */
  async function saveSetting(nextView: JournalDefaultEntryView): Promise<JournalDefaultEntryView> {
    saving.value = true;
    errorMessage.value = "";
    try {
      const setting = unwrapObj<JournalUserSetting>(
        await apiPut<JournalUserSetting>("/api/journal/settings/me", { defaultEntryView: nextView }),
        t("user.my.journal.save.failure"),
      );
      const resolved = parseDefaultEntryView(
        setting?.defaultEntryView,
        t("user.my.journal.value.invalid"),
      );
      defaultEntryView.value = resolved;
      loaded.value = true;
      console.info("[user-journal-setting] setting saved", { defaultEntryView: resolved });
      return resolved;
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("user.my.journal.save.failure");
      console.error("[user-journal-setting] setting save failed", error);
      throw error;
    } finally {
      saving.value = false;
    }
  }

  return {
    defaultEntryView,
    loading,
    saving,
    loaded,
    errorMessage,
    fetchSetting,
    saveSetting,
  };
});
