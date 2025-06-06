// WARNING
import { createSlice, current, PayloadAction } from "@reduxjs/toolkit";
import { isDefined, notEmpty } from "utils/utilities";

interface fileIndexState {
  parentIndexes: { [key: string]: any };
  fileIndexes: { [key: string]: any };
  projects: { [key: string]: any };
  favourites: any[];
  recycleBin: any[];
  createdByYou: any[];
  updatedByYou: any[];
  recentlyViewed: any[];
}

const initialState: fileIndexState = {
  parentIndexes: {},
  fileIndexes: {},
  projects: {},
  favourites: [],
  recycleBin: [],
  createdByYou: [],
  updatedByYou: [],
  recentlyViewed: [],
};

const fileIndexSlice = createSlice({
  name: "indexObj",
  initialState,
  reducers: {
    updateParentIndexes: (
      state: fileIndexState,
      action: PayloadAction<any>
    ) => {
      state.parentIndexes = { ...state.parentIndexes, ...action.payload };
    },
    updateFileIndexes: (state: fileIndexState, action: PayloadAction<any>) => {
      const curentFileIndexes = current(state.fileIndexes);

      try {
        for (const indexId in action.payload) {
          const indexObj = action.payload[indexId];
          const currentIndex = curentFileIndexes[indexId];
          if (currentIndex) {
            state.fileIndexes[indexId].project = indexObj.project;
            state.fileIndexes[indexId].parent = indexObj.parent;
            state.fileIndexes[indexId].size = indexObj.size;
            state.fileIndexes[indexId].name = indexObj.name;
            state.fileIndexes[indexId].description = indexObj.description;
            state.fileIndexes[indexId].type = indexObj.type;
            state.fileIndexes[indexId].subType = indexObj.subType;
            state.fileIndexes[indexId].status = indexObj.status;
            state.fileIndexes[indexId].createdBy = indexObj.createdBy;
            state.fileIndexes[indexId].createdAt = indexObj.createdAt;
            state.fileIndexes[indexId].updatedBy = indexObj.updatedBy;
            state.fileIndexes[indexId].updatedAt = indexObj.updatedAt;
            state.fileIndexes[indexId].favourite = indexObj.favourite;
            state.fileIndexes[indexId].metaData = indexObj.metaData;
            state.fileIndexes[indexId].children = indexObj.children?.map(
              (child: any) => curentFileIndexes[child.id] ?? child
            );
          } else {
            state.fileIndexes[indexId] = action.payload[indexId];
          }
        }
      } catch (e) {}
    },
    updateFavouriteStatus: (
      state: fileIndexState,
      action: PayloadAction<any>
    ) => {
      state.fileIndexes[action.payload.id].favourite = action.payload.favourite;

      if (notEmpty(state.fileIndexes[action.payload.parent])) {
        state.fileIndexes[action.payload.parent].children = [
          ...state.fileIndexes[action.payload.parent].children?.filter(
            (ele: any) => ele.id !== action.payload.id
          ),
          state.fileIndexes[action.payload.id],
        ];
      }
    },
    updateNameAndDesc: (state: fileIndexState, action: PayloadAction<any>) => {
      state.fileIndexes[action.payload.id].name =
        action.payload.name ?? state.fileIndexes[action.payload.id].name;
      state.fileIndexes[action.payload.id].description =
        action.payload.description ??
        state.fileIndexes[action.payload.id].description;

      if (notEmpty(state.fileIndexes[action.payload.parent])) {
        state.fileIndexes[action.payload.parent].children = [
          ...state.fileIndexes[action.payload.parent].children?.filter(
            (ele: any) => ele.id !== action.payload.id
          ),
          state.fileIndexes[action.payload.id],
        ];
      }
    },
    updateParent: (state: fileIndexState, action: PayloadAction<any>) => {
      try {
        const resource = action.payload;
        const currentFileIndexes = current(state.fileIndexes);

        const currentResource = currentFileIndexes[resource.id];
        const oldParent = currentFileIndexes[currentResource.parent];
        const newParent = currentFileIndexes[resource.parent];

        if (oldParent.id !== newParent.id) {
          const hasSameChildren: boolean =
            newParent.children?.filter((child: any) => child.id === resource.id)
              .length > 0;

          if (!hasSameChildren) {
            state.fileIndexes[oldParent.id].children =
              oldParent.children?.filter(
                (child: any) => child.id !== resource.id
              );
            state.fileIndexes[newParent.id].children = [
              ...(notEmpty(newParent.children) ? newParent.children : []),
              resource,
            ];
            state.fileIndexes[resource.id].parent = resource.parent;
            state.parentIndexes[resource.id] = newParent;
          }
        }
      } catch (e) {
        //
      }
    },
    deleteResource: (state: fileIndexState, action: PayloadAction<any>) => {
      try {
        const resource = action.payload;

        const currentFileIndexes = current(state.fileIndexes);
        const currentRecycleBin = current(state.recycleBin);

        const parent = currentFileIndexes[resource.parent];
        const updatedChildren = parent.children?.filter(
          (child: any) => child.id !== resource.id
        );

        const hasChildren: boolean =
          currentRecycleBin.filter((child: any) => child.id === resource.id)
            .length > 0;
        //updating the list of resource's parent to remove resource
        state.fileIndexes[parent.id].children = updatedChildren;
        // Settings the resource index as undefined and children has undefined
        state.fileIndexes[resource.id].children = undefined;
        if (!hasChildren) {
          state.recycleBin = [...currentRecycleBin, resource];
          state.parentIndexes[resource.id] = undefined;
        }

        // DEV - Wasnt working if did resource file index as undefined
        //state.fileIndexes[resource.id] = undefined;
      } catch (e) {}
    },
    addNewResource: (state: fileIndexState, action: PayloadAction<any>) => {
      const child = action.payload;
      const parentId = child.parent;

      const currentFileIndexes = current(state.fileIndexes);
      const parent = currentFileIndexes[parentId];

      if (isDefined(parent)) {
        const hasSameChildren: boolean =
          parent.children?.filter((c: any) => c.id === child.id).length > 0;

        if (isDefined(parent) && !hasSameChildren) {
          state.fileIndexes[parentId].children = notEmpty(parent.children)
            ? [...parent.children, child]
            : [child];
          state.fileIndexes[child.id] = child;
          state.parentIndexes[child.id] = parent;
        }
      }
    },
    removeFromDeleted: (state: fileIndexState, action: PayloadAction<any>) => {
      state.recycleBin = state.recycleBin.filter(
        (data) => data.id != action.payload.id
      );
    },
    // SPECIAL TYPE OF LISTS
    setRecycleBin: (state: fileIndexState, action: PayloadAction<any[]>) => {
      state.recycleBin = action.payload;
    },
    setFavourites: (state: fileIndexState, action: PayloadAction<any[]>) => {
      state.favourites = action.payload;
    },
    setCreatedByYou: (state: fileIndexState, action: PayloadAction<any[]>) => {
      state.createdByYou = action.payload;
    },
    setUpdatedByYou: (state: fileIndexState, action: PayloadAction<any[]>) => {
      state.updatedByYou = action.payload;
    },
    setRecentlyViewed: (
      state: fileIndexState,
      action: PayloadAction<any[]>
    ) => {
      state.recentlyViewed = action.payload;
    },
    updateRecycleBin: (state: fileIndexState, action: PayloadAction<any[]>) => {
      state.recycleBin = [state.recycleBin, ...action.payload];
    },
    updateFavourites: (state: fileIndexState, action: PayloadAction<any[]>) => {
      state.favourites = [state.favourites, ...action.payload];
    },
    updateCreatedByYou: (
      state: fileIndexState,
      action: PayloadAction<any[]>
    ) => {
      state.createdByYou = [state.createdByYou, ...action.payload];
    },
    updateUpdatedByYou: (
      state: fileIndexState,
      action: PayloadAction<any[]>
    ) => {
      state.updatedByYou = [state.updatedByYou, ...action.payload];
    },
    updateRecentlyViewed: (
      state: fileIndexState,
      action: PayloadAction<any[]>
    ) => {
      state.recentlyViewed = [state.recentlyViewed, ...action.payload];
    },
  },
});

export const {
  updateParentIndexes,
  updateFileIndexes,
  addNewResource,
  deleteResource,
  updateFavouriteStatus,
  removeFromDeleted,
  updateParent,
  updateNameAndDesc,
  setRecycleBin,
  setFavourites,
  setCreatedByYou,
  setUpdatedByYou,
  setRecentlyViewed,
  updateRecycleBin,
  updateFavourites,
  updateCreatedByYou,
  updateUpdatedByYou,
  updateRecentlyViewed,
} = fileIndexSlice.actions;
export default fileIndexSlice.reducer;
