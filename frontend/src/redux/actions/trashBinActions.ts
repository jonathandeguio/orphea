import axios from "axios";
import { openNotification } from "utils/utilities";

import {
  TRASH_BIN_FAIL,
  TRASH_BIN_REQUEST,
  TRASH_BIN_SUCCESS,
} from "../constants/trashBinConstants";

export const getTrashBinItems =
  (id: string) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: TRASH_BIN_REQUEST });

      const { data } = await axios.get(`/kitab/folder/children/${id}/IN_TRASH`);
      const dataKeys = data.map((object: $TSFixMe) => {
        const key = object.id;
        delete object["id"];
        return { key: key, ...object };
      });

      dispatch({
        type: TRASH_BIN_SUCCESS,
        payload: dataKeys,
      });
      return dataKeys;
    } catch (error) {
      dispatch({
        type: TRASH_BIN_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
      return error;
    }
  };

export const moveToTrash =
  (id: string) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      await axios.delete(`/kitab/${id}/moveToTrash`);

      // message.success("Delete sucessful");
    } catch (error) {
      openNotification(
        "error in handleDelete",
        "There occurred an unexpected error in handleDelete",
        "error"
      );
    }
  };

export const restoreFromTrash =
  (selectedTrashItems: any) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      await Promise.all(
        selectedTrashItems.map(async (element: any) => {
          await axios.get(`/kitab/${element}/restoreFromTrash`);
        })
      );
    } catch (error) {
      openNotification("Error in restoring ", " ", "error");
    }
  };

export const permanentDelete =
  (selectedTrashItems: any) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      await Promise.all(
        selectedTrashItems.map(async (element: any) => {
          await axios.delete(`/kitab/${element}/permanentDelete`);
        })
      );
    } catch (error) {
      openNotification("Error in deleting ", " ", "error");
    }
  };
