import { cacheCallbacks } from "cache/cache";
import { useCreateCacheStore } from "cache/createCacheStore";
import { fetchUserDetailsAPI2 } from "common/components/UserInfo/UserInfo.api";
import { useCallback } from "react";
export const useUserHook = (
  id?: string | undefined,
  callbacks?: cacheCallbacks,
  rehydrateCallback?: () => boolean,
  conditional?: () => boolean,
  depArr: any[] = []
) => {
  const reCallback = useCallback(() => false, []);
  const conCallback = useCallback(() => true, []);

  const resCallback = useCallback(() => {}, []);
  const errCallback = useCallback(() => {}, []);
  const finCallback = useCallback(() => {}, []);

  const topic = "users";
  const { cachePromise: thunkPromise } = useCreateCacheStore({
    props: {
      id: id,
    },
    apiCallback: fetchUserDetailsAPI2,
    topic: topic,
    cacheKey: id,
    resolveCallback: callbacks?.resolveCallback ?? resCallback,
    errorCallback: callbacks?.errorCallback ?? errCallback,
    finallyCallback: callbacks?.finallyCallback ?? finCallback,
    rehydrateCallback: rehydrateCallback ?? reCallback,
    conditional: conditional ?? conCallback,
    depArr: [],
  });

  return (id: string) => thunkPromise({ id }, id);
};
