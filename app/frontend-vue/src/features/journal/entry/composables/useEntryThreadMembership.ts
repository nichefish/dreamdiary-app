/**
 * useEntryThreadMembership.ts
 * 엔트리의 스레드 소속(멤버십) 관련 로직을 캡슐화한 composable.
 *
 * JournalEntryItem.vue 의 「스레드에 추가」 서브메뉴 후보 조회·검색·필터,
 * 소속 토글, 새 스레드 생성+소속, 소속 스레드 칩 표시, 스레드 상세 열기를 담당한다.
 *
 * 컴포넌트가 제공하는 guardAxisWrite · scrollAfterFetch 콜백에 의존해
 * 축 잠금 판정과 화면 갱신 트리거를 위임한다.
 */
import { computed, onBeforeUnmount, type ComputedRef } from "vue";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { swalFire } from "@/shared/utils/swal";
import {
  useJournalThreadMembershipStore,
  type ThreadOption,
} from "@/features/journal/stores/journalThreadMembership";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import type { JournalEntryDto } from "@/features/journal/stores/journal";

export interface UseEntryThreadMembershipOptions {
  /** 현재 엔트리 reactive 참조 (props.entry) */
  entry: ComputedRef<JournalEntryDto>;
  /** 축 쓰기 잠금 guard. false 반환 시 동작 중단. */
  guardAxisWrite: () => boolean;
  /** 액션 성공 후 화면 재조회+스크롤 트리거 */
  scrollAfterFetch: (stdrdDt?: string, opts?: { scroll?: boolean }) => void;
  /** i18n t 함수 */
  t: (key: string) => string;
}

/**
 * 엔트리 ↔ 스레드 소속(멤버십) 서브메뉴 전체 로직.
 *
 * 반환값은 템플릿에서 직접 바인딩하거나 컨텍스트 메뉴 핸들러로 사용된다.
 */
