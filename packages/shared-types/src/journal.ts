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

export type JournalInterpretation = {
  id: Identifier;
  contentType?: ContentType;
  refId?: Identifier;
  refContentType?: ContentType;
  journalDayId?: Identifier;
  title?: string;
  content?: string;
  markdownContent?: string;
  sortOrder?: number;
  stdrdDt?: string;
  state?: StateComposition;
  lifecycle?: LifecycleComposition;
  history?: HistoryComposition;
  comment?: CommentComposition;
};

export type JournalEntry = {
  id: Identifier;
  contentType?: ContentType;
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
  journalInterpretationList?: JournalInterpretation[];
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

