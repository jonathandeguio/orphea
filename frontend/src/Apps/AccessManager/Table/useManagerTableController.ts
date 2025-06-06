import { useState, useEffect, useCallback } from "react";
import { getAllAccessRequestsAPI } from "../AccessManager.api";
import { IAccessManagerFilters } from "../AccessManager";
import { isDefined, isEmpty } from "utils/utilities";
import useInfiniteScroll from "hooks/useInfiniteScroll";
import useEffectOnlyOnDependencyUpdate from "hooks/useEffectOnlyOnDependencyUpdate";

interface IProps {
  filters: IAccessManagerFilters;
}

export const useManagerTableController = ({ filters }: IProps) => {
  const pageSize = 10;
  const [loading, setLoading] = useState(false);
  const [requests, setRequests] = useState([]);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMoreDataToShow, setHasMoreDataToShow] = useState(true);

  const isListEmpty = requests && isEmpty(requests);

  const resurfaceRequests = useCallback(async () => {
    setIsLoadingMore(true);
    const { data } = await getAllAccessRequestsAPI(page, pageSize, filters);
    if (data.content.length < pageSize) setHasMoreDataToShow(false);
    else setHasMoreDataToShow(true);
    setPage((prev) => prev + 1);
    setRequests((requests) => {
      const newRequests = isDefined(requests)
        ? requests.concat(data.content)
        : [].concat(data.content);

      return newRequests;
    });
    setLoading(false);
    setIsLoadingMore(false);
  }, [page, isLoadingMore, filters]);

  const { lastElementRef } = useInfiniteScroll({
    next: resurfaceRequests,
    isLoading: isLoadingMore,
    hasMore: hasMoreDataToShow,
  });

  const resurfaceRequestsToPage0 = async () => {
    setLoading(true);
    setPage(0);
    const { data } = await getAllAccessRequestsAPI(0, pageSize, filters);
    if (data.content.length < pageSize) setHasMoreDataToShow(false);
    else setHasMoreDataToShow(true);
    setPage((prev) => prev + 1);
    setRequests(data.content);
    setLoading(false);
  };

  useEffect(() => {
    resurfaceRequests();
  }, []);

  useEffectOnlyOnDependencyUpdate(() => {
    resurfaceRequestsToPage0();
  }, [filters]);

  return { requests, loading, lastElementRef, isLoadingMore, isListEmpty };
};
