import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";

/**
 * 저널 복사(엔트리·챕터·검색·스레드)가 리플렉션(해석)을 어느 범위까지 담을지 정하는 단일 계약.
 * <p>
 * 복사 split 버튼의 3단계 그라디언트를 표현한다:
 * <ul>
 *   <li>{@code full} — 주 버튼. 모든 리플렉션 포함(보류 PENDING 포함).</li>
 *   <li>{@code no-pending} — 드롭다운. 리플렉션을 포함하되 보류(PENDING)만 제외(OPEN·RESOLVED 유지).</li>
 *   <li>{@code body} — 드롭다운. 리플렉션을 전부 제외(본문만).</li>
 * </ul>
 * 리플렉션 본문 소스 선택(`content ?? markdownContent`)은 복사 내용 계약을 따르며,
 * 이 모듈은 "어느 리플렉션을 담을지"(포함/제외)와 복사 본문 사이의 공통 빈 줄 경계를 함께 보장한다.
 */
export type CopyReflectionMode = "full" | "no-pending" | "body";

/** 저널 클립보드 복사의 공통 Windows 호환 줄바꿈. */
export const JOURNAL_COPY_LINE_BREAK = "\r\n";

/** 저널 클립보드 복사에서 본문 블록 사이에 빈 줄 1개를 만드는 공통 구분자. */
export const JOURNAL_COPY_BLOCK_SEPARATOR = `${JOURNAL_COPY_LINE_BREAK}${JOURNAL_COPY_LINE_BREAK}`;

/** 공통 복사 formatter가 참조하는 리플렉션 최소 계약. */
interface CopyReflectionSource {
  content?: string | null;
  markdownContent?: string | null;
  lifecycle?: {
    lifecycleKey?: string | null;
  } | null;
}

/**
 * 주어진 복사 모드에서 이 리플렉션을 복사에 포함할지 판정한다.
 *
 * @param mode 복사 모드
 * @param lifecycleKey 리플렉션 lifecycle 키(OPEN/PENDING/RESOLVED). 없으면 OPEN 취급.
 * @return 포함하면 true
 */
export function includeReflectionInCopy(
  mode: CopyReflectionMode,
  lifecycleKey: string | null | undefined,
): boolean {
  if (mode === "body") return false;
  if (mode === "no-pending") return lifecycleKey !== "PENDING";
  return true;
}

/**
 * 텍스트에 섞인 LF·CRLF 줄바꿈을 저널 복사 공통 CRLF로 정규화한다.
 *
 * @param text 정규화할 평문
 * @return CRLF 줄바꿈을 사용하는 평문
 */
export function normalizeJournalCopyLineBreaks(text: string): string {
  return text.replace(/\r\n|\r|\n/g, JOURNAL_COPY_LINE_BREAK);
}

/**
 * 엔트리 본문 뒤에 복사 모드가 허용한 리플렉션 본문을 빈 줄 1개로 이어 붙인다.
 * <p>
 * 엔트리·챕터·검색·스레드 복사는 이 함수를 공유해 리플렉션 사이 경계와 줄바꿈 형식을 동일하게 유지한다.
 * 빈 리플렉션은 제외하며, 저작 원문 {@code content}를 우선해 평문으로 변환한다.
 *
 * @param baseText 날짜·순번·엔트리 본문을 포함한 선행 평문
 * @param reflections 대상 엔트리를 가리키는 리플렉션 목록
 * @param mode 복사 모드
 * @return 선행 평문과 리플렉션 본문을 공통 포맷으로 조립한 문자열
 */
export function appendReflectionsToCopyText(
  baseText: string,
  reflections: CopyReflectionSource[] | null | undefined,
  mode: CopyReflectionMode,
): string {
  const normalizedBase = normalizeJournalCopyLineBreaks(baseText);
  const reflectionText = (reflections ?? [])
    .filter((reflection) => includeReflectionInCopy(mode, reflection.lifecycle?.lifecycleKey))
    .map((reflection) => htmlToPlainText(reflection.content ?? reflection.markdownContent ?? ""))
    .filter(Boolean)
    .map(normalizeJournalCopyLineBreaks)
    .join(JOURNAL_COPY_BLOCK_SEPARATOR);

  if (!reflectionText) return normalizedBase;
  if (!normalizedBase) return reflectionText;
  return `${normalizedBase}${JOURNAL_COPY_BLOCK_SEPARATOR}${reflectionText}`;
}

/**
 * 복사 성공 토스트 i18n 키를 복사 범위에 맞게 고른다.
 * <p>
 * 본문만은 항상 본문 문구, 나머지는 리플렉션이 하나도 없으면 공용 복사 문구로 수렴한다.
 *
 * @param mode 복사 모드
 * @param hasReflection 복사 대상에 리플렉션이 하나라도 있으면 true
 * @return i18n 키
 */
export function copySuccessKey(mode: CopyReflectionMode, hasReflection: boolean): string {
  if (mode === "body") return "journal.copy.body.success";
  if (!hasReflection) return "common.copy.success";
  if (mode === "no-pending") return "journal.copy.no-pending.success";
  return "journal.copy.full.success";
}
