import { ref } from "vue";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { JournalReflectionRegistModel } from "@/features/journal/stores/journalModal.types";

/**
 * 저널 Reflection 등록/수정 모달 surface.
 */
export function createJournalModalReflection() {
  const reflectionRegistOpen = ref(false);
  /** Reflection 등록/수정 폼 모델 */
  const reflectionRegistModel = ref<JournalReflectionRegistModel | null>(null);

  /**
   * Reflection 등록/수정 모달을 연다. Reflection 은 Entry 이므로 entry 상세/등록 경로를 쓴다.
   * 수정(id 있음)은 entry 상세 API로 실제 저장값을 폼 모델로 사용한다.
   * 신규는 target 묶기(refId/refContentType) 또는 챕터 직속(journalChapterId 만) 모두 가능하다.
   *
   * @param payload 신규 시 target·chapter 초기값(독립이면 journalChapterId만), 수정 시 id
   */
  async function openReflectionRegist(payload?: JournalReflectionRegistModel): Promise<void> {
    if (!await assertAuthenticatedBeforeModal()) return;
    /* payload 의 refId/refContentType 을 categoryMap 로드 전에 심어 딸린/독립 판정이 빗나가지 않게 한다. */
    let merged: JournalReflectionRegistModel = {
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
      ...payload,
    };
    reflectionRegistModel.value = merged;
    if (payload?.id) {
      try {
        const res = await axios.get(`/api/journal/reflection/${payload.id}`);
        const dto = res.data?.rsltObj as JournalReflectionRegistModel | undefined;
        if (!res.data?.rslt || !dto?.id) {
          console.error("[journalModal] openReflectionRegist 상세 조회 결과 없음 id=", payload.id);
          reflectionRegistModel.value = null;
          return;
        }
        const tagCmpstn = dto.tag as { tagListStrWithCtgr?: string; tagListStr?: string } | undefined;
        /* 수정: 상세의 target 이 있으면 유지. 없으면 payload(호출 의도)를 존중한다. */
        merged = {
          ...merged,
          ...dto,
          refId: dto.refId ?? payload.refId ?? merged.refId,
          refContentType: dto.refContentType ?? payload.refContentType ?? merged.refContentType,
          journalDayId: dto.journalDayId ?? merged.journalDayId,
          journalChapterId: dto.journalChapterId ?? merged.journalChapterId,
          stdrdDt: dto.stdrdDt ?? merged.stdrdDt,
          tag: { tagListStrWithCtgr: tagCmpstn?.tagListStrWithCtgr ?? tagCmpstn?.tagListStr ?? "" },
        };
        reflectionRegistModel.value = merged;
      } catch (e: unknown) {
        console.error("[journalModal] openReflectionRegist 상세 조회 실패 id=", payload.id, e);
        reflectionRegistModel.value = null;
        return;
      }
    }
    reflectionRegistOpen.value = true;
  }

  /** Reflection 등록/수정 모달을 닫는다. 다음 오픈 시 이전 target/태그 상태가 남지 않게 모델을 비운다. */
  function closeReflectionRegist() {
    reflectionRegistOpen.value = false;
    reflectionRegistModel.value = null;
  }

  return {
    reflectionRegistOpen,
    reflectionRegistModel,
    openReflectionRegist,
    closeReflectionRegist,
  };
}
