/** 백엔드 JournalInterpretationDto 최소 필드 */
export interface JournalInterpretation {
  id: number;
  contentType: string;
  /** 참조 엔트리 ID (JournalEntry.id) */
  refId: number;
  /** 기준 일자 (YYYY-MM-DD) */
  stdrdDt?: string;
  title?: string;
  /** HTML 본문 */
  content?: string;
  /** 마크다운 처리된 본문 */
  markdownContent?: string;
}