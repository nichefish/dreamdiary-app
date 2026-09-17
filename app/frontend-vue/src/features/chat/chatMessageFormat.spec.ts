import { describe, expect, it } from "vitest";
import type { ChatMessage } from "@/features/chat/stores/chat";
import {
  assistantHtml,
  escapeHtml,
  formatCountItems,
  formatScore,
  formatTextList,
  isAssistantMessage,
  isOwnMessage,
  messageBubbleClass,
  messageRagMetadata,
  messageRole,
  messageRowClass,
  messageText,
  messageTime,
  ragMessageKey,
  type RagMetadata,
  tagPairText,
  timelineText,
  topTagText,
  tf,
  personRoleLabel,
  formatRoleList,
  personFocusText,
  responseModeText,
  guardDetailText,
} from "@/features/chat/chatMessageFormat";

/** 테스트용 catalog 번역 함수. ({0} placeholder 는 tf 가 치환) */
const catalog: Record<string, string> = {
  "chat.guard.prefix": "가드: {0}",
  "chat.guard.retry-prefix": "재시도: {0}",
  "chat.rag.person.target": "대상 {0}",
  "chat.response-mode.llm": "LLM 응답",
  "chat.person-role.LEADER": "리더",
};
const fakeT = (key: string): string => catalog[key] ?? key;

/** 테스트용 ChatMessage 부분 객체를 만든다. (표시 함수는 일부 필드만 참조) */
function msg(partial: Partial<ChatMessage>): ChatMessage {
  return partial as ChatMessage;
}

describe("messageRole / role 판정", () => {
  it("role 을 대문자로 반환하고 없으면 빈 문자열", () => {
    expect(messageRole(msg({ role: "user" }))).toBe("USER");
    expect(messageRole(msg({}))).toBe("");
  });

  it("ASSISTANT/AI/SYSTEM 은 assistant 메시지", () => {
    expect(isAssistantMessage(msg({ role: "assistant" }))).toBe(true);
    expect(isAssistantMessage(msg({ role: "AI" }))).toBe(true);
    expect(isAssistantMessage(msg({ role: "system" }))).toBe(true);
    expect(isAssistantMessage(msg({ role: "user" }))).toBe(false);
  });

  it("본인 메시지는 non-assistant 이면서 isCreatedBy 가 true", () => {
    expect(isOwnMessage(msg({ role: "user", isCreatedBy: true }))).toBe(true);
    expect(isOwnMessage(msg({ role: "user", isCreatedBy: false }))).toBe(false);
    expect(isOwnMessage(msg({ role: "assistant", isCreatedBy: true }))).toBe(false);
  });
});

describe("텍스트/HTML 표시", () => {
  it("messageText / messageTime 은 없으면 빈 문자열", () => {
    expect(messageText(msg({ content: "hi" }))).toBe("hi");
    expect(messageText(msg({}))).toBe("");
    expect(messageTime(msg({ createdAt: "2026-08-31" }))).toBe("2026-08-31");
    expect(messageTime(msg({}))).toBe("");
  });

  it("escapeHtml 은 & < > \" 를 이스케이프한다", () => {
    expect(escapeHtml(`<a href="x">&`)).toBe("&lt;a href=&quot;x&quot;&gt;&amp;");
  });

  it("assistantHtml 은 markdownContent 를 우선한다", () => {
    expect(assistantHtml(msg({ markdownContent: "<h1>t</h1>" }))).toBe("<h1>t</h1>");
  });

  it("assistantHtml 은 markdownContent 가 없거나 '-' 면 평문을 escape 해 문단으로 감싼다", () => {
    expect(assistantHtml(msg({ markdownContent: "-", content: "a<b>\nc" }))).toBe(
      "<p>a&lt;b&gt;<br>c</p>"
    );
    expect(assistantHtml(msg({ content: "plain" }))).toBe("<p>plain</p>");
  });

  it("assistantHtml 은 내용이 전혀 없으면 빈 문자열", () => {
    expect(assistantHtml(msg({}))).toBe("");
  });
});

describe("messageRagMetadata 파싱/판정", () => {
  it("metadataJson 이 없으면 null", () => {
    expect(messageRagMetadata(msg({}))).toBeNull();
  });

  it("유효하지 않은 JSON 이면 null", () => {
    expect(messageRagMetadata(msg({ metadataJson: "{not json" }))).toBeNull();
  });

  it("유의미한 필드가 없으면 null", () => {
    expect(messageRagMetadata(msg({ metadataJson: JSON.stringify({ ragIntent: "x" }) }))).toBeNull();
  });

  it("responseMode 또는 ragSourceCount>0 이면 메타데이터를 반환한다", () => {
    expect(
      messageRagMetadata(msg({ metadataJson: JSON.stringify({ responseMode: "LLM" }) }))
    ).toEqual({ responseMode: "LLM" });
    expect(
      messageRagMetadata(msg({ metadataJson: JSON.stringify({ ragSourceCount: 3 }) }))
    ).toEqual({ ragSourceCount: 3 });
    expect(
      messageRagMetadata(msg({ metadataJson: JSON.stringify({ ragSourceCount: 0 }) }))
    ).toBeNull();
  });
});

