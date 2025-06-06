// actionTypes.ts
export const API_REQUEST = "API_REQUEST";
export const API_FAILURE = "API_FAILURE";
export const CACHE_API_RESULT = "CACHE_API_RESULT";
export const INVALIDATE_API_RESULT = "INVALIDATE_API_RESULT";

interface ApiRequestAction {
  type: typeof API_REQUEST;
  payload: {
    id: string;
    topic: string;
    data: any;
  };
}

interface ApiFailureAction {
  type: typeof API_FAILURE;
  payload: {
    id: string;
    topic: string;
    data: any;
  };
}

interface CacheApiResultAction {
  type: typeof CACHE_API_RESULT;
  payload: {
    id: string;
    topic: string;
    data: any;
  };
}

interface InvalidateApiResultAction {
  type: typeof INVALIDATE_API_RESULT;
  payload: {
    id: string;
    topic: string;
  };
}

export type ApiActionTypes =
  | ApiRequestAction
  | ApiFailureAction
  | InvalidateApiResultAction
  | CacheApiResultAction;
