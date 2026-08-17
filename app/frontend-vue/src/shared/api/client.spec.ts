import { beforeEach, describe, expect, it, vi } from "vitest";
import axios from "axios";
import {
  apiGet,
  apiPost,
  assertOk,
  unwrapObj,
  unwrapList,
  unwrapOk,
  unwrapPage,
  getObj,
  getList,
  getPage,
} from "./client";
import type { AjaxResponse } from "./types";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

const ok = <T>(over: Partial<AjaxResponse<T>>): AjaxResponse<T> => ({ rslt: true, ...over });
const fail = (over: Partial<AjaxResponse> = {}): AjaxResponse => ({ rslt: false, ...over });

describe("shared/api client", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("apiGet 은 axios 응답의 data 를 AjaxResponse 로 반환한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: ok({ rsltObj: { id: 1 } }) });

    const res = await apiGet<{ id: number }>("/api/x", { params: { a: 1 } });

    expect(mockedGet).toHaveBeenCalledWith("/api/x", { params: { a: 1 } });
    expect(res.rslt).toBe(true);
    expect(res.rsltObj).toEqual({ id: 1 });
  });

  it("apiGet 은 data 가 없으면 rslt=false 로 정규화한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: undefined });

    const res = await apiGet("/api/x");

    expect(res.rslt).toBe(false);
  });

  it("apiPost 는 body 와 config 를 그대로 전달한다", async () => {
    mockedPost.mockResolvedValueOnce({ data: ok({}) });
    const fd = new FormData();
    fd.append("k", "v");

    await apiPost("/api/x", fd, { headers: { "Content-Type": "multipart/form-data" } });

    expect(mockedPost).toHaveBeenCalledWith("/api/x", fd, { headers: { "Content-Type": "multipart/form-data" } });
  });

  it("assertOk 는 실패 시 서버 message 를 우선해 throw 한다", () => {
    expect(() => assertOk(fail({ message: "서버 실패" }), "폴백")).toThrow("서버 실패");
  });

  it("assertOk 는 서버 message 가 없으면 failureMessage 로 throw 한다", () => {
    expect(() => assertOk(fail(), "폴백 문구")).toThrow("폴백 문구");
  });

  it("assertOk 는 둘 다 없으면 최후 문구로 throw 한다", () => {
    expect(() => assertOk(fail())).toThrow("REQUEST_FAILED");
  });

  it("unwrapObj 는 성공 시 rsltObj 를 반환한다", () => {
    expect(unwrapObj(ok({ rsltObj: { name: "가상" } }))).toEqual({ name: "가상" });
  });

  it("unwrapList 는 성공 시 rsltList 를, 없으면 빈 배열을 반환한다", () => {
    expect(unwrapList(ok({ rsltList: [1, 2] }))).toEqual([1, 2]);
    expect(unwrapList(ok({}))).toEqual([]);
  });

  it("unwrapList 는 실패 시 throw 한다", () => {
    expect(() => unwrapList(fail({ message: "목록 실패" }))).toThrow("목록 실패");
  });

  it("unwrapOk 는 성공 시 서버 message 를 반환한다", () => {
    expect(unwrapOk(ok({ message: "저장 완료" }))).toBe("저장 완료");
    expect(unwrapOk(ok({}))).toBe("");
  });

  it("unwrapPage 는 rsltObj 안의 페이징 필드를 정규화한다", () => {
    const page = unwrapPage(ok({ rsltObj: { content: [{ id: 1 }], totalElements: "5", totalPages: 1, number: 0, size: 10 } }));
    expect(page.content).toEqual([{ id: 1 }]);
    expect(page.totalElements).toBe(5);
    expect(page.number).toBe(0);
  });

  it("unwrapPage 는 content 가 없으면 빈 배열로 방어한다", () => {
    const page = unwrapPage(ok({ rsltObj: {} }));
    expect(page.content).toEqual([]);
    expect(page.totalElements).toBe(0);
  });

  it("getObj 는 GET 요청 후 단건을 언랩한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: ok({ rsltObj: { id: 7 } }) });

    const obj = await getObj<{ id: number }>("/api/x/7", { failureMessage: "실패" });

    expect(mockedGet).toHaveBeenCalledWith("/api/x/7");
    expect(obj).toEqual({ id: 7 });
  });

  it("getList 는 실패 응답에서 failureMessage 로 throw 한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: fail() });

    await expect(getList("/api/x", { failureMessage: "목록 로드 실패" })).rejects.toThrow("목록 로드 실패");
  });

  it("getPage 는 config 를 전달하고 페이징을 언랩한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: ok({ rsltObj: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 25 } }) });

    const page = await getPage("/api/x", { config: { params: { page: 0, size: 25 } } });

    expect(mockedGet).toHaveBeenCalledWith("/api/x", { params: { page: 0, size: 25 } });
    expect(page.size).toBe(25);
  });
});