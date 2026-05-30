import type { TagItem } from "@dreamdiary/shared-types";

export function normalizeTagName(value: string): string {
  return value.trim().replace(/\s+/g, "_");
}

export function serializeTagsWithCategory(tags: TagItem[]): string {
  return tags
    .map((tag) => {
      const name = normalizeTagName(tag.name);
      const category = tag.ctgr?.trim();
      const metaValue = tag.value?.trim();

      if (category && metaValue) return `${name}:${category}:${metaValue}`;
      if (category) return `${name}:${category}`;

      return name;
    })
    .filter(Boolean)
    .join(",");
}

