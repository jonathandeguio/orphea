import { ThunkAction } from "redux-thunk";
import { RootState } from "redux/types/store";
import { isDefined } from "utils/utilities";
import {
  CACHE_API_RESULT,
  API_FAILURE,
  API_REQUEST,
  ApiActionTypes,
  INVALIDATE_API_RESULT,
} from "../../redux/constants/cacheApiConstants";
import { AxiosResponse } from "axios";

export const invalidateCache = (topic: string, id: string) => {
  return {
    type: INVALIDATE_API_RESULT,
    payload: {
      topic,
      id,
    },
  };
};

export const fetchDataOnce =
  (
    props: any,
    topic: string,
    id: string,
    apiCallback: (props: any) => Promise<AxiosResponse<any, any>>,
    rehydrate: boolean = false
  ): ThunkAction<void, RootState, unknown, ApiActionTypes> =>
  async (dispatch: any, getState: any) => {
    if (!rehydrate) {
      const topicCache = getState().cache[topic];

      if (isDefined(topicCache?.[id])) {
        return topicCache[id];
      }
    }

    try {
      const apidetailsPromise = apiCallback(props);

      dispatch({
        type: API_REQUEST,
        payload: {
          id: id,
          topic,
          data: apidetailsPromise,
        },
      });

      apidetailsPromise.then(({ data }) => {
        dispatch({ type: CACHE_API_RESULT, payload: { id, topic, data } });
      });

      return {
        status: "LOADING",
        data: apidetailsPromise,
      };
    } catch (error: any) {
      dispatch({
        type: API_FAILURE,
        payload: { id, topic, data: error.message },
      });

      return {
        status: "ERROR",
        data: error.message,
      };
    }
  };
