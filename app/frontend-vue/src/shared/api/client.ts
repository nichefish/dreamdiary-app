import axios, { type AxiosRequestConfig } from "axios";
import type { AjaxResponse, PageResult } from "./types";

/**
 * 서버 메시지·호출부 fallback 이 모두 없을 때 던질 최후 문구.
 * 호출부는 통상 `failureMessage` 로 현재 locale 문구를 전달하므로 실제로 노출되는 일은 드물다.
 */
const DEFAULT_FAILURE_MESSAGE = "REQUEST_FAILED";

/** axios 응답을 공통 AjaxResponse 로 정규화한다. body 가 없으면 실패로 간주한다. */
async function toAjax<T>(promise: Promise<{ data?: unknown }>): Promise<AjaxResponse<T>> {
  const res = await promise;
  return (res.data ?? { rslt: false }) as AjaxResponse<T>;
}

/* ---- 저수준: throw 하지 않고 AjaxResponse 를 그대로 반환 (soft-fail·raw 소비용) ---- */

/** GET 요청. 응답 계약(AjaxResponse)을 그대로 반환한다. */
export function apiGet<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AjaxResponse<T>> {
  return toAjax<T>(config ? axios.get(url, config) : axios.get(url));
}

/** POST 요청. body 는 JSON 또는 FormData 를 그대로 전달한다. */
export function apiPost<T = unknown>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<AjaxResponse<T>> {
  return toAjax<T>(config ? axios.post(url, body, config) : axios.post(url, body));
}

/** PUT 요청. */
export function apiPut<T = unknown>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<AjaxResponse<T>> {
  return toAjax<T>(config ? axios.put(url, body, config) : axios.put(url, body));
}

/** PATCH 요청. */
export function apiPatch<T = unknown>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<AjaxResponse<T>> {
  return toAjax<T>(config ? axios.patch(url, body, config) : axios.patch(url, body));
}

/** DELETE 요청. */
export function apiDelete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AjaxResponse<T>> {
  return toAjax<T>(config ? axios.delete(url, config) : axios.delete(url));
}

/* ---- 언랩: rslt=false 면 서버 메시지 우선으로 throw (지배 패턴) ---- */

/**
 * 응답이 실패면 예외를 던진다. 메시지 우선순위: 서버 message → 호출부 failureMessage → 최후 문구.
 *
 * @param res 정규화된 응답
 * @param failureMessage 서버 메시지가 없을 때 사용할 현재 locale 문구
 */
export function assertOk(res: AjaxResponse, failureMessage?: string): void {
  if (!res.rslt) throw new Error(res.message ?? failureMessage ?? DEFAULT_FAILURE_MESSAGE);
}

/** 실패면 throw, 성공이면 단건 페이로드(rsltObj)를 반환한다. */
export function unwrapObj<T>(res: AjaxResponse<T>, failureMessage?: string): T {
  assertOk(res, failureMessage);
  return res.rsltObj as T;
}

/** 실패면 throw, 성공이면 목록 페이로드(rsltList, 없으면 빈 배열)를 반환한다. */
export function unwrapList<T>(res: AjaxResponse<T>, failureMessage?: string): T[] {
  assertOk(res, failureMessage);
  return res.rsltList ?? [];
}

/** 실패면 throw, 성공이면 서버 메시지를 반환한다. (등록/수정/삭제 등 성공 메시지 소비용) */
export function unwrapOk(res: AjaxResponse, failureMessage?: string): string {
  assertOk(res, failureMessage);
  return res.message ?? "";
}

/** 실패면 throw, 성공이면 rsltObj 안의 페이징 결과를 정규화해 반환한다. */
export function unwrapPage<T>(res: AjaxResponse, failureMessage?: string): PageResult<T> {
  assertOk(res, failureMessage);
  const page = (res.rsltObj ?? {}) as Partial<PageResult<T>>;
  return {
    content: Array.isArray(page.content) ? page.content : [],
    totalElements: Number(page.totalElements ?? 0),
    totalPages: Number(page.totalPages ?? 0),
    number: Number(page.number ?? 0),
    size: Number(page.size ?? 0),
  };
}

/* ---- 편의(GET): 요청 + 언랩(throw)을 한 번에 ---- */

/** 요청 옵션. axios config 와 실패 시 fallback 메시지를 함께 넘긴다. */
export interface RequestOpts {
  config?: AxiosRequestConfig;
  failureMessage?: string;
}

/** GET + 단건 언랩. 실패면 throw. */
export async function getObj<T>(url: string, opts?: RequestOpts): Promise<T> {
  return unwrapObj<T>(await apiGet<T>(url, opts?.config), opts?.failureMessage);
}

/** GET + 목록 언랩. 실패면 throw. */
export async function getList<T>(url: string, opts?: RequestOpts): Promise<T[]> {
  return unwrapList<T>(await apiGet<T>(url, opts?.config), opts?.failureMessage);
}

/** GET + 페이징 언랩. 실패면 throw. */
export async function getPage<T>(url: string, opts?: RequestOpts): Promise<PageResult<T>> {
  return unwrapPage<T>(await apiGet(url, opts?.config), opts?.failureMessage);
}