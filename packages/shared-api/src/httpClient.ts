export type QueryValue = string | number | boolean | undefined | null;

export type RequestConfig = {
  query?: Record<string, QueryValue>;
  headers?: Record<string, string>;
};

export type HttpClient = {
  get<T>(path: string, config?: RequestConfig): Promise<T>;
  post<T>(path: string, body?: unknown, config?: RequestConfig): Promise<T>;
  put<T>(path: string, body?: unknown, config?: RequestConfig): Promise<T>;
  delete<T>(path: string, config?: RequestConfig): Promise<T>;
};

