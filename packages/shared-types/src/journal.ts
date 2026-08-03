import type { ContentType, JournalEntryKind } from "./content";
import type { Identifier } from "./api";
import type { TagComposition } from "./tag";

export type StateItem = {
  stateKey: string;
};

export type StateComposition = {
  list?: StateItem[];
};

export type LifecycleComposition = {
  lifecycleKey?: string;
  lifecycleDesc?: string;
};

export type CommentItem = {
  id: Identifier;
  content: string;
  regDt?: string;
  createdAt?: string;
  createdByNm?: string;
};

export type CommentComposition = {
  list?: CommentItem[];
  cnt?: number;
  hasComment?: boolean;
};

export type RelatedContentItem = {
  id?: Identifier;
  contentType?: ContentType;
  refContentNo?: Identifier;
  refTitle?: string;
  relReason?: string;
};

export type HistoryComposition = {
  historyTriggeredAt?: string;
};

export type JournalEntry = {
  id: Identifier;
  contentType?: ContentType;
  /** Reflection target: 가리키는 대상 엔트리 ID */
  refId?: Identifier;
  /** Reflection target 콘텐츠 타입 */
  refContentType?: ContentType;
  title?: string;
  content?: string;
  markdownContent?: string;
  journalDayId?: Identifier;
  journalChapterId?: Identifier;
  stdrdDt?: string;
  sortOrder?: number;
  elseDreamYn?: "Y" | "N" | string;
  elseDreamerNm?: string;
  tag?: TagComposition;
  state?: StateComposition;
  lifecycle?: LifecycleComposition;
  history?: HistoryComposition;
  comment?: CommentComposition;
  relatedContentList?: RelatedContentItem[];
  reflectionList?: JournalEntry[];
};

export type JournalChapter = {
  id: Identifier;
  chapterType?: JournalEntryKind;
  title?: string;
  categoryCode?: string;
  categoryName?: string;
  journalDayId?: Identifier;
  stdrdDt?: string;
  sortOrder?: number;
  journalEntryList?: JournalEntry[];
  tag?: TagComposition;
  state?: StateComposition;
};

export type JournalEntryCreatePayload = {
  contentType?: ContentType;
  title?: string;
  content: string;
  markdownContent?: string;
  stdrdDt?: string;
  tag?: {
    tagListStrWithCtgr?: string;
  };
};

export type QuickDreamCapture = Omit<JournalEntryCreatePayload, "contentType"> & {
  dreamedAt?: string;
};

export type EmotionCapture = Omit<JournalEntryCreatePayload, "contentType"> & {
  recordedAt?: string;
  emotionTags?: string[];
};

