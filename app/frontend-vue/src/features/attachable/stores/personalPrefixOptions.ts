import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

/** 개인 Prefix 소비 화면이 공유하는 활성 선택지. */
export interface PersonalPrefixOption {
  id: number;
  name: string;
  color?: string | null;
  sortOrder?: number;
  activeYn?: "Y" | "N";
}

/** 활성 개인 Prefix 선택지의 콘텐츠 타입별 세션 캐시. */
export const usePersonalPrefixOptionsStore = defineStore("personalPrefixOptions", () => {
  const optionsByContentType = ref<Record<string, PersonalPrefixOption[]>>({});
  const loadedByContentType = ref<Record<string, boolean>>({});
  const loadingByContentType = ref<Record<string, boolean>>({});
  const failedByContentType = ref<Record<string, boolean>>({});

  /** 콘텐츠 타입 하나의 무효화 이후 늦게 도착한 응답을 폐기하기 위한 버전. */
  const contentTypeVersions = new Map<string, number>();
  /** 로그아웃·사용자 전환 이후 이전 사용자 응답을 폐기하기 위한 세대. */
  let cacheGeneration = 0;
  /** 같은 콘텐츠 타입의 동시 조회를 한 요청으로 합친다. */
  const requests = new Map<string, Promise<boolean>>();

  function optionsFor(contentType: string): PersonalPrefixOption[] {
    return optionsByContentType.value[contentType] ?? [];
  }

  function isLoading(contentType: string): boolean {
    return loadingByContentType.value[contentType] === true;
  }

  function hasFailed(contentType: string): boolean {
    return failedByContentType.value[contentType] === true;
  }

  /**
   * 관리 쓰기가 성공한 콘텐츠 타입의 활성 선택지를 폐기한다.
   * 다음 소비 화면 진입이 서버 확정 목록을 다시 조회하게 하며, 진행 중이던 이전 응답도 무효화한다.
   */
  function invalidate(contentType: string): void {
    const nextVersion = (contentTypeVersions.get(contentType) ?? 0) + 1;
    contentTypeVersions.set(contentType, nextVersion);
    requests.delete(contentType);
    delete optionsByContentType.value[contentType];
    delete loadedByContentType.value[contentType];
    delete loadingByContentType.value[contentType];
    delete failedByContentType.value[contentType];
    console.info("[personalPrefixOptions] 콘텐츠 타입 캐시 무효화", {
      contentType,
      version: nextVersion,
    });
  }

  /** 로그아웃·세션 만료 시 모든 개인 Prefix 선택지와 진행 중 요청의 결과를 폐기한다. */
  function resetAll(): void {
    cacheGeneration += 1;
    optionsByContentType.value = {};
    loadedByContentType.value = {};
    loadingByContentType.value = {};
    failedByContentType.value = {};
    requests.clear();
    contentTypeVersions.clear();
    console.info("[personalPrefixOptions] 사용자 세션 캐시 초기화", { cacheGeneration });
  }

  /**
   * 콘텐츠 타입별 활성 개인 Prefix를 조회한다.
   * 정상 빈 목록도 캐시하고, 실패한 요청은 완료로 기억하지 않아 다음 진입에서 재시도한다.
   *
   * @param contentType Prefix Scope 콘텐츠 타입
   * @param force 기존 캐시와 진행 중 요청을 무효화하고 새로 조회할지 여부
   * @return 현재 세대의 정상 응답을 반영했으면 true
   */
  async function fetchOptions(contentType: string, force = false): Promise<boolean> {
    if (!contentType) {
      console.error("[personalPrefixOptions] 조회 콘텐츠 타입 누락");
      return false;
    }
    if (force) invalidate(contentType);
    if (loadedByContentType.value[contentType]) return true;

    const inFlight = requests.get(contentType);
    if (inFlight) return inFlight;

    const requestGeneration = cacheGeneration;
    const requestVersion = contentTypeVersions.get(contentType) ?? 0;
    loadingByContentType.value[contentType] = true;
    failedByContentType.value[contentType] = false;

    let request!: Promise<boolean>;
    request = axios.get("/api/my/prefixes/options", { params: { contentType } })
      .then((response) => {
        if (
          requestGeneration !== cacheGeneration
          || requestVersion !== (contentTypeVersions.get(contentType) ?? 0)
        ) {
          console.info("[personalPrefixOptions] 무효화된 선택지 응답 폐기", {
            contentType,
            requestGeneration,
            activeGeneration: cacheGeneration,
            requestVersion,
            activeVersion: contentTypeVersions.get(contentType) ?? 0,
          });
          return false;
        }
        optionsByContentType.value[contentType] = Array.isArray(response.data?.rsltList)
          ? response.data.rsltList
          : [];
        loadedByContentType.value[contentType] = true;
        failedByContentType.value[contentType] = false;
        return true;
      })
      .catch((error) => {
        if (
          requestGeneration !== cacheGeneration
          || requestVersion !== (contentTypeVersions.get(contentType) ?? 0)
        ) {
          console.info("[personalPrefixOptions] 무효화된 선택지 실패 응답 폐기", { contentType });
          return false;
        }
        console.error("[personalPrefixOptions] 활성 선택지 조회 실패", { contentType }, error);
        delete optionsByContentType.value[contentType];
        loadedByContentType.value[contentType] = false;
        failedByContentType.value[contentType] = true;
        return false;
      })
      .finally(() => {
        if (requests.get(contentType) !== request) return;
        requests.delete(contentType);
        loadingByContentType.value[contentType] = false;
      });
    requests.set(contentType, request);
    return request;
  }

  return {
    optionsFor,
    isLoading,
    hasFailed,
    fetchOptions,
    invalidate,
    resetAll,
  };
});
