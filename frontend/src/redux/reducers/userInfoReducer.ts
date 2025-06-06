import { ApiStatusType } from "assets/constants/global";
import { AnyAction } from "redux";
import {
  CACHE_API_RESULT,
  API_FAILURE,
  API_REQUEST,
  INVALIDATE_API_RESULT,
} from "../../redux/constants/cacheApiConstants";

type cacheState = {
  [key: string]: {
    data: any;
    status: ApiStatusType;
  };
};

type ApiCacheState = { [key: string]: cacheState };

const initialState: ApiCacheState = {};

export const apiReducer = (
  state = initialState,
  action: AnyAction
): ApiCacheState => {
  const apiStatusMap = {
    API_REQUEST: "LOADING",
    API_FAILURE: "ERROR",
    CACHE_API_RESULT: "SUCCESS",
  };
  switch (action.type) {
    case API_REQUEST:
    case API_FAILURE:
    case CACHE_API_RESULT: {
      const status = (apiStatusMap as any)[action.type];
      return {
        ...state,
        [action.payload.topic]: {
          ...state[action.payload.topic],
          [action.payload.id]: {
            status: status,
            data: action.payload.data,
          },
        },
      };
      break;
    }
    case INVALIDATE_API_RESULT:
      return {
        ...state,
        [action.payload.topic]: {
          ...state[action.payload.topic],
          [action.payload.id]: null,
        },
      };
    default:
      return state;
  }
};
