/**
 * journalThreadMembership.ts
 * 저널 엔트리 ↔ 스레드 소속 지정 액션 스토어.
 *
 * FLOW 를 대체하는 축이다. 엔트리 ⋯ 메뉴의 「스레드에 추가」 서브메뉴와
 * 본문 소속 칩이 이 스토어의 액션을 호출한다.
 *
 * 스레드 목록 페이지 상태(journalThread.ts)와 분리한다 — 엔트리 메뉴가
 * 목록 페이지 상태에 의존하면 안 되기 때문이다.
 */
import { defineStore } from "pinia";
import { computed, ref } from "vue";
import axios from "axios";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAjaxResult, swalRequestError } from "@/shared/utils/swal";
import type { ThreadPrefix } from "@/features/journal/stores/journalThread";
import { usePersonalPrefixOptionsStore } from "@/features/attachable/stores/personalPrefixOptions";

/** 서브메뉴에 띄우는 스레드 후보 항목 (경량) */
export interface ThreadOption {
  id: number;
  title?: string;
  prefix?: ThreadPrefix | null;
  /** OPEN/PENDING/RESOLVED. 미설정은 서버가 OPEN 으로 내린다. */
  lifecycleKey?: string;
  membershipCount: number;
  lastMembershipAt?: string;
  member: boolean;
}

