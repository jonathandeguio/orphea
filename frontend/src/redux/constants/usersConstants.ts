// actionTypes.ts
export const USER_API_REQUEST = "USER_API_REQUEST";
export const USER_API_FAILURE = "USER_API_FAILURE";
export const CACHE_USER_API_RESULT = "CACHE_USER_API_RESULT";

interface UserApiRequestAction {
  type: typeof USER_API_REQUEST;
  payload: {
    id: string;
    data: any;
  };
}

interface UserApiFailureAction {
  type: typeof USER_API_FAILURE;
  payload: {
    id: string;
    data: any;
  };
}

interface CacheUserApiResultAction {
  type: typeof CACHE_USER_API_RESULT;
  payload: {
    id: string;
    data: any;
  };
}

export type UserApiActionTypes =
  | UserApiRequestAction
  | UserApiFailureAction
  | CacheUserApiResultAction;
