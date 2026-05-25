import type {
  ApiResult,
  EmotionCapture,
  JournalEntry,
  JournalEntryCreatePayload,
  PageQuery,
  PageResponse,
  PeriodQuery,
  QuickDreamCapture,
  TagListItem
} from "@dreamdiary/shared-types";

import type { HttpClient } from "./httpClient";

export type JournalListQuery = PageQuery & PeriodQuery & {
  keyword?: string;
};

export function createJournalApi(client: HttpClient) {
  return {
    listDays(query: JournalListQuery = {}) {
      return client.get<PageResponse<JournalEntry> | JournalEntry[]>("/api/journal/days", { query });
    },

    listDiaries(query: JournalListQuery = {}) {
      return client.get<PageResponse<JournalEntry> | JournalEntry[]>("/api/journal/diaries", { query });
    },

    listDreams(query: JournalListQuery = {}) {
      return client.get<PageResponse<JournalEntry> | JournalEntry[]>("/api/journal/dreams", { query });
    },

    createDiary(payload: JournalEntryCreatePayload | EmotionCapture) {
      return client.post<ApiResult<JournalEntry>>("/api/journal/diaries", payload);
    },

    createDream(payload: JournalEntryCreatePayload | QuickDreamCapture) {
      return client.post<ApiResult<JournalEntry>>("/api/journal/dreams", payload);
    },

    deleteEntry(id: string | number) {
      return client.delete<ApiResult>(`/api/journal/entry/${id}`);
    },

    listEntryTags(query: PeriodQuery & { type?: "DIARY" | "DREAM" } = {}) {
      return client.get<TagListItem[]>("/api/journal/entry/tags", { query });
    }
  };
}

