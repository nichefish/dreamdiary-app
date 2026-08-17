import { computed, ref } from "vue";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { JournalEntryDto, JournalPrefixDto } from "@/features/journal/stores/journal";
import type {
  JournalChapterOption,
  JournalEntryRegistModel,
  JournalReflectionRegistModel,
} from "@/features/journal/stores/journalModal.types";
import {
  CATEGORY_MAP_URL_ENTRY_DREAM,
  resolveEntryCategoryMapUrl,
} from "@/features/journal/stores/journalCategoryMaps";
import { swalRequestError } from "@/shared/utils/swal";

export interface JournalModalEntryDeps {
  t: (key: string) => string;
  ensureCategoryMap: (url: string) => Promise<Record<string, string[]>>;
  mapForEntryContentType: (contentType: string | undefined) => Record<string, string[]> | null;
  personalPrefixOptionsStore: {
    optionsFor: (contentType: string) => unknown[];
    hasFailed: (contentType: string) => boolean;
    fetchOptions: (contentType: string, force?: boolean) => Promise<boolean>;
  };
  openReflectionRegist: (payload?: JournalReflectionRegistModel) => Promise<void>;
}

/**
 * 저널 엔트리 등록·수정·읽기 전용 모달 surface.
 * Reflection 수정 전환은 openReflectionRegist 의존성으로 위임한다.
 */
