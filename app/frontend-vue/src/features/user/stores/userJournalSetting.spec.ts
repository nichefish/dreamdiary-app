import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import {
  resolveJournalDefaultEntryRouteName,
  useUserJournalSettingStore,
} from "./userJournalSetting";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
  },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({
    t: (key: string) => key,
  }),
}));

describe("userJournalSetting store", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPut = vi.mocked(axios.put);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("로그인 사용자의 서버 확정 기본 진입 화면을 조회한다", async () => {
    mockedGet.mockResolvedValue({
      data: { rslt: true, rsltObj: { defaultEntryView: "WEEKLY" } },
    });
    const store = useUserJournalSettingStore();

    await expect(store.fetchSetting()).resolves.toBe("WEEKLY");

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/settings/me");
    expect(store.defaultEntryView).toBe("WEEKLY");
    expect(store.loaded).toBe(true);
    expect(store.loading).toBe(false);
  });

  it("선택값을 저장하고 응답의 서버 확정값으로 상태를 갱신한다", async () => {
    mockedPut.mockResolvedValue({
      data: { rslt: true, rsltObj: { defaultEntryView: "MONTHLY" } },
    });
    const store = useUserJournalSettingStore();

    await expect(store.saveSetting("MONTHLY")).resolves.toBe("MONTHLY");

    expect(mockedPut).toHaveBeenCalledWith("/api/journal/settings/me", {
      defaultEntryView: "MONTHLY",
    });
    expect(store.defaultEntryView).toBe("MONTHLY");
    expect(store.saving).toBe(false);
  });

  it("지원하지 않는 서버 값을 기본값으로 가장하지 않고 실패시킨다", async () => {
    mockedGet.mockResolvedValue({
      data: { rslt: true, rsltObj: { defaultEntryView: "UNKNOWN" } },
    });
    const store = useUserJournalSettingStore();

    await expect(store.fetchSetting()).rejects.toThrow("user.my.journal.value.invalid");

    expect(store.loaded).toBe(false);
    expect(store.errorMessage).toBe("user.my.journal.value.invalid");
  });

  it("각 설정값을 저널 일자 명시적 route로 해석한다", () => {
    expect(resolveJournalDefaultEntryRouteName("DAILY")).toBe("journal-daily-tab");
    expect(resolveJournalDefaultEntryRouteName("WEEKLY")).toBe("journal-weekly");
    expect(resolveJournalDefaultEntryRouteName("MONTHLY")).toBe("journal-monthly");
  });
});