describe("ragMessageKey", () => {
  it("id 가 있으면 id 문자열, 없으면 role-createdAt-content 조합", () => {
    expect(ragMessageKey(msg({ id: 42 }))).toBe("42");
    expect(ragMessageKey(msg({ role: "user", createdAt: "t", content: "c" }))).toBe("user-t-c");
  });
});

describe("format 헬퍼", () => {
  it("formatCountItems 는 limit 까지 name(count) 로 조인하고 빈 값은 대체한다", () => {
    expect(formatCountItems([{ name: "a", count: 2 }, {}], 5)).toBe("a(2), -(0)");
    expect(formatCountItems([{ name: "a" }, { name: "b" }, { name: "c" }], 2)).toBe("a(0), b(0)");
    expect(formatCountItems(undefined, 5)).toBe("");
    expect(formatCountItems([], 5)).toBe("");
  });

  it("formatTextList 는 limit 까지 조인한다", () => {
    expect(formatTextList(["a", "b", "c"], 2)).toBe("a, b");
    expect(formatTextList(undefined, 2)).toBe("");
  });

  it("formatScore 는 소수 3자리, 유한하지 않으면 빈 문자열", () => {
    expect(formatScore(0.12345)).toBe("0.123");
    expect(formatScore(Number.NaN)).toBe("");
    expect(formatScore(Number.POSITIVE_INFINITY)).toBe("");
  });
});

describe("timeline / tag 텍스트", () => {
  it("timelineText 는 timeline 이 없으면 빈 문자열", () => {
    expect(timelineText({} as RagMetadata)).toBe("");
  });

  it("timelineText 는 기간·종류·월을 ' · ' 로 조합한다", () => {
    const metadata: RagMetadata = {
      ragTimelineSummary: {
        firstDate: "2026-01",
        lastDate: "2026-08",
        contentKinds: [{ name: "DREAM", count: 3 }],
        months: [{ name: "2026-08", count: 2 }],
      },
    };
    expect(timelineText(metadata)).toBe("2026-01 ~ 2026-08 · DREAM(3) · 2026-08(2)");
  });

  it("topTagText / tagPairText 는 tagSummary 를 formatCountItems 로 위임한다", () => {
    const metadata: RagMetadata = {
      ragTagSummary: {
        totalTags: [{ name: "t1", count: 5 }],
        tagPairs: [{ name: "t1~t2", count: 2 }],
      },
    };
    expect(topTagText(metadata)).toBe("t1(5)");
    expect(tagPairText(metadata)).toBe("t1~t2(2)");
  });
});

describe("로케일(t) 주입 포매터", () => {
  it("tf 는 {0}.. placeholder 를 순서대로 치환한다", () => {
    expect(tf(fakeT, "chat.guard.prefix", "X")).toBe("가드: X");
    expect(tf((k) => k, "a {0} b {1}", "x", 2)).toBe("a x b 2");
  });

  it("personRoleLabel 은 매핑되면 라벨, 없으면 roleCode 를 반환한다", () => {
    expect(personRoleLabel("LEADER", fakeT)).toBe("리더");
    expect(personRoleLabel("UNKNOWN", fakeT)).toBe("UNKNOWN");
  });

  it("formatRoleList 은 ROLE(n) 을 라벨(n) 로 바꾸고 비매칭은 그대로 둔다", () => {
    expect(formatRoleList(["LEADER(3)", "MISC"], 5, fakeT)).toBe("리더(3), MISC");
    expect(formatRoleList(undefined, 5, fakeT)).toBe("");
  });

  it("responseModeText 는 매핑 모드는 번역, 미매핑은 원문, 없으면 빈 문자열", () => {
    expect(responseModeText({ responseMode: "LLM" }, fakeT)).toBe("LLM 응답");
    expect(responseModeText({ responseMode: "FOO" }, fakeT)).toBe("FOO");
    expect(responseModeText({}, fakeT)).toBe("");
  });

  it("guardDetailText 는 guard/retry 를 조합하고 동일 retry 는 생략한다", () => {
    expect(guardDetailText({ guardDetail: "a", retryGuardDetail: "b" }, fakeT)).toBe(
      "가드: a · 재시도: b"
    );
    expect(guardDetailText({ guardDetail: "a", retryGuardDetail: "a" }, fakeT)).toBe("가드: a");
    expect(guardDetailText({}, fakeT)).toBe("");
  });

  it("personFocusText 는 personFocus 가 없으면 빈 문자열, 있으면 target 을 조합한다", () => {
    expect(personFocusText({}, fakeT)).toBe("");
    expect(personFocusText({ personFocus: { target: "X" } }, fakeT)).toBe("대상 X");
  });
});

describe("row / bubble class", () => {
  it("assistant / own / other 별 클래스", () => {
    expect(messageRowClass(msg({ role: "assistant" }))).toBe("chat-message-row--assistant");
    expect(messageRowClass(msg({ role: "user", isCreatedBy: true }))).toBe("chat-message-row--own");
    expect(messageRowClass(msg({ role: "user", isCreatedBy: false }))).toBe("chat-message-row--other");
    expect(messageBubbleClass(msg({ role: "assistant" }))).toBe("chat-message-bubble--assistant");
    expect(messageBubbleClass(msg({ role: "user", isCreatedBy: true }))).toBe("chat-message-bubble--own");
    expect(messageBubbleClass(msg({ role: "user", isCreatedBy: false }))).toBe("chat-message-bubble--other");
  });
});
