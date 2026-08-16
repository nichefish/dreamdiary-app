import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import { swalFire } from "@/shared/utils/swal";

/** 복사/다운로드가 참조하는 최소 스레드 계약 (제목·식별자만 필요). */
interface JournalThreadCopySource {
  id?: number | string;
  title?: string;
}

/**
 * 스레드 상세를 "제목 + 소속 엔트리" 평문으로 만든다.
 * <p>
 * 소속 엔트리는 검색 팝업 전체 복사(copyAll)와 동일한 포맷 — 일자가 바뀔 때만 `날짜(요일)` 헤더,
 * 그 아래 `#순번`과 본문 평문, 엔트리 사이 빈 줄 — 을 따르고, 맨 앞에 스레드 제목을 머리행으로 얹는다.
 * 서버 내보내기(JournalThreadExportService)와 배너 유무만 다르며 엔트리 블록 계약은 같다.
 *
 * @param thread 스레드(제목)
 * @param entries 소속 엔트리 (일자 오름차순)
 * @param t 현재 locale 번역 함수 (요일 라벨용)
 * @param includeReflection 해석 포함 여부 (기본 true). 포함 시 각 엔트리를 target 으로 한 리플렉션 본문을 이어 붙인다.
 * @return 클립보드/미리보기용 평문
 */
export function buildThreadCopyText(
  thread: JournalThreadCopySource | null | undefined,
  entries: JournalEntryDto[],
  t: (key: string) => string,
  includeReflection = true,
): string {
  const title = (thread?.title ?? "").trim();
  let prevDate: string | null = null;
  const blocks = entries.map((entry) => {
    const dt = entry.stdrdDt ?? "";
    const weekDay = getWeekDayStr(dt, t);
    const dateLabel = dt ? (weekDay ? `${dt} (${weekDay})` : dt) : "";
    const content = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
    let block = "";
    if (dateLabel !== prevDate) {
      block += `\r\n${dateLabel}\r\n`;
      prevDate = dateLabel;
    }
    block += [`#${entry.sortOrder ?? ""}`, content].join("\r\n");
    /* 해석 포함 시에만: 이 엔트리를 target 으로 한 리플렉션 본문을 빈 줄로 이어 붙인다(마커 없음). */
    if (includeReflection) {
      for (const reflection of entry.reflectionList ?? []) {
        const reflRaw = htmlToPlainText(reflection.content ?? reflection.markdownContent ?? "");
        if (reflRaw) block += `\r\n\r\n${reflRaw}`;
      }
    }
    return block;
  });
  const body = blocks.join("\r\n\r\n").trim();
  return title ? `${title}\r\n\r\n${body}`.trim() : body;
}

/**
 * 스레드 상세(제목 + 소속 엔트리)를 클립보드에 복사하고 결과를 알린다.
 *
 * @param thread 스레드(제목)
 * @param entries 소속 엔트리
 * @param t 현재 locale 번역 함수
 * @param includeReflection 해석 포함 여부 (기본 true)
 */
export async function copyThreadDetail(
  thread: JournalThreadCopySource | null | undefined,
  entries: JournalEntryDto[],
  t: (key: string) => string,
  includeReflection = true,
): Promise<void> {
  const text = buildThreadCopyText(thread, entries, t, includeReflection);
  try {
    await navigator.clipboard.writeText(text);
    /* 성공 토스트는 복사 범위를 명시: 포함=전체, 제외=본문만. 리플렉션이 없으면 공용 문구. */
    const hasReflection = entries.some((e) => (e.reflectionList?.length ?? 0) > 0);
    const successKey = !includeReflection
      ? "journal.copy.body.success"
      : (hasReflection ? "journal.copy.full.success" : "common.copy.success");
    void swalFire({ icon: "success", text: t(successKey) });
  } catch (error: unknown) {
    console.error("[journal-thread] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

/**
 * 스레드 소속 엔트리를 서버 텍스트 내보내기 엔드포인트로 다운로드한다.
 * 서버가 `=== dreamdiary export ===` 배너 + 제목 + 소속 엔트리 텍스트를 첨부로 내려준다.
 *
 * @param threadId 스레드 식별자
 * @param includeReflection 해석 포함 여부 (기본 true). 서버 export가 각 엔트리 target 리플렉션 본문을 함께 내보낸다.
 */
export function downloadThreadDetail(threadId: number | string, includeReflection = true): void {
  window.location.href = `/api/journal/threads/${threadId}/export?includeReflection=${includeReflection}`;
}
