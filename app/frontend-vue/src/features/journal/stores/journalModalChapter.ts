import { ref, watch } from "vue";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { JournalPrefixDto } from "@/features/journal/stores/journal";
import type { JournalChapterRegistModel } from "@/features/journal/stores/journalModal.types";

export interface JournalModalChapterDeps {
  personalPrefixOptionsStore: {
    optionsFor: (contentType: string) => unknown[];
    hasFailed: (contentType: string) => boolean;
    fetchOptions: (contentType: string, force?: boolean) => Promise<boolean>;
  };
}

/**
 * 저널 챕터 등록/수정 모달 surface.
 * 유형별 개인 Prefix 선택지 워밍을 포함한다.
 */
export function createJournalModalChapter(deps: JournalModalChapterDeps) {
  const { personalPrefixOptionsStore } = deps;

  /** 챕터 등록/수정 모달 오픈 여부 */
  const chapterRegistOpen = ref(false);
  /** 챕터 등록/수정 폼 모델 */
  const chapterRegistModel = ref<JournalChapterRegistModel | null>(null);

  /**
   * 챕터 유형을 챕터 말머리 목록의 개인 content_type 으로 변환한다.
   * 챕터의 attachable 정체성은 JOURNAL_CHAPTER 로 불변이지만, 말머리 목록은 일기 챕터
   * (JOURNAL_CHAPTER_DIARY)와 노트 챕터(JOURNAL_CHAPTER_NOTE)가 각각 사용자 정의로 분리된다
   * (백엔드 JournalChapterService.resolveChapterPrefixScopeContentType 과 동일 계약).
   * DREAM 등 사용자 말머리가 없는 유형은 null 을 반환한다.
   * @param chapterType 챕터 유형(DIARY | NOTE | ...)
   * @return 말머리 목록 content_type 또는 null
   */
  function resolveChapterPrefixContentType(chapterType: string | null | undefined): string | null {
    if (chapterType === "DIARY") return "JOURNAL_CHAPTER_DIARY";
    if (chapterType === "NOTE") return "JOURNAL_CHAPTER_NOTE";
    return null;
  }

  /**
   * 지정한 챕터 유형 범위의 활성 개인 말머리 선택지를 반환한다.
   * 변경 전: DIARY·NOTE 가 JOURNAL_CHAPTER 한 목록을 공유했다.
   * 변경 후: 챕터 유형별 목록을 읽고, 사용자 말머리가 없는 유형(DREAM 등)은 빈 목록을 반환한다.
   * @param chapterType 챕터 유형(생략·미대응 유형이면 빈 목록)
   */
  function chapterPrefixOptionsFor(chapterType?: string | null): JournalPrefixDto[] {
    const contentType = resolveChapterPrefixContentType(chapterType);
    if (!contentType) return [];
    return personalPrefixOptionsStore.optionsFor(contentType) as JournalPrefixDto[];
  }

  /**
   * 지정한 챕터 유형 말머리 선택지 조회 실패 여부.
   * @param chapterType 챕터 유형(미대응 유형이면 false)
   */
  function chapterPrefixLoadFailedFor(chapterType?: string | null): boolean {
    const contentType = resolveChapterPrefixContentType(chapterType);
    return contentType ? personalPrefixOptionsStore.hasFailed(contentType) : false;
  }

  /**
   * 챕터 개인 말머리 옵션을 조회한다.
   * 화면 마운트 시점에도 호출하여 콘텐츠 타입 공통 캐시에 넣어 두면 모달 오픈 시 로딩 없이 사용 가능하다.
   * 관리 화면 변경으로 무효화된 경우 다음 호출이 서버 확정 목록을 다시 조회한다.
   * @param chapterType 조회할 챕터 유형. 생략하면 사용자 말머리를 갖는 모든 유형(DIARY·NOTE)을 워밍한다.
   */
  async function prefetchChapterPrefixes(chapterType?: string | null): Promise<void> {
    if (chapterType == null) {
      await Promise.all([
        personalPrefixOptionsStore.fetchOptions("JOURNAL_CHAPTER_DIARY"),
        personalPrefixOptionsStore.fetchOptions("JOURNAL_CHAPTER_NOTE"),
      ]);
      return;
    }
    const contentType = resolveChapterPrefixContentType(chapterType);
    if (!contentType) return;
    await personalPrefixOptionsStore.fetchOptions(contentType);
  }

  /** 등록/수정 폼에서 챕터 유형을 바꾸면 해당 유형의 말머리 목록을 미리 적재한다. */
  watch(
    () => chapterRegistModel.value?.chapterType,
    (chapterType) => {
      if (!chapterRegistOpen.value) return;
      void prefetchChapterPrefixes(chapterType);
    },
  );

  /**
   * 챕터 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 챕터 데이터, 신규 시 journalDayId 등 초기값
   */
  async function openChapterRegist(payload?: JournalChapterRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    chapterRegistModel.value = {
      chapterType: "DIARY",
      prefixId: null,
      summaryYn: "N",
      title: "",
      ...payload,
    };
    chapterRegistOpen.value = true;
    void prefetchChapterPrefixes();
  }

  /** 챕터 등록/수정 모달을 닫는다. */
  function closeChapterRegist() {
    chapterRegistOpen.value = false;
  }

  return {
    chapterRegistOpen,
    chapterRegistModel,
    chapterPrefixOptionsFor,
    chapterPrefixLoadFailedFor,
    prefetchChapterPrefixes,
    openChapterRegist,
    closeChapterRegist,
  };
}
