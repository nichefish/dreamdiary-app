import { reportRuntimeError } from "@/shared/utils/appRuntimeStatus";

export type RichEditorRuntimeComponent = typeof import("./RichEditorRuntime.vue")["default"];

let runtimePromise: Promise<RichEditorRuntimeComponent> | null = null;

/**
 * TinyMCE 런타임 컴포넌트를 최초 에디터 렌더 시 로드한다.
 * 모든 에디터 인스턴스는 같은 진행 중 요청을 공유하며, 실패한 요청은 다음 마운트에서 재시도한다.
 */
export function loadRichEditorRuntime(): Promise<RichEditorRuntimeComponent> {
  if (!runtimePromise) {
    runtimePromise = import("./RichEditorRuntime.vue")
      .then((module) => module.default)
      .catch((error: unknown) => {
        runtimePromise = null;
        reportRuntimeError(error, "rich-editor-runtime-load");
        throw error;
      });
  }
  return runtimePromise;
}
