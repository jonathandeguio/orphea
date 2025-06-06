import { useEffect, useRef } from "react";

const useEffectOnlyOnDependencyUpdate = (
  callback: any,
  dependencies: any[]
) => {
  const dependenciesRef = useRef(dependencies);

  useEffect(() => {
    const dependenciesChanged = dependencies.some(
      (dep, index) => dep !== dependenciesRef.current[index]
    );

    if (dependenciesChanged) {
      callback();
      dependenciesRef.current = dependencies;
    }
  }, [callback, dependencies]);
};

export default useEffectOnlyOnDependencyUpdate;
