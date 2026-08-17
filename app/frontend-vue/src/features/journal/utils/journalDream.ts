/** API {@code JournalDreamSectionDto} — 일자 카드·상세 꿈 가상 섹션 */
export interface JournalDreamSectionDto {
  sectionKey: string;
  title: string;
  dreamerName?: string | null;
  entries: import("@/features/journal/stores/journal").JournalEntryDto[];
}

/** 꿈꾼 이름 트림 */
export function normalizeDreamerName(name?: string | null): string {
  return (name ?? "").trim();
}

/** 지정 꿈꾼(타인 꿈) 여부 — 이름 비어 있지 않으면 true */
export function hasDreamerName(entry: { dreamerName?: string | null } | null | undefined): boolean {
  return normalizeDreamerName(entry?.dreamerName) !== "";
}

/** 섹션 목록에 꿈 엔트리가 하나라도 있으면 true */
export function hasDreamSections(sections: JournalDreamSectionDto[] | null | undefined): boolean {
  return (sections ?? []).some((s) => (s.entries?.length ?? 0) > 0);
}

/** 등록 모달 초기값용 꿈꾼 이름 (내 꿈 섹션은 빈 문자열) */
export function sectionDreamerNameForRegist(section: JournalDreamSectionDto): string {
  return normalizeDreamerName(section.dreamerName);
}
