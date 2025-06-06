import { useCallback } from "react";
import { useDispatch } from "react-redux";
import { autoSaveHandler } from "../../../redux/actions/versionActions";
import { ThunkAppDispatch } from "../../../redux/types/store";

export function useAutoSaveReady() {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const setAutoSaveReady = useCallback(
    (payload: boolean) => {
      dispatch(autoSaveHandler({ isAutoSaveReady: payload }));
    },
    [dispatch]
  );

  return setAutoSaveReady;
}
