/** Prefix 필드 표시 판단에 필요한 최소 선택지 계약. */
export interface JournalPrefixFieldOption {
  id?: number | null;
  activeYn?: string | null;
}

/** Prefix 필드의 표시 여부와 별도로 주입할 비활성 과거 선택. */
export interface JournalPrefixFieldPresentation<T extends JournalPrefixFieldOption> {
  visible: boolean;
  inactivePrefix: T | null;
}

/**
 * 활성 선택지와 저장된 과거 선택을 기준으로 Prefix 필드 표시 상태를 계산한다.
 * 비활성 과거 선택은 활성 옵션 목록에 없을 때만 별도 옵션으로 제공한다.
 */
export function resolveJournalPrefixField<T extends JournalPrefixFieldOption>(
  options: readonly JournalPrefixFieldOption[],
  selectedPrefix?: T | null,
  forceVisible = false,
): JournalPrefixFieldPresentation<T> {
  const inactivePrefix = selectedPrefix?.activeYn === "N"
    && selectedPrefix.id != null
    && !options.some((option) => option.id === selectedPrefix.id)
    ? selectedPrefix
    : null;

  return {
    visible: forceVisible || options.length > 0 || inactivePrefix !== null,
    inactivePrefix,
  };
}
