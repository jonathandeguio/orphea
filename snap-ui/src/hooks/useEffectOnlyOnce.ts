import { useEffect, useRef } from "react";

// If condition condition is provided, then the useEffect will only fire onces when the condition is satisfied
export function useOnlyOnce(fn: () => void, condition?: () => boolean) {
  const _firstFlag = useRef<boolean>(true);

  useEffect(() => {
    if (_firstFlag.current && (!condition || condition())) {
      _firstFlag.current = false;
      fn();
    }
  }, [condition, fn]);
}
