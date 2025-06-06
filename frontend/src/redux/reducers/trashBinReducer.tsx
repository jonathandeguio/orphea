import {
  TRASH_BIN_REQUEST,
  TRASH_BIN_SUCCESS,
  TRASH_BIN_FAIL,
} from "../constants/trashBinConstants";

export const getTrashBinItemsReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case TRASH_BIN_REQUEST:
      return { trashBinItems: undefined, loading: true, error: false };

    case TRASH_BIN_SUCCESS:
      return { trashBinItems: action.payload, loading: false, error: false };

    case TRASH_BIN_FAIL:
      return {
        trashBinItems: undefined,
        loading: false,
        error: action.payload,
      };

    default:
      return state;
  }
};
