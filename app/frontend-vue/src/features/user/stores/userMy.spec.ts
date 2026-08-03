import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useUserMyStore, type UserMyUpdatePayload } from "./userMy";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    post: vi.fn(),
  },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({
    t: (key: string) => key,
  }),
}));

const FIXTURE_USER = {
  username: "alice",
  nickname: "Alice",
  phoneNumber: "010-0000-0000",
  profile: {
    brthdy: "2000-01-01",
    lunarYn: "N",
    proflCn: "가상 사용자 소개",
  },
};

describe("userMy store 개인 프로필 수정", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPut = vi.mocked(axios.put);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("허용된 개인 프로필 필드만 JSON으로 저장하고 서버 상태를 다시 조회한다", async () => {
    mockedPut.mockResolvedValue({ data: { rslt: true } });
    mockedGet.mockResolvedValue({ data: { rslt: true, rsltObj: FIXTURE_USER } });
    const store = useUserMyStore();
    const payload: UserMyUpdatePayload = {
      nickname: FIXTURE_USER.nickname,
      phoneNumber: FIXTURE_USER.phoneNumber,
      brthdy: FIXTURE_USER.profile.brthdy,
      lunarYn: "N",
      proflCn: FIXTURE_USER.profile.proflCn,
    };

    await store.updateMyInfo(payload);

    expect(mockedPut).toHaveBeenCalledWith("/api/user/my", payload);
    expect(mockedGet).toHaveBeenCalledWith("/api/user/my");
    expect(store.user.nickname).toBe(FIXTURE_USER.nickname);
    expect(store.saving).toBe(false);
  });

  it("저장 실패를 정상 상태로 가장하지 않고 서버 메시지를 전달한다", async () => {
    mockedPut.mockResolvedValue({ data: { rslt: false, message: "가상 저장 실패" } });
    const store = useUserMyStore();

    await expect(store.updateMyInfo({
      nickname: FIXTURE_USER.nickname,
      phoneNumber: null,
      brthdy: null,
      lunarYn: "N",
      proflCn: null,
    })).rejects.toThrow("가상 저장 실패");

    expect(mockedGet).not.toHaveBeenCalled();
    expect(store.saving).toBe(false);
  });
});
