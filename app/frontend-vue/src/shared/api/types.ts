/**
 * 백엔드 공통 응답 계약(AjaxResponse) 타입.
 * <p>
 * Spring `AjaxResponse` 와 1:1 대응한다. 성공 여부(`rslt`)와 메시지, 단건(`rsltObj`)·
 * 목록(`rsltList`) 페이로드를 담는다. 페이징 응답은 `rsltObj` 안에 {@link PageResult} 형태로 들어온다.
 */
export interface AjaxResponse<T = unknown> {
  /** 처리 성공 여부 */
  rslt: boolean;
  /** 서버 결과 메시지 (성공/실패 공통) */
  message?: string;
  /** 단건 결과 페이로드 */
  rsltObj?: T;
  /** 목록 결과 페이로드 */
  rsltList?: T[];
}

/**
 * 페이징 목록 응답의 정규화 형태.
 * <p>
 * 서버 `Page` 직렬화(`content`/`totalElements`/`totalPages`/`number`/`size`)를 그대로 반영한다.
 */
export interface PageResult<T> {
  /** 현재 페이지 항목 목록 */
  content: T[];
  /** 전체 항목 수 */
  totalElements: number;
  /** 전체 페이지 수 */
  totalPages: number;
  /** 현재 페이지 번호 (0-base) */
  number: number;
  /** 페이지 크기 */
  size: number;
}