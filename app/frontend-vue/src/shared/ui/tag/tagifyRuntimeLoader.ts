import { reportRuntimeError } from "@/shared/utils/appRuntimeStatus";

export type TagifyRuntimeConstructor = typeof import("@yaireo/tagify")["default"];

let runtimePromise: Promise<TagifyRuntimeConstructor> | null = null;

/**
 * Tagify 런타임과 기본 스타일을 최초 태그 입력 렌더 시 로드한다.
 * 모든 태그 입력 인스턴스는 같은 진행 중 요청을 공유하며, 실패한 요청은 다음 마운트에서 재시도한다.
 */
export function loadTagifyRuntime(): Promise<TagifyRuntimeConstructor> {
  if (!runtimePromise) {
    runtimePromise = Promise.all([
      import("@yaireo/tagify"),
      import("@yaireo/tagify/dist/tagify.css"),
    ])
      .then(([module]) => module.default)
      .catch((error: unknown) => {
        runtimePromise = null;
        reportRuntimeError(error, "tagify-runtime-load");
        throw error;
      });
  }
  return runtimePromise;
}
