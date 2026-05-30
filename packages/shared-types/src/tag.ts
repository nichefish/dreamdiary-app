import type { Identifier } from "./api";

export type TagItem = {
  tagId: Identifier;
  name: string;
  ctgr?: string;
  value?: string;
};

export type TagComposition = {
  list?: TagItem[];
  tagListStrWithCtgr?: string;
};

export type TagListItem = {
  id: Identifier;
  name: string;
  ctgr: string;
  contentSize: number;
  textClass?: string;
  tagClass?: string;
};

export type TagListParams = {
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
};

