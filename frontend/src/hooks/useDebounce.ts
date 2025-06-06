import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { isDefined } from "utils/utilities";

/**
 * Custom debounce hook
 * @param _value default value
 * @param delay mS you want the debounce for
 * @returns [debouncedValue, setValue, value] debouncedValue, dispatch function to set it and plain value that will be debounced
 */
export function useDebounceState<T>(
  _value?: T,
  delay: number = 800
): [T, Dispatch<SetStateAction<T>>, T] {
  const [value, setValue] = useState<T>(
    isDefined(_value) ? _value : (undefined as any)
  );
  const [debouncedValue, setDebounceValue] = useState<T>(undefined as any);

  useEffect(() => {
    const debounceTimeout = setTimeout(() => setDebounceValue(value), delay);

    return () => {
      clearTimeout(debounceTimeout);
    };
  }, [value]);

  return [debouncedValue, setValue, value];
}
