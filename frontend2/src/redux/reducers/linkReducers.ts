import {
  LINK_CREATE_FAIL,
  LINK_CREATE_REQUEST,
  LINK_CREATE_SUCCESS,
  LINK_DELETE_FAIL,
  LINK_DELETE_REQUEST,
  LINK_DELETE_SUCCESS,
  LINK_LIST_FAIL,
  LINK_LIST_REQUEST,
  LINK_LIST_SUCCESS,
} from "../constants/linkConstants";

export const linkListReducer = (state : any = { link: [] }, action: any) => {
  switch (action.type) {
    case LINK_LIST_REQUEST:
      return { linksLoading: true, links: [] };

    case LINK_LIST_SUCCESS:
      return {
        linksLoading: false,
        links: action.payload,
      };
    case LINK_LIST_FAIL:
      return { linksLoading: false, error: action.payload };
    default:
      return state;
  }
};

export const linkDeleteReducer = (state : any = { link: [] }, action: any) => {
  switch (action.type) {
    case LINK_DELETE_REQUEST:
      return { linksLoading: true, links: [] };

    case LINK_DELETE_SUCCESS:
      return {
        linksLoading: false,
        links: action.payload,
      };
    case LINK_DELETE_FAIL:
      return { linksLoading: false, error: action.payload };
    default:
      return state;
  }
};

export const linkCreateReducer = (state = {}, action: any) => {
  switch (action.type) {
    case LINK_CREATE_REQUEST:
      return { linksLoading: true };

    case LINK_CREATE_SUCCESS:
      return { linksLoading: false, success: true, links: action.payload };

    case LINK_CREATE_FAIL:
      return { linksLoading: false, error: action.payload };

    default:
      return state;
  }
};
