// import { fetchUserDetailsAPI } from "common/components/UserInfo/UserInfo.api";
// import { ThunkAction } from "redux-thunk";
// import { RootState } from "redux/types/store";
// import { isDefined } from "utils/utilities";
// import {
//   CACHE_USER_API_RESULT,
//   USER_API_FAILURE,
//   USER_API_REQUEST,
//   UserApiActionTypes,
// } from "../../redux/constants/usersConstants";

// export const fetchDataOnce =
//   (
//     id: string,
//     rehydrate: boolean = false
//   ): ThunkAction<void, RootState, unknown, UserApiActionTypes> =>
//   async (dispatch, getState) => {
//     if (!rehydrate) {
//       const userInfoCache = getState().cache.user;

//       if (isDefined(userInfoCache[id])) {
//         return userInfoCache[id];
//       }
//     }

//     try {
//       const userdetailsPromise = fetchUserDetailsAPI(id);

//       dispatch({
//         type: USER_API_REQUEST,
//         payload: {
//           id: id,
//           data: userdetailsPromise,
//         },
//       });

//       userdetailsPromise.then(({ data }) => {
//         dispatch({ type: CACHE_USER_API_RESULT, payload: { id, data } });
//       });

//       return {
//         status: "LOADING",
//         data: userdetailsPromise,
//       };
//     } catch (error: any) {
//       dispatch({
//         type: USER_API_FAILURE,
//         payload: { id, data: error.message },
//       });

//       return {
//         status: "ERROR",
//         data: error.message,
//       };
//     }
//   };
