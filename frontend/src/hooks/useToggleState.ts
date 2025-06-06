import { useState } from "react";

type UseToggleStateReturn = [boolean, () => void, () => void, () => void];

/**
 * A wrapper over useState for boolean only states which can be set on or off.
 * const [isPopupActive, showPopup, hidePopup] = useToggleState(false);
 */
export const useToggleState = (initialState: boolean): UseToggleStateReturn => {
  const [state, setState] = useState(initialState);
  const setTrue = () => setState(true);
  const setFalse = () => setState(false);
  const toggle = () => setState((prevState) => !prevState);
  return [state, setTrue, setFalse, toggle];
};