export function useEntryThreadMembership(options: UseEntryThreadMembershipOptions) {
  const { entry, guardAxisWrite, scrollAfterFetch, t } = options;

  const membershipStore = useJournalThreadMembershipStore();
  const threadStore = useJournalThreadStore();

  // ──────────────────────────────────────────────
  // computed
  // ──────────────────────────────────────────────

  /** 이 엔트리가 속한 스레드 목록. */
  const entryThreadList = computed(() => entry.value.threadList ?? []);

  /** 검색·말머리가 적용 중인지 여부. 정상 빈 목록의 안내 문구를 구분한다. */
  const hasThreadCandidateFilter = computed(() =>
    membershipStore.optionKeyword.trim() !== "" || membershipStore.optionPrefix !== "",
  );

  /** 후보 말머리 선택지를 표시한다. */
  const membershipPrefixItems = computed(() => membershipStore.prefixOptions);

  /**
   * 표시용 스레드 후보 목록.
   * 설계 §2-6: 연관 스레드에서 빌려온 엔트리(sourceThreadId 존재)는 멤버십 제거("스레드에서 빼기")를 숨긴다.
   */
  const filteredThreadOptions = computed(() => {
    const opts = membershipStore.threadOptions;
    if (!entry.value.sourceThreadId) return opts;
    return opts.filter((opt) => !opt.member);
  });

  // ──────────────────────────────────────────────
  // debounce timer
  // ──────────────────────────────────────────────

  /** 제목 입력마다 API를 호출하지 않도록 마지막 입력 뒤 250ms에 조회한다. */
  let threadCandidateSearchTimer: ReturnType<typeof setTimeout> | undefined;

  onBeforeUnmount(() => {
    if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
  });

  // ──────────────────────────────────────────────
  // actions
  // ──────────────────────────────────────────────

  /** 서브메뉴 진입 시 현재 엔트리 기준 후보와 분류를 조회한다. */
  function ensureThreadOptions(): void {
    if (!entry.value.id) return;
    void membershipStore.openThreadOptions(entry.value.id);
  }

  /** 제목 검색을 debounce하여 현재 엔트리 후보를 갱신한다. */
  function scheduleThreadCandidateSearch(): void {
    if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
    const entryId = entry.value.id;
    if (!entryId) return;
    threadCandidateSearchTimer = setTimeout(() => {
      if (membershipStore.candidateEntryId === entryId) {
        void membershipStore.fetchThreadOptions(entryId);
      }
    }, 250);
  }

  /** 분류 변경 시 현재 엔트리 후보를 즉시 갱신한다. */
  function refreshThreadCandidates(): void {
    if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
    const entryId = entry.value.id;
    if (!entryId || membershipStore.candidateEntryId !== entryId) return;
    void membershipStore.fetchThreadOptions(entryId);
  }

  /** 후보 스레드 라이프사이클 표시 라벨. */
  function threadLifecycleLabel(lifecycleKey: string): string {
    if (lifecycleKey === "PENDING") return t("lifecycle.pending");
    if (lifecycleKey === "RESOLVED") return t("status.completed");
    return t("journal.entry.lifecycle.open");
  }

  /** 후보 스레드 말머리 이름. */
  function threadPrefixName(option: ThreadOption): string {
    return option.prefix?.name ?? "";
  }

  /** 스레드 소속 토글: 속해 있으면 제외, 아니면 추가. 성공 시 목록 갱신. */
  async function toggleThread(option: ThreadOption): Promise<void> {
    if (!guardAxisWrite()) return;
    if (!entry.value.id) return;
    // 빌려온 엔트리의 멤버십 제거 시도 거부 (설계 §2-6)
    if (entry.value.sourceThreadId && option.member) return;
    const entryId = entry.value.id;
    const ok = option.member
      ? await membershipStore.removeFromThread(option.id, entryId)
      : await membershipStore.addToThread(option.id, entryId);
    if (ok) {
      if (membershipStore.candidateEntryId === entryId) {
        await membershipStore.fetchThreadOptions(entryId);
      }
      void threadStore.refreshPeriodSummary();
      scrollAfterFetch(undefined, { scroll: false });
    }
  }

  /**
   * 말머리·제목을 받아 새 스레드를 만들고 이 엔트리를 소속시킨다.
   * 서브메뉴 말머리 필터가 있으면 생성 폼의 기본값으로 쓴다.
   */
  async function startNewThread(): Promise<void> {
    if (!guardAxisWrite()) return;
    if (!entry.value.id) return;
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
        `<select id="swal-thread-prefix" class="form-select form-select-sm mb-3">`,
        `<option value="">${escapeHtmlAttr(t("journal.thread.prefix.select"))}</option>`,
        prefixOptionsHtml,
        `</select>`,
        `<label class="form-label fs-7 mb-1">${escapeHtmlAttr(t("journal.entry.thread.new.prompt"))}</label>`,
        `<input id="swal-thread-title" class="form-control form-control-sm" maxlength="200"`,
        ` placeholder="${escapeHtmlAttr(t("journal.entry.thread.new.placeholder"))}" />`,
        `</div>`,
      ].join(""),
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: t("common.save"),
      cancelButtonText: t("common.cancel"),
      didOpen: () => {
        const titleEl = document.getElementById("swal-thread-title") as HTMLInputElement | null;
        titleEl?.focus();
      },
      preConfirm: () => {
        const titleEl = document.getElementById("swal-thread-title") as HTMLInputElement | null;
        const prefixEl = document.getElementById("swal-thread-prefix") as HTMLSelectElement | null;
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
    const entryId = entry.value.id!;
    const ok = await membershipStore.createThreadAndAdd(created.title, entryId, created.prefixId);
    if (ok) {
      if (membershipStore.candidateEntryId === entryId) {
        await membershipStore.fetchThreadOptions(entryId);
      }
      void threadStore.refreshPeriodSummary();
      scrollAfterFetch(undefined, { scroll: false });
    }
  }

  /**
   * 현재 저널 화면을 유지한 채 전역 스레드 상세 모달을 연다.
   * 스레드 목록의 route 기반 상세 진입은 딥링크 계약으로 별도 유지한다.
   */
  function openThreadDetail(threadId: number): void {
    void threadStore.openDetail(threadId);
  }

  // ──────────────────────────────────────────────
  // internal helpers
  // ──────────────────────────────────────────────

  /** Swal HTML 옵션·라벨용 최소 이스케이프 */
  function escapeHtmlAttr(value: string): string {
    return value
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  return {
    // state (store proxy — 템플릿 바인딩용)
    membershipStore,
    /** threadStore 인스턴스. scrollAfterFetch 등 컴포넌트 내 다른 액션이 사용한다. */
    threadStore,
    // computed
    entryThreadList,
    hasThreadCandidateFilter,
    membershipPrefixItems,
    filteredThreadOptions,
    // actions
    ensureThreadOptions,
    scheduleThreadCandidateSearch,
    refreshThreadCandidates,
    threadLifecycleLabel,
    threadPrefixName,
    toggleThread,
    startNewThread,
    openThreadDetail,
  };
}
