import { describe, expect, it } from "vitest";
import type { ChatMessage } from "@/features/chat/stores/chat";
import type { RagMetadata, RagSource } from "@/features/chat/chatMessageFormat";
import { useRagSources } from "@/features/chat/useRagSources";

function msg(id: number): ChatMessage {
  return { id } as ChatMessage;
}

function sources(n: number): RagSource[] {
  return Array.from({ length: n }, (_, i) => ({ rank: i + 1, journalEntryId: i + 1 }));
}

function meta(n: number): RagMetadata {
  return { ragSources: sources(n) };
}

describe("useRagSources", () => {
  it("5개 초과면 미리보기 5개만 노출하고 나머지를 hidden 으로 센다", () => {
    const { visibleRagSources, hiddenRagSourceCount } = useRagSources();
    const m = msg(1);
    expect(visibleRagSources(m, meta(8))).toHaveLength(5);
    expect(hiddenRagSourceCount(m, meta(8))).toBe(3);
  });

  it("5개 이하면 전체 노출, hidden 0", () => {
    const { visibleRagSources, hiddenRagSourceCount } = useRagSources();
    const m = msg(1);
    expect(visibleRagSources(m, meta(3))).toHaveLength(3);
    expect(hiddenRagSourceCount(m, meta(3))).toBe(0);
  });

  it("expandRagSources 후에는 전체 노출·hidden 0", () => {
    const { visibleRagSources, hiddenRagSourceCount, expandRagSources } = useRagSources();
    const m = msg(1);
    expandRagSources(m);
    expect(visibleRagSources(m, meta(8))).toHaveLength(8);
    expect(hiddenRagSourceCount(m, meta(8))).toBe(0);
  });

  it("메타데이터/소스가 없으면 빈 배열·hidden 0", () => {
    const { visibleRagSources, hiddenRagSourceCount } = useRagSources();
    const m = msg(1);
    expect(visibleRagSources(m, null)).toEqual([]);
    expect(hiddenRagSourceCount(m, null)).toBe(0);
    expect(visibleRagSources(m, {})).toEqual([]);
  });

  it("펼침은 메시지별로 독립적이다", () => {
    const { visibleRagSources, expandRagSources } = useRagSources();
    const a = msg(1);
    const b = msg(2);
    expandRagSources(a);
    expect(visibleRagSources(a, meta(8))).toHaveLength(8);
    expect(visibleRagSources(b, meta(8))).toHaveLength(5);
  });
});