export function createJournalModalEntry(deps: JournalModalEntryDeps) {
  const { t, ensureCategoryMap, mapForEntryContentType, personalPrefixOptionsStore, openReflectionRegist } = deps;

  // ---- 엔트리(일기/꿈/노트) 등록/수정 모달 ----

  /** 엔트리 등록/수정 모달 오픈 여부 */
  const entryRegistOpen = ref(false);
  /** 엔트리 등록/수정 로딩 여부 */
  const entryRegistLoading = ref(false);
  /** 엔트리 등록/수정 폼 모델 */
  const entryRegistModel = ref<JournalEntryRegistModel | null>(null);
  /**
   * 엔트리 유형별 활성 개인 Prefix 선택지는 콘텐츠 타입 공통 캐시에서 읽는다.
   * 빈 목록 캐시·실패 재시도·동시 요청 공유·이전 사용자 응답 폐기도 공통 store가 담당한다.
   */
  /** 엔트리 모달용 뷰 (contentType 에 따라 diary/dream map 참조) */
  const entryCategoryMap = computed((): Record<string, string[]> | null => {
    return mapForEntryContentType(entryRegistModel.value?.contentType);
  });
  let dreamEntryRegistOpening = false;

  /** 엔트리 유형별 활성 Prefix 선택지를 반환한다. */
  function entryPrefixOptionsFor(contentType?: string): JournalPrefixDto[] {
    if (!contentType) return [];
    return personalPrefixOptionsStore.optionsFor(contentType) as JournalPrefixDto[];
  }

  /** 엔트리 유형별 Prefix 조회 실패 여부를 반환한다. */
  function entryPrefixLoadFailedFor(contentType?: string): boolean {
    if (!contentType) return false;
    return personalPrefixOptionsStore.hasFailed(contentType);
  }

  /**
   * 엔트리 유형별 개인 Prefix 선택지를 조회한다.
   * 빈 목록도 정상 결과로 캐시하고 실패한 요청만 다음 모달 진입에서 재시도한다.
   */
  async function prefetchEntryPrefixes(contentType: string): Promise<void> {
    await personalPrefixOptionsStore.fetchOptions(contentType);
  }

  /**
   * 엔트리 신규 등록 모달을 연다. (DIARY/NOTE: 챕터 목록을 caller 가 전달)
   * categoryMap 과 챕터 옵션을 병렬로 조회한 뒤 모달 로딩을 해제한다.
   * @param payload - contentType, journalDayId, stdrdDt 등 초기값
   */
  async function openEntryRegist(payload: JournalEntryRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    entryRegistOpen.value = true;
    entryRegistLoading.value = true;
    entryRegistModel.value = null;
    try {
      const model: JournalEntryRegistModel = {
        prefixId: null,
        title: "",
        content: "",
        tag: { tagListStrWithCtgr: "" },
        chapterList: [],
        ...payload,
      };
      const showTagForType = isDiaryLikeEntry(payload.contentType) || isDreamEntry(payload.contentType);
      const categoryUrl = resolveEntryCategoryMapUrl(payload.contentType);
      await Promise.all([
        showTagForType && categoryUrl ? ensureCategoryMap(categoryUrl) : Promise.resolve(),
        hydrateEntryPrefixContext(model),
      ]);
      entryRegistModel.value = model;
    } finally {
      entryRegistLoading.value = false;
    }
  }

  /**
   * 꿈 엔트리 신규 등록 모달을 연다. dream-auto API 로 챕터를 자동 생성/조회한다.
   * @param params - journalDayId, stdrdDt, dreamerName
   */
  async function openDreamEntryRegist(params: {
    journalDayId: number;
    stdrdDt: string;
    dreamerName?: string;
  }) {
    if (dreamEntryRegistOpening) return;
    if (!await assertAuthenticatedBeforeModal()) return;
    dreamEntryRegistOpening = true;
    entryRegistOpen.value = true;
    entryRegistLoading.value = true;
    entryRegistModel.value = null;
    try {
      const fd = new FormData();
      fd.append("journalDayId", String(params.journalDayId));
      const [res] = await Promise.all([
        axios.post("/api/journal/chapters/dream-auto", fd, { headers: { "Content-Type": "multipart/form-data" } }),
        ensureCategoryMap(CATEGORY_MAP_URL_ENTRY_DREAM),
        prefetchEntryPrefixes("JOURNAL_DREAM"),
      ]);
      const chapter = res.data?.rsltObj;
      entryRegistModel.value = {
        contentType: "JOURNAL_DREAM",
        prefixContentType: "JOURNAL_DREAM",
        prefixId: null,
        journalDayId: params.journalDayId,
        journalChapterId: chapter?.id ?? "",
        stdrdDt: params.stdrdDt,
        dreamerName: params.dreamerName?.trim() ?? "",
        title: "",
        content: "",
        tag: { tagListStrWithCtgr: "" },
        chapterList: [],
      };
    } catch (e: unknown) {
      console.error("[journalModal] openDreamEntryRegist failed", { journalDayId: params.journalDayId }, e);
      entryRegistModel.value = null;
      entryRegistOpen.value = false;
      void swalRequestError(e, t("journal.entry.dream-regist.load.failure"));
    } finally {
      entryRegistLoading.value = false;
      dreamEntryRegistOpening = false;
    }
  }

  /**
   * 엔트리 수정 모달을 연다. API 에서 기존 데이터를 조회한다.
   * @param id - 엔트리 ID
   */
  async function openEntryModify(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    entryRegistLoading.value = true;
    entryRegistModel.value = null;
    try {
      const res = await axios.get(`/api/journal/entry/${id}`);
      const entry = res.data?.rsltObj;
      if (!entry) {
        entryRegistOpen.value = false;
        return;
      }
      /* JOURNAL_REFLECTION 수정은 태그·제목 계약을 가진 Reflection 전용 모달로 보낸다. */
      if (entry.contentType === "JOURNAL_REFLECTION") {
        entryRegistOpen.value = false;
        await openReflectionRegist({
          id,
          refId: entry.refId,
          refContentType: entry.refContentType,
          journalDayId: entry.journalDayId,
          journalChapterId: entry.journalChapterId,
        });
        return;
      }
      entryRegistOpen.value = true;
      const merged: JournalEntryRegistModel = {
        ...entry,
        tag: { tagListStrWithCtgr: entry.tag?.tagListStrWithCtgr ?? "" },
        chapterList: entry.chapterList ?? [],
      };
      const showTagForType = isDiaryLikeEntry(merged.contentType) || isDreamEntry(merged.contentType);
      const categoryUrl = resolveEntryCategoryMapUrl(merged.contentType);
      await Promise.all([
        showTagForType && categoryUrl ? ensureCategoryMap(categoryUrl) : Promise.resolve(),
        hydrateEntryPrefixContext(merged),
      ]);
      entryRegistModel.value = merged;
    } catch (e: unknown) {
      console.error("[journalModal] openEntryModify failed", { entryId: id }, e);
      entryRegistModel.value = null;
      entryRegistOpen.value = false;
      void swalRequestError(e, t("journal.entry.modify.load.failure"));
    } finally {
      entryRegistLoading.value = false;
    }
  }

  function shouldLoadChapterOptions(model: JournalEntryRegistModel | null): model is JournalEntryRegistModel {
    if (!model?.journalDayId) return false;
    return isDiaryLikeEntry(model.contentType) || isNoteLikeEntry(model.contentType);
  }

  function isDiaryLikeEntry(contentType: string): boolean {
    return contentType === "JOURNAL_DIARY" || contentType === "DIARY";
  }

  function isNoteLikeEntry(contentType: string): boolean {
    return contentType === "JOURNAL_NOTE" || contentType === "NOTE";
  }

  function isDreamEntry(contentType: string): boolean {
    return contentType === "JOURNAL_DREAM" || contentType === "DREAM";
  }

  /**
   * 엔트리 챕터 선택 목록에 쓸 chapterType (신규·수정 공통: 노트끼리·일기끼리).
   * 변경 전: contentType 만으로 NOTE/DIARY 분기 → NOTE 챕터의 JOURNAL_DIARY 엔트리가 DIARY 목록으로 잘못 필터됨.
   * 변경 후: journalChapterId 가 NOTE 챕터면 NOTE 목록 유지 (백엔드 JournalEntryTypeResolver 와 동일).
   */
  function resolveExpectedChapterTypeForEntry(
    contentType: string,
    journalChapterId: number | string | undefined,
    rawList: Record<string, unknown>[],
  ): "NOTE" | "DIARY" | "DREAM" {
    if (isDreamEntry(contentType)) return "DREAM";
    if (isNoteLikeEntry(contentType)) return "NOTE";
    if (journalChapterId !== undefined && journalChapterId !== null && String(journalChapterId) !== "") {
      const current = rawList.find((raw) => String(raw.id) === String(journalChapterId));
      const chapterType = String(current?.chapterType ?? "");
      if (chapterType === "NOTE") return "NOTE";
      if (chapterType === "DREAM") return "DREAM";
    }
    return "DIARY";
  }

  function normalizeChapterOptions(
    rawList: unknown,
    contentType: string,
    journalChapterId?: number | string,
  ): JournalChapterOption[] {
    if (!Array.isArray(rawList)) return [];
    const rawRecords = rawList.map((raw) => raw as Record<string, unknown>);
    const expectedChapterType = resolveExpectedChapterTypeForEntry(contentType, journalChapterId, rawRecords);
    return rawRecords
      .filter((raw) => {
        const chapterType = String(raw.chapterType ?? "");
        return chapterType === "" || chapterType === expectedChapterType;
      })
      .map((raw) => ({
        id: raw.id as number | string,
        title: String(raw.title ?? ""),
        sortOrder: raw.sortOrder as number | undefined,
        prefix: (raw.prefix as JournalPrefixDto | null | undefined) ?? null,
        prefixId: raw.prefixId == null ? null : Number(raw.prefixId),
        summaryYn: String(raw.summaryYn ?? "N"),
        chapterType: String(raw.chapterType ?? ""),
      }))
      .filter((chapter) => chapter.id !== undefined && chapter.id !== null && String(chapter.id) !== "");
  }

  async function hydrateEntryChapterOptions(model: JournalEntryRegistModel | null): Promise<void> {
    if (!shouldLoadChapterOptions(model)) return;
    try {
      const res = await axios.get(`/api/journal/day/${model.journalDayId}`);
      const day = res.data?.rsltObj ?? {};
      const rawChapterList = Array.isArray(day.chapterList) && day.chapterList.length > 0
        ? day.chapterList
        : day.journalChapterList;
      const options = normalizeChapterOptions(rawChapterList, model.contentType, model.journalChapterId);
      if (options.length === 0) return;
      model.chapterList = options;
      const chapterStillValid = model.journalChapterId
        && options.some((chapter) => String(chapter.id) === String(model.journalChapterId));
      if (!chapterStillValid) {
        console.warn(
          "[journalModal] 엔트리 챕터 ID가 선택 목록에 없어 첫 챕터로 보정",
          { journalChapterId: model.journalChapterId, contentType: model.contentType, fallbackChapterId: options[0].id },
        );
        model.journalChapterId = options[0].id;
      }
    } catch {
      console.error("[journalModal] 엔트리 챕터 옵션 조회 실패", model.journalDayId);
    }
  }

  /**
   * 엔트리가 사용할 개인 Prefix 목록 키를 소속 챕터 유형으로 결정한다.
   * NOTE 엔트리는 영속 contentType이 JOURNAL_DIARY여도 JOURNAL_NOTE를 반환한다.
   */
  function resolveEntryPrefixContentType(
    model: JournalEntryRegistModel,
  ): "JOURNAL_DIARY" | "JOURNAL_DREAM" | "JOURNAL_NOTE" {
    if (model.prefixContentType) return model.prefixContentType;
    if (isDreamEntry(model.contentType)) return "JOURNAL_DREAM";
    if (isNoteLikeEntry(model.contentType)) return "JOURNAL_NOTE";
    const chapter = model.chapterList?.find(
      (option) => String(option.id) === String(model.journalChapterId ?? ""),
    );
    if (chapter?.chapterType === "NOTE") return "JOURNAL_NOTE";
    if (chapter?.chapterType === "DREAM") return "JOURNAL_DREAM";
    return "JOURNAL_DIARY";
  }

  /** 챕터 선택지와 그 챕터 유형의 개인 Prefix 선택지를 순서대로 적재한다. */
  async function hydrateEntryPrefixContext(model: JournalEntryRegistModel): Promise<void> {
    await hydrateEntryChapterOptions(model);
    const contentType = resolveEntryPrefixContentType(model);
    model.prefixContentType = contentType;
    await prefetchEntryPrefixes(contentType);
  }

  /** 엔트리 등록/수정 모달을 닫는다. */
  function closeEntryRegist() {
    entryRegistOpen.value = false;
    entryRegistModel.value = null;
  }

  // ---- 엔트리 읽기 전용 뷰 모달 (채팅 RAG 원문 등) ----

  /** 엔트리 읽기 전용 모달 오픈 여부 */
  const entryViewOpen = ref(false);
  /** 엔트리 읽기 전용 로딩 여부 */
  const entryViewLoading = ref(false);
  /** 엔트리 읽기 전용 표시 모델 */
  const entryViewModel = ref<JournalEntryDto | null>(null);

  /**
   * 엔트리 읽기 전용 모달을 연다. 수정 폼이 아니라 목록과 동일한 markdownContent 본문을 보여 준다.
   * @param id - 엔트리 ID
   */
  async function openEntryView(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    closeEntryRegist();
    entryViewOpen.value = true;
    entryViewLoading.value = true;
    entryViewModel.value = null;
    try {
      const res = await axios.get(`/api/journal/entry/${id}`);
      const entry = res.data?.rsltObj as JournalEntryDto | undefined;
      if (!entry) {
        entryViewOpen.value = false;
        return;
      }
      entryViewModel.value = entry;
    } catch (e: unknown) {
      console.error("[journalModal] openEntryView failed", { entryId: id }, e);
      entryViewModel.value = null;
      entryViewOpen.value = false;
      void swalRequestError(e, t("journal.entry.view.load.failure"));
    } finally {
      entryViewLoading.value = false;
    }
  }

  /** 엔트리 읽기 전용 모달을 닫는다. */
  function closeEntryView() {
    entryViewOpen.value = false;
    entryViewModel.value = null;
  }

  /**
   * 읽기 전용 모달에서 수정 모달로 전환한다.
   * JOURNAL_REFLECTION 은 Reflection 전용 모달로, 그 외는 openEntryModify 경로를 쓴다.
   */
  async function openEntryModifyFromView() {
    const id = entryViewModel.value?.id;
    const contentType = entryViewModel.value?.contentType;
    const refId = entryViewModel.value?.refId;
    const refContentType = entryViewModel.value?.refContentType;
    const journalDayId = entryViewModel.value?.journalDayId;
    const journalChapterId = entryViewModel.value?.journalChapterId;
    closeEntryView();
    if (id == null) return;
    if (contentType === "JOURNAL_REFLECTION") {
      await openReflectionRegist({ id, refId, refContentType, journalDayId, journalChapterId });
      return;
    }
    await openEntryModify(id);
  }


  return {
    entryRegistOpen,
    entryRegistLoading,
    entryRegistModel,
    entryCategoryMap,
    entryPrefixOptionsFor,
    entryPrefixLoadFailedFor,
    prefetchEntryPrefixes,
    openEntryRegist,
    openDreamEntryRegist,
    openEntryModify,
    closeEntryRegist,
    entryViewOpen,
    entryViewLoading,
    entryViewModel,
    openEntryView,
    closeEntryView,
    openEntryModifyFromView,
  };
}
