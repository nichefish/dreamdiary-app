/**
 * appPath.ts
 * Vue SPA base(`/vue-app/`)와 경로를 합칠 때 연속 슬래시(`//`)가 생기지 않게 정규화한다.
 * Spring StrictHttpFirewall 은 요청 URI 에 `//` 가 포함되면 거부한다.
 */

/**
 * 연속 슬래시를 단일 슬래시로 접는다. (query/hash 는 유지)
 */
export function normalizeRoutePath(path: string): string {
  const hashIndex = path.indexOf("#");
  const hash = hashIndex >= 0 ? path.slice(hashIndex) : "";
  const withoutHash = hashIndex >= 0 ? path.slice(0, hashIndex) : path;
  const queryIndex = withoutHash.indexOf("?");
  const query = queryIndex >= 0 ? withoutHash.slice(queryIndex) : "";
  const pathname = queryIndex >= 0 ? withoutHash.slice(0, queryIndex) : withoutHash;
  const normalizedPathname = pathname.replace(/\/{2,}/g, "/") || "/";
  return `${normalizedPathname}${query}${hash}`;
}

/**
 * `/vue-app` 접두를 제거해 Vue Router `to` 경로로 맞춘다.
 */
export function stripVueAppPrefix(path: string): string {
  const stripped = path.replace(/^\/vue-app(?=\/|$)/i, "");
  return stripped || "/";
}

/**
 * import.meta.env.BASE_URL 과 하위 경로를 합쳐 브라우저 절대 경로를 만든다.
 * @param segment `/journal/daily` 형태 (선행 슬래시 optional)
 */
export function joinAppBasePath(segment: string): string {
  const base = String(import.meta.env.BASE_URL ?? "/").replace(/\/+$/, "");
  const rest = String(segment ?? "").replace(/^\/+/, "");
  if (!rest) return normalizeRoutePath(base || "/");
  const joined = base ? `${base}/${rest}` : `/${rest}`;
  return normalizeRoutePath(joined);
}

/**
 * API path segment(id 등)가 비어 있으면 `/api/...//...` 형태가 되므로 거부한다.
 */
export function requireApiPathSegment(value: unknown, label: string): string {
  const segment = String(value ?? "").trim();
  if (!segment) {
    throw new Error(`[appPath] missing API path segment: ${label}`);
  }
  return segment;
}
