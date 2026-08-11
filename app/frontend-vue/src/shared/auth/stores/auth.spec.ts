/**
 * auth store 인증 확인·사용자 세션 초기화 계약.
 * 짧은 신선도 재사용과 동시 요청 병합, 로그아웃 전 응답 폐기를 함께 고정한다.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";

vi.mock("@metronic/core/services/ApiService", () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));
vi.mock("@/features/journal/stores/journalModal", () => ({
  preloadCategoryMaps: vi.fn(() => Promise.resolve()),
  useJournalModalStore: () => ({ resetCategoryMaps: vi.fn() }),
}));
vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));
vi.mock("@/shared/utils/metronicReinit", () => ({
  reinitMetronicAfterDom: vi.fn(() => Promise.resolve()),
}));
vi.mock("@/shared/menu/stores/menu", () => ({
  useMenuStore: () => ({ resetMenu: vi.fn() }),
}));

import { useJournalStore } from "@/features/journal/stores/journal";
import ApiService from "@metronic/core/services/ApiService";
import {
  AUTH_VERIFICATION_FRESHNESS_MS,
  useAuthStore,
  type AuthUser,
} from "./auth";

const FIXTURE_AUTH_USER: AuthUser = {
  username: "fixture-user",
  nickname: "Fixture User",
  email: "fixture@example.invalid",
  profileImageUrl: "",
  roles: [],
  isMngr: false,
  isDev: false,
};

const mockedApiGet = vi.mocked(ApiService.get);
type ApiGetResponse = Awaited<ReturnType<typeof ApiService.get>>;

/** 정상 인증 확인 Axios 응답 픽스처. */
function verifiedResponse(): ApiGetResponse {
  return {
    data: { rslt: true, rsltObj: FIXTURE_AUTH_USER },
  } as unknown as ApiGetResponse;
}

/** 응답 타이밍을 테스트가 직접 제어하기 위한 deferred 헬퍼. */
function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

/** node 환경의 저널 store 초기화를 위한 in-memory localStorage stub. */
function makeLocalStorageStub() {
  const bag = new Map<string, string>();
  return {
    getItem: (key: string) => bag.get(key) ?? null,
    setItem: (key: string, value: string) => void bag.set(key, value),
    removeItem: (key: string) => void bag.delete(key),
    clear: () => bag.clear(),
  };
}

describe("auth store 인증 확인과 사용자 세션 초기화", () => {
  beforeEach(() => {
    vi.stubGlobal("localStorage", makeLocalStorageStub());
    setActivePinia(createPinia());
    mockedApiGet.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it("purgeAuth 는 저널 태그 클라우드 상태를 초기화한다", () => {
    const journalStore = useJournalStore();
    const resetTagCloudState = vi.spyOn(journalStore, "resetTagCloudState");
    const authStore = useAuthStore();

    authStore.purgeAuth();

    expect(resetTagCloudState).toHaveBeenCalledTimes(1);
  });

  it("동시 verifyAuth 는 진행 중 서버 요청을 공유한다", async () => {
    const response = deferred<ApiGetResponse>();
    mockedApiGet.mockReturnValueOnce(response.promise);
    const authStore = useAuthStore();

    const first = authStore.verifyAuth();
    const second = authStore.verifyAuth();
    expect(mockedApiGet).toHaveBeenCalledTimes(1);

    response.resolve(verifiedResponse());
    await Promise.all([first, second]);
    expect(authStore.isAuthenticated).toBe(true);
  });

  it("신선도 구간의 라우트 인증 확인은 서버 요청을 재사용한다", async () => {
    const now = vi.spyOn(Date, "now").mockReturnValue(1_000);
    mockedApiGet.mockResolvedValue(verifiedResponse());
    const authStore = useAuthStore();

    await authStore.verifyAuth();
    now.mockReturnValue(1_000 + AUTH_VERIFICATION_FRESHNESS_MS - 1);
    await authStore.verifyAuth();

    expect(mockedApiGet).toHaveBeenCalledTimes(1);
  });

  it("신선도 만료와 force 옵션은 서버 인증 정보를 다시 조회한다", async () => {
    const now = vi.spyOn(Date, "now").mockReturnValue(1_000);
    mockedApiGet.mockResolvedValue(verifiedResponse());
    const authStore = useAuthStore();

    await authStore.verifyAuth();
    await authStore.verifyAuth({ force: true });
    now.mockReturnValue(1_000 + AUTH_VERIFICATION_FRESHNESS_MS);
    await authStore.verifyAuth();

    expect(mockedApiGet).toHaveBeenCalledTimes(3);
  });

  it("purgeAuth 전에 시작한 인증 성공 응답은 로그인 상태를 복원하지 않는다", async () => {
    const response = deferred<ApiGetResponse>();
    mockedApiGet.mockReturnValueOnce(response.promise);
    const authStore = useAuthStore();

    const verification = authStore.verifyAuth();
    authStore.purgeAuth();
    response.resolve(verifiedResponse());
    await verification;

    expect(authStore.isAuthenticated).toBe(false);
    expect(authStore.user).toBeNull();
  });
});
