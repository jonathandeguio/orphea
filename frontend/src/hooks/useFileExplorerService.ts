import { Resource } from "Apps/explorer/explorer";
import {
  addToFavouritesApi,
  deleteResourceApi,
  getCreatedByYou,
  getFavourites,
  getProjectStructure,
  getRecentlyViewed,
  getRecycleBin,
  getResourceApi,
  getResourceApi2,
  getUpdatedByYou,
  removeFromFavouritesApi,
  restoreResourceApi,
} from "Apps/explorer/explorer.api";
import { useNavigateHelper } from "Apps/explorer/explorer.hooks";
import { buildFileIndex, buildParentIndex } from "Apps/explorer/explorer.utils";
import { cacheCallbacks } from "cache/cache";
import { useCreateCacheStore } from "cache/createCacheStore";
import { User } from "global";
import { useCallback, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router";
import { useSearchParams } from "react-router-dom";
import { isDefined, notEmpty, openNotification } from "utils/utilities";
import {
  addNewResource,
  deleteResource,
  removeFromDeleted,
  setCreatedByYou,
  setFavourites,
  setRecentlyViewed,
  setRecycleBin,
  setUpdatedByYou,
  updateFavouriteStatus,
  updateFileIndexes,
  updateParentIndexes,
} from "../redux/fileIndexSlice";
import store from "../redux/store";
import { RootState } from "../redux/types/store";

const { validate } = require("uuid");

const emptyUser = () => {
  const user: User = {
    id: "string",
    name: "string",
    username: "string",
    givenName: "string",
    familyName: "string",
    location: "string",
    profileImage: "any",
    email: "string",
    preferences: "any",
    provider: "any",
    providerId: "any",
    ssoAttributes: "any",
    lastLoginAt: 0,
    createdAt: 0,
    updatedAt: 0,
    createdBy: "any",
    updatedBy: "any",
  };
  return user;
};
const emptyResource = (id: string) => {
  const resource: Resource = {
    id: id,
    workspace: "any",
    size: 0,
    name: "string",
    description: null,
    type: "string",
    subType: "string",
    status: "string",
    children: [],
    createdBy: emptyUser(),
    createdAt: 0,
    updatedBy: emptyUser(),
    updatedAt: 0,
  };
  return resource;
};

export const useResourceHook = (
  id?: string | undefined,
  callbacks?: cacheCallbacks,
  rehydrateCallback?: () => boolean,
  conditional?: () => boolean,
  depArr: any[] = []
) => {
  const topic = "files";

  const reCallback = useCallback(() => false, []);
  const conCallback = useCallback(() => true, []);

  const resCallback = useCallback(() => {}, []);
  const errCallback = useCallback(() => {}, []);
  const finCallback = useCallback(() => {}, []);

  const { cachePromise, deleteCache } = useCreateCacheStore({
    props: {
      id: id,
    },
    apiCallback: getResourceApi2,
    topic: topic,
    cacheKey: id,
    resolveCallback: callbacks?.resolveCallback ?? resCallback,
    errorCallback: callbacks?.errorCallback ?? errCallback,
    finallyCallback: callbacks?.finallyCallback ?? finCallback,
    rehydrateCallback: rehydrateCallback ?? reCallback,
    conditional: conditional ?? conCallback,
    depArr: [],
  });

  const cacheDataPromise = useCallback(
    (id: string, rehydrate: boolean = false) =>
      cachePromise({ id }, id, rehydrate),
    [id]
  );

  return { cacheDataPromise, deleteCache };
};

const useResourcePromise = (
  id?: string | undefined,
  callbacks?: cacheCallbacks,
  depArr: any[] = []
) => {
  const dispatch = useDispatch();
  const state = store.getState();
  const fileIndexes = useSelector(
    (state: RootState) => state.indexes.fileIndexes
  );
  const { cacheDataPromise: getFileIndex } = useResourceHook();

  const getResourcePromise = useCallback(
    (rId: string) =>
      new Promise<any>((resolve, reject) => {
        try {
          if (notEmpty(fileIndexes[rId])) {
            resolve(fileIndexes[rId]);
          } else {
            if (notEmpty(rId) && validate(rId)) {
              getFileIndex(rId)
                .then((data) => {
                  try {
                    dispatch(addNewResource(data));
                  } catch (e) {
                    console.error(e);
                  }

                  buildParentIndex(data).then((indexes) => {
                    dispatch(updateParentIndexes(indexes));
                  });

                  buildFileIndex(data).then((indexes) => {
                    dispatch(updateFileIndexes(indexes));
                  });

                  resolve(data);
                })
                .catch(({ response }) => {
                  reject(response);
                });
            } else {
              reject(new Error("Invalid ID"));
            }
          }
        } catch (err) {
          reject(err);
        }
      }),
    [fileIndexes]
  );

  useEffect(() => {
    if (isDefined(id)) {
      getResourcePromise(id)
        .then((data: Resource) => {
          callbacks?.resolveCallback?.(data);
        })
        .catch((error) => {
          callbacks?.errorCallback?.(error);
        })
        .finally(() => {
          callbacks?.finallyCallback?.();
        });
    }
  }, [...depArr, id, state?.indexes?.fileIndexes]);

  return getResourcePromise;
};

export const useFileExplorerService = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const navigator = useNavigateHelper();
  const [searchParams, _] = useSearchParams();
  const queryActiveId = searchParams.get("activeId");

  const projectId = useSelector(
    (state: RootState) => state.fileExplorer.projectId
  );
  const getFileIndex = useResourcePromise();
  const { deleteCache } = useResourceHook();

  // RESOURCE SERVICE
  const createResource = (id: string) => {};
  const updateResource = (id: string) => {};
  const fetchResource = (id: string) => {
    return getResourceApi(id)
      .then(({ data }): any => {
        dispatch(addNewResource(data));
        buildParentIndex(data).then((indexes) => {
          dispatch(updateParentIndexes(indexes));
        });
        buildFileIndex(data).then((indexes) => {
          dispatch(updateFileIndexes(indexes));
        });

        return data;
      })
      .catch(({ response }) => {
        // openNotification(
        //   response.data.error,
        //   response.data.description,
        //   "error"
        // );
        navigate(`/portal`);
      });
  };
  const fetchResourceTree = (id: string) => {
    return getProjectStructure(id)
      .then(({ data }): any => {
        buildParentIndex(data).then((indexes) => {
          dispatch(updateParentIndexes(indexes));
        });
        buildFileIndex(data).then((indexes) => {
          dispatch(updateFileIndexes(indexes));
        });

        return data;
      })
      .catch(({ response }) => {
        console.log("WE ARE GETTING FROM HERE");
        openNotification(
          response.data.error,
          response.data.description,
          "error"
        );
        navigate(`/portal`);
      });
  };

  // FAVOURITES SERVICES
  const addToFavourites = (id: string) => {
    addToFavouritesApi(id).then((data) => {
      getFileIndex(id).then((data) => {
        dispatch(updateFavouriteStatus({ ...data, favourite: true }));
      });
    });
  };
  const removeFromFavourites = (id: string) => {
    removeFromFavouritesApi(id).then((data) => {
      getFileIndex(id).then((data) => {
        dispatch(updateFavouriteStatus({ ...data, favourite: false }));
      });
    });
  };
  const fetchFavourite = (id: string) => {};

  // RECYCLE BIN SERVICE
  const addToRecycleBin = (id: string) => {
    deleteResourceApi(id)
      .then(({ data }) => {
        console.log("DETELET", data.parent);
        dispatch(deleteResource(data));
        deleteCache(data.parent);

        if (id === queryActiveId && projectId) {
          navigator(projectId);
        }
        // openNotification(
        //   getLanguageLabel("delete"),
        //   data.name + " is deleted. Go to trash to restore it",
        //   "info"
        // );
      })
      .catch(({ response }) => {
        openNotification("Operation Failed !", response.data.error, "error");
      });
  };
  const removeFromRecycleBin = (id: string) => {
    restoreResourceApi(id)
      .then(({ data }) => {
        dispatch(addNewResource(data));
        dispatch(removeFromDeleted(data));
        // openNotification("Restored", "", "info");
      })
      .catch(({ response }) => {
        // openNotification(response.data.error, "", "error");
      });
  };

  // SPECIAL LISTS
  const rehydrateFavouriteList = () => {
    getFavourites().then(({ data }) => dispatch(setFavourites(data)));
  };
  const rehydrateRecycleBin = (id: string) => {
    getRecycleBin(id).then(({ data }) => dispatch(setRecycleBin(data)));
  };
  const rehydrateCreatedByYouList = () => {
    getCreatedByYou(0, 10).then(({ data }) => dispatch(setCreatedByYou(data)));
  };
  const rehydrateUpdatedByYouList = () => {
    getUpdatedByYou(0, 10).then(({ data }) => dispatch(setUpdatedByYou(data)));
  };
  const rehydrateRecentlyViewedList = () => {
    getRecentlyViewed().then(({ data }) => dispatch(setRecentlyViewed(data)));
  };

  return {
    getFileIndex,
    createResource,
    updateResource,
    fetchResource,
    fetchResourceTree,
    addToFavourites,
    removeFromFavourites,
    fetchFavourite,
    addToRecycleBin,
    removeFromRecycleBin,
    rehydrateFavouriteList,
    rehydrateRecycleBin,
    rehydrateCreatedByYouList,
    rehydrateUpdatedByYouList,
    rehydrateRecentlyViewedList,
  };
};
