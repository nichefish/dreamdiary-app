export type Identifier = number | string;

export type ApiResult<T = unknown> = {
  rslt?: boolean;
  message?: string;
  rsltSts?: string;
  rsltObj?: T;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first?: boolean;
  last?: boolean;
};

export type PageQuery = {
  page?: number;
  size?: number;
  sort?: string;
};

export type PeriodQuery = {
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
  startDt?: string;
  endDt?: string;
};

