import { useEffect, useLayoutEffect, useState } from "react";

export type Dimensions = {
  width: number;
  height: number;
};

export const useComponentSize = (eleRef: any): Dimensions => {
  const [dimensions, setDimensions] = useState<Dimensions>({
    height: 0,
    width: 0,
  });

  useEffect(() => {
    let ele = eleRef.current;

    function onWindowResize(event: UIEvent): void {
      setDimensions({
        width: ele?.offsetWidth ?? 0,
        height: ele?.offsetHeight ?? 0,
      });
    }

    const resizeObserver = new ResizeObserver((entries) => {
      for (let entry of entries) {
        setDimensions({
          width: entry.contentRect.width,
          height: entry.contentRect.height,
        });
      }
    });
    if (ele) {
      window.addEventListener("resize", onWindowResize);

      resizeObserver.observe(ele);
    }
    return () => {
      window.removeEventListener("resize", onWindowResize);
      if (ele) {
        resizeObserver.unobserve(ele);
      }
      resizeObserver.disconnect();
    };
  }, [eleRef.current]);

  return dimensions;
};
