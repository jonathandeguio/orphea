import {
  ADD_FILTERS_FROM_DATASET_SUCCESS,
  FILTERS_DATASET_SUCCESS
} from "../../redux/constants/filtersConstants";
import { RootState, ThunkAppDispatch } from "../types/store";

export const datasetFiltersUpdate =
  (filters: any[] | undefined, resourceId: string) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: FILTERS_DATASET_SUCCESS,
      payload: filters,
      resourceId: resourceId,
    });
  };

export const addFiltersFromDataset =
  (filter: any[] | undefined, resourceId: string) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: ADD_FILTERS_FROM_DATASET_SUCCESS,
      payload: filter,
      resourceId: resourceId,
    });
  };
