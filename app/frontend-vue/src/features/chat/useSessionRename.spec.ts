// @vitest-environment happy-dom
import { nextTick } from "vue";
import { describe, expect, it, vi } from "vitest";
import type { ChatSession } from "@/features/chat/stores/chat";
import { useSessionRename } from "@/features/chat/useSessionRename";

interface FakeChat {
  lastError: string;
  renameSession: ReturnType<typeof vi.fn>;
}

function setup(overrides: Partial<FakeChat> = {}) {
  const chat: FakeChat = {
    lastError: "",
    renameSession: vi.fn().mockResolvedValue({ id: 1, title: "새 제목" }),
    ...overrides,
  };
  const t = (key: string): string => key;
  const rename = useSessionRename(
    chat as unknown as Parameters<typeof useSessionRename>[0],
    t
  );
  return { chat, rename };
}

const session = (over: Partial<ChatSession> = {}): ChatSession =>
  ({ id: 1, title: "기존", ...over }) as ChatSession;

describe("useSessionRename", () => {
  it("startRename 은 편집 상태를 세팅한다", () => {
    const { rename } = setup();
    rename.startRename(session({ id: 7, title: "T" }));
    expect(rename.renamingSessionId.value).toBe(7);
    expect(rename.renameDraft.value).toBe("T");
  });

  it("저장 중에는 startRename 이 무시된다", () => {
    const { rename } = setup();
    rename.isRenamingSaving.value = true;
    rename.startRename(session({ id: 7 }));
    expect(rename.renamingSessionId.value).toBeNull();
  });

  it("cancelRename 은 편집 상태를 초기화한다", () => {
    const { rename } = setup();
    rename.startRename(session({ id: 3, title: "x" }));
    rename.cancelRename();
    expect(rename.renamingSessionId.value).toBeNull();
    expect(rename.renameDraft.value).toBe("");
    expect(rename.isRenamingSaving.value).toBe(false);
  });

  it("빈 제목 커밋은 오류를 남기고 저장 없이 취소한다", async () => {
    const { chat, rename } = setup();
    rename.startRename(session({ id: 1 }));
    rename.renameDraft.value = "   ";
    await rename.commitRename(session({ id: 1 }));
    expect(chat.lastError).toBe("chat.session.rename.empty");
    expect(chat.renameSession).not.toHaveBeenCalled();
    expect(rename.renamingSessionId.value).toBeNull();
  });

  it("변경 없는 커밋은 저장 없이 취소한다", async () => {
    const { chat, rename } = setup();
    rename.startRename(session({ id: 1, title: "그대로" }));
    await rename.commitRename(session({ id: 1, title: "그대로" }));
    expect(chat.renameSession).not.toHaveBeenCalled();
    expect(rename.renamingSessionId.value).toBeNull();
  });

  it("성공 커밋은 renameSession 을 호출하고 상태를 리셋한다", async () => {
    const { chat, rename } = setup();
    rename.startRename(session({ id: 1, title: "기존" }));
    rename.renameDraft.value = "새 제목";
    await rename.commitRename(session({ id: 1, title: "기존" }));
    expect(chat.renameSession).toHaveBeenCalledWith(1, "새 제목");
    expect(chat.lastError).toBe("");
    expect(rename.renamingSessionId.value).toBeNull();
    expect(rename.isRenamingSaving.value).toBe(false);
  });

  it("renameSession 이 falsy 를 반환하면 실패 오류를 남긴다", async () => {
    const { chat, rename } = setup({ renameSession: vi.fn().mockResolvedValue(null) });
    rename.startRename(session({ id: 1, title: "기존" }));
    rename.renameDraft.value = "새 제목";
    await rename.commitRename(session({ id: 1, title: "기존" }));
    expect(chat.lastError).toBe("chat.session.rename.failure");
    expect(rename.renamingSessionId.value).toBeNull();
  });

  it("renameSession 이 throw 하면 실패 오류를 남긴다", async () => {
    const errorSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    const { chat, rename } = setup({
      renameSession: vi.fn().mockRejectedValue(new Error("boom")),
    });
    rename.startRename(session({ id: 1, title: "기존" }));
    rename.renameDraft.value = "새 제목";
    await rename.commitRename(session({ id: 1, title: "기존" }));
    expect(chat.lastError).toBe("chat.session.rename.failure");
    expect(rename.isRenamingSaving.value).toBe(false);
    errorSpy.mockRestore();
  });

  it("취소로 잠긴 뒤의 커밋은 무시된다", async () => {
    const { chat, rename } = setup();
    rename.startRename(session({ id: 1, title: "기존" }));
    rename.renameDraft.value = "새 제목";
    rename.cancelRename();
    await rename.commitRename(session({ id: 1, title: "기존" }));
    expect(chat.renameSession).not.toHaveBeenCalled();
  });

  it("setRenameInputRef 로 등록된 input 에 startRename 이 포커스한다", async () => {
    const { rename } = setup();
    const input = document.createElement("input");
    const focusSpy = vi.spyOn(input, "focus");
    rename.setRenameInputRef(5, input);
    rename.startRename(session({ id: 5, title: "T" }));
    await nextTick();
    expect(focusSpy).toHaveBeenCalled();
  });
});
