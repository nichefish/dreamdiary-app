/** 관리자 화면 공용 숫자 포매터. AdminPage 및 admin 카드 컴포넌트가 공유한다. */
export function formatNumber(value: number): string {
  return new Intl.NumberFormat().format(Number(value) || 0);
}

/** 백분율 표기(소수 2자리). */
export function formatPercent(value: number): string {
  return `${(Number(value) || 0).toFixed(2)}%`;
}
