import { useCallback, useRef } from "react";

interface IProps {
  isLoading: boolean;
  hasMore: boolean;
  next: () => void;
}

const useInfiniteScroll = ({ isLoading, hasMore, next }: IProps) => {
  const observer = useRef<IntersectionObserver | null>(null);

  const lastElementRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (isLoading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          next();
        }
      });
      if (node) observer.current.observe(node);
    },
    [isLoading, observer, hasMore]
  );

  return { lastElementRef };
};

export default useInfiniteScroll;
