import { useEffect } from "react";
import { isDefined } from "utils/utilities";

export const useOutsideClickHandler = (
  onOutsideClick: (event: MouseEvent | KeyboardEvent) => void,
  excludedComponentRefs: any[]
) => {
  const handleClickOutside = (event: MouseEvent) => {
    let isInside = false;
    excludedComponentRefs.forEach((ref) => {
      if (isDefined(ref?.current)) {
        if (ref.current.contains(event.target as Node)) {
          isInside = true;
        }
      }
    });

    if (!isInside) {
      onOutsideClick(event);
    }
  };

  const handleEscapeKeyDown = (event: KeyboardEvent) => {
    if (event.key === "Escape") {
      onOutsideClick(event);
    }
  };

  useEffect(() => {
    document.addEventListener("keydown", handleEscapeKeyDown);
    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("keydown", handleEscapeKeyDown);
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [...excludedComponentRefs]);
};
