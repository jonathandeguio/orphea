import { AxiosResponse } from "axios";
import { useCallback, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState, ThunkAppDispatch } from "redux/types/store";
import { isDefined } from "utils/utilities";
import {
  fetchDataOnce,
  invalidateCache,
} from "../redux/actions/cacheApiActions";

export const useCreateCacheStore = ({
  apiCallback,
  props,
  topic,
  cacheKey = "",
  resolveCallback,
  errorCallback,
  finallyCallback,
  rehydrateCallback,
  conditional,
  depArr,
}: {
  apiCallback: (props: any) => Promise<AxiosResponse<any, any>>;
  props: any;
  topic: string;
  cacheKey?: string | undefined;
  resolveCallback: (data: any) => void;
  errorCallback: (error: any) => void;
  finallyCallback: () => void;
  rehydrateCallback: () => void;
  conditional: () => boolean;
  depArr: any[];
}) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const topicCache = useSelector((state: RootState) => state.cache[topic]);

  const _rehydrateCallback = useCallback(rehydrateCallback, []);
  const _conditional = useCallback(conditional, []);

  const _resolveCallback = useCallback(resolveCallback, []);
  const _errorCallback = useCallback(errorCallback, []);
  const _finallyCallback = useCallback(finallyCallback, []);

  const cachePromise = useCallback(
    (props: any, cacheKey: string, rehydrate: boolean = false) => {
      return new Promise<any>((resolve, reject) => {
        if (isDefined(props)) {
          dispatch(
            fetchDataOnce(props, topic, cacheKey, apiCallback, rehydrate)
          ).then((cacheData: any) => {
            if (cacheData.status === "LOADING") {
              cacheData.data.then(({ data }: any) => {
                resolve(data);
              });
            } else if (cacheData.status === "SUCCESS") {
              resolve(cacheData.data);
            } else {
              reject(cacheData.data);
            }
          });
        }
      });
    },
    [topicCache]
  );
  const deleteCache = useCallback((cacheKey: string) => {
    dispatch(invalidateCache(topic, cacheKey));
  }, []);

  useEffect(() => {
    if (_conditional() && isDefined(cacheKey) && isDefined(topicCache)) {
      let userInfo = topicCache[cacheKey];

      if (isDefined(userInfo)) {
        if (userInfo.status === "SUCCESS") {
          _resolveCallback(userInfo.data);
          _finallyCallback();
        } else if (userInfo.status === "ERROR") {
          _errorCallback(userInfo.data);
          _finallyCallback();
        }
      }
    }
  }, [
    ...depArr,
    cacheKey,
    topicCache?.[cacheKey],
    _conditional,
    _resolveCallback,
    _finallyCallback,
    _errorCallback,
  ]);

  useEffect(() => {
    if (_conditional() && isDefined(cacheKey)) {
      dispatch(fetchDataOnce(props, topic, cacheKey, apiCallback, false));
    }
  }, [cacheKey]);

  return { cachePromise, deleteCache };
};