/** 스레드 후보 말머리 선택지 */
export const useJournalThreadMembershipStore = defineStore("journalThreadMembership", () => {
  const { t } = useLocaleStore();
  const personalPrefixOptionsStore = usePersonalPrefixOptionsStore();

  /**
   * 서브메뉴에 띄울 스레드 후보 목록.
   *
   * 변경 전: 일반 스레드 목록의 첫 7개를 모든 엔트리 메뉴가 공유했다.
   * 변경 후: 후보 API가 현재 엔트리 소속·최근 사용·소속 수를 반영해 정렬한 결과를
   * 엔트리별로 조회하고, 제목·말머리 필터를 서버에 전달한다.
   */
  const threadOptions = ref<ThreadOption[]>([]);
  const optionsLoading = ref(false);
  /** 후보 조회 실패. 정상 0건과 구분하기 위해 별도 상태로 둔다. */
  const optionsError = ref("");
  /** 현재 후보 목록이 귀속된 엔트리 ID. 전역 Pinia 상태의 엔트리 간 혼용을 막는다. */
  const candidateEntryId = ref<number | null>(null);
  /** 후보 제목 검색어 */
  const optionKeyword = ref("");
  /** 후보 말머리 필터 */
  const optionPrefix = ref("");
  /** 완료(RESOLVED) 스레드 후보 포함 여부. 기본 false. */
  const optionIncludeResolved = ref(false);
  /** 스레드 목록·편집과 같은 JOURNAL_THREAD 활성 개인 Prefix 선택지. */
  const prefixOptions = computed<ThreadPrefix[]>(() =>
    personalPrefixOptionsStore.optionsFor("JOURNAL_THREAD") as ThreadPrefix[]
  );
  const prefixesLoading = computed(() => personalPrefixOptionsStore.isLoading("JOURNAL_THREAD"));
  const prefixError = computed(() => personalPrefixOptionsStore.hasFailed("JOURNAL_THREAD")
    ? t("journal.thread.prefix.load.failure")
    : "");

  /** 서브메뉴에 노출할 스레드 후보 최대 개수 */
  const OPTION_LIMIT = 7;
  /** 늦게 끝난 이전 엔트리 요청이 현재 후보를 덮어쓰지 못하게 하는 순번. */
  let candidateRequestSequence = 0;

  /**
   * 현재 엔트리에 맞는 스레드 후보 목록을 조회한다.
   *
   * 같은 엔트리의 검색 실패 시에는 직전 성공 목록을 보존한다. 다른 엔트리로 전환할
   * 때는 {@link openThreadOptions}가 기존 목록을 먼저 비워 잘못된 소속 표시를 막는다.
   *
   * @param entryId 후보 소속 여부를 판정할 현재 엔트리 ID
   * @returns 조회 성공 여부
   */
  async function fetchThreadOptions(entryId: number): Promise<boolean> {
    const requestSequence = ++candidateRequestSequence;
    candidateEntryId.value = entryId;
    optionsLoading.value = true;
    optionsError.value = "";
    try {
      const keyword = optionKeyword.value.trim();
      const prefixId = optionPrefix.value;
      const res = await axios.get("/api/journal/threads/candidates", {
        params: {
          entryId,
          limit: OPTION_LIMIT,
          ...(keyword ? { keyword } : {}),
          ...(prefixId ? { prefixId } : {}),
          ...(optionIncludeResolved.value ? { includeResolved: true } : {}),
        },
      });
      if (requestSequence !== candidateRequestSequence) {
        console.debug("[journalThreadMembership] stale candidate response ignored", {
          entryId,
          requestSequence,
          activeRequestSequence: candidateRequestSequence,
        });
        return false;
      }
      threadOptions.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
      return true;
    } catch (e: unknown) {
      if (requestSequence !== candidateRequestSequence) {
        console.debug("[journalThreadMembership] stale candidate error ignored", {
          entryId,
          requestSequence,
          activeRequestSequence: candidateRequestSequence,
        });
        return false;
      }
      console.error("[journalThreadMembership] fetchThreadOptions failed", {
        entryId,
        keyword: optionKeyword.value.trim(),
        prefixId: optionPrefix.value,
        includeResolved: optionIncludeResolved.value,
      }, e);
      optionsError.value = t("journal.entry.thread.candidates.load.failure");
      return false;
    } finally {
      if (requestSequence === candidateRequestSequence) {
        optionsLoading.value = false;
      }
    }
  }

  /**
   * 스레드 말머리 선택지를 콘텐츠 타입 공통 캐시에서 조회한다.
   *
   * 개인 말머리 목록은 콘텐츠 타입별로 분리되므로 스레드 타입을 명시한다.
   * 관리 화면 변경으로 캐시가 무효화되면 다음 메뉴 진입에서 최신 목록을 다시 조회한다.
   * 실패 상태는 후보 조회 실패와 분리하며 다음 메뉴 진입에서 재시도한다.
   */
  async function fetchPrefixOptions(): Promise<boolean> {
    return personalPrefixOptionsStore.fetchOptions("JOURNAL_THREAD");
  }

  /**
   * 엔트리의 「스레드에 추가」 서브메뉴를 준비한다.
   *
   * 엔트리가 바뀌면 이전 엔트리의 검색 조건과 후보를 버리고 새 엔트리 기준으로
   * 다시 조회한다. 같은 엔트리를 재진입하면 현재 필터를 유지한 채 최신 랭킹을 받는다.
   *
   * @param entryId 메뉴를 연 엔트리 ID
   */
  async function openThreadOptions(entryId: number): Promise<void> {
    if (candidateEntryId.value !== entryId) {
      candidateEntryId.value = entryId;
      optionKeyword.value = "";
      optionPrefix.value = "";
      optionIncludeResolved.value = false;
      optionsError.value = "";
      threadOptions.value = [];
    }
    await Promise.all([
      fetchThreadOptions(entryId),
      fetchPrefixOptions(),
    ]);
  }

  /**
   * 엔트리를 스레드에 추가한다. (멱등 — 서버가 이미 소속이면 변경 없이 성공)
   *
   * @param threadId 스레드 ID
   * @param entryId 엔트리 ID
   * @returns 처리 성공 여부
   */
  async function addToThread(threadId: number, entryId: number): Promise<boolean> {
    try {
      const fd = new FormData();
      fd.append("entryId", String(entryId));
      const res = await axios.post(`/api/journal/threads/${threadId}/entries`, fd);
      const rslt = res.data?.rslt === true;
      if (!rslt) {
        void swalAjaxResult({ rslt: false, message: res.data?.message, failureFallback: t("common.result.failure") });
      }
      return rslt;
    } catch (e: unknown) {
      void swalRequestError(e);
      return false;
    }
  }

  /**
   * 엔트리를 스레드에서 제외한다. (멱등 — 이미 제외면 변경 없이 성공)
   *
   * @param threadId 스레드 ID
   * @param entryId 엔트리 ID
   * @returns 처리 성공 여부
   */
  async function removeFromThread(threadId: number, entryId: number): Promise<boolean> {
    try {
      const res = await axios.delete(`/api/journal/threads/${threadId}/entries/${entryId}`);
      const rslt = res.data?.rslt === true;
      if (!rslt) {
        void swalAjaxResult({ rslt: false, message: res.data?.message, failureFallback: t("common.result.failure") });
      }
      return rslt;
    } catch (e: unknown) {
      void swalRequestError(e);
      return false;
    }
  }

  /**
   * 제목·선택 말머리로 새 스레드를 만들고 엔트리를 바로 소속시킨다.
   *
   * 스레드 생성 API(멀티파트)를 재사용해 제목과 nullable prefixId 를 채워 만들고, 반환된 id 로 소속을 건다.
   * 본문·태그·첨부는 이후 스레드 상세에서 채우는 것을 전제로 비운다.
   *
   * @param title 스레드 제목 (비어 있지 않아야 함)
   * @param entryId 엔트리 ID
   * @param prefixId 스레드 말머리 ID (없으면 미선택)
   * @returns 처리 성공 여부
   */
  async function createThreadAndAdd(title: string, entryId: number, prefixId?: number | null): Promise<boolean> {
    try {
      const fd = new FormData();
      fd.append("contentType", "JOURNAL_THREAD");
      fd.append("title", title);
      fd.append("content", "");
      fd.append("tag.tagListStr", "");
      if (prefixId != null) fd.append("prefixId", String(prefixId));
      const res = await axios.post("/api/journal/threads", fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.data?.rslt !== true) {
        void swalAjaxResult({ rslt: false, message: res.data?.message, failureFallback: t("common.result.failure") });
        return false;
      }
      const newThreadId = res.data?.rsltObj?.id as number | undefined;
      if (newThreadId == null) {
        console.error("[journalThreadMembership] created thread id missing", res.data);
        void swalAjaxResult({ rslt: false, failureFallback: t("common.result.failure") });
        return false;
      }
      return await addToThread(newThreadId, entryId);
    } catch (e: unknown) {
      void swalRequestError(e);
      return false;
    }
  }

  return {
    threadOptions,
    optionsLoading,
    optionsError,
    candidateEntryId,
    optionKeyword,
    optionPrefix,
    optionIncludeResolved,
    prefixOptions,
    prefixesLoading,
    prefixError,
    fetchThreadOptions,
    fetchPrefixOptions,
    openThreadOptions,
    addToThread,
    removeFromThread,
    createThreadAndAdd,
  };
});
