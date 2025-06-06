import { Col, Divider, Row, Typography } from "antd";
import React, { useEffect, useRef, useState } from "react";
import { getLanguageLabel, isEmpty } from "utils/utilities";

import { getRecentlyViewed } from "Apps/explorer/explorer.api";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";
import NoData from "components/CommonUI/NoData";
import BoslerLoader from "components/boslerLoader";
import { getGlobalSearchResults } from "layouts/GlobalSearchFilter/GlobalSearchFIlter.api";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { getAllUserDetails } from "../../redux/actions/userActions";
import "../GlobalSearchFilter/GlobalFilters.scss";
import {
  IGlobalResourceSearchFilters,
  SortOrder,
} from "../GlobalSearchFilter/HeaderSearch.types";
import {
  bolserApplications,
  removeDuplicateResources,
} from "./GlobalSearch.utils";
import { CGlobalFilters } from "./GlobalSearch.constants";
import HeaderSearchFooter from "./GlobalSearchFooter";
import ResourceList from "./GlobalSearchResourceList";
import GlobalSearchHeader from "./GlobalSearchHeader";
import { IResource } from "global";

const HeaderSearch = ({
  setIsHeaderSearchModalOpen,
}: {
  setIsHeaderSearchModalOpen: any;
}) => {
  const dispatch = useDispatch<$TSFixMe>();

  const timeoutRef = useRef<NodeJS.Timeout | null>(null);
  const { user } = useSelector((state: RootState) => state.userDetails);
  const { allusers } = useSelector((state: RootState) => state.allUserDetails);

  const [page, setPage] = useState<number>(0);
  const [resources, setResources] = useState<IResource[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [totalResources, setTotalResources] = useState<number>(0);
  const [totalPage, setTotalPage] = useState<number | null>(null);
  const [hasMoreDataToShow, setHasMoreDataToShow] = useState(true);
  const [recentlyViewed, setRecetlyViewed] = useState<IResource[]>([]);
  const [applications, setApplications] = useState<string[]>([]);
  const [sortOrder, setSortOrder] = useState<SortOrder>(SortOrder.DESCENDING);
  const [globalSearchFilters, setGlobalSearchFilters] =
    useState<IGlobalResourceSearchFilters>({
      ...CGlobalFilters,
      createdBy: [user.id],
    });

  const getFilteredResources = (): void => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    const getFilteredResourcesAPIV2 = () => {
      if (!hasMoreDataToShow) return;

      setIsLoading(true);
      getGlobalSearchResults(
        globalSearchFilters,
        page,
        15,
        sortOrder,
        "updatedAt"
      )
        .then(({ data }) => {
          setTotalResources(data.totalElements);
          setTotalPage(data.totalPages);

          if (data.content.length > 0) {
            if (resources.length > 0 && page !== 0) {
              setResources((prev) =>
                removeDuplicateResources(prev, data.content)
              );
            } else {
              setRecetlyViewed([]);
              setResources(data.content);
            }
          } else {
            setHasMoreDataToShow(false);
            setRecetlyViewed([]);
            setResources([]);
          }
        })
        .finally(() => {
          setIsLoading(false);
        });
    };
    timeoutRef.current = setTimeout(getFilteredResourcesAPIV2, 300);
    const newApplications = bolserApplications?.filter((application: string) =>
      application
        .toLowerCase()
        .includes(globalSearchFilters?.searchText?.toLowerCase()!)
    );
    setApplications(newApplications);
  };

  useEffect(() => {
    setIsLoading(true);
    if (!isEmpty(globalSearchFilters.searchText)) {
      getFilteredResources();
    } else {
      setTotalPage(null);
      setApplications(bolserApplications?.filter((_, index) => index < 10));
      setResources([]);

      getRecentlyViewed()
        .then((res) => {
          setRecetlyViewed(res.data);
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
    };
  }, [globalSearchFilters, sortOrder, page]);
  useEffect(() => {
    dispatch(getAllUserDetails());
  }, []);

  return (
    <div className="global-search-wrapper">
      <GlobalSearchHeader
        setHasMoreDataToShow={setHasMoreDataToShow}
        setResources={setResources}
        setSortOrder={setSortOrder}
        globalSearchFilters={globalSearchFilters}
        setGlobalSearchFilters={setGlobalSearchFilters}
        sortOrder={sortOrder}
        setPage={setPage}
        setIsLoading={setIsLoading}
        setTotalPage={setTotalPage}
      />
      <Divider />

      <div className="options-container">
        <div className="resource-list-info">
          {!isEmpty(recentlyViewed) && (
            <BoslerTypography variant="para">
              {getLanguageLabel("recentlyVisitedByYou")}
            </BoslerTypography>
          )}
          {!isEmpty(resources) && (
            <>
              <BoslerTypography variant="para">
                {getLanguageLabel("searchResult")}
              </BoslerTypography>
              <BoslerTypography variant="para">
                {`${getLanguageLabel("showing")} ${
                  !isEmpty(resources) ? (resources as IResource[])?.length : 0
                } ${getLanguageLabel("of")} ${totalResources}`}
              </BoslerTypography>
            </>
          )}
        </div>
        <Row
          className="resource-list-header"
          justify="space-between"
          align="middle"
        >
          <Col>
            <Row align="middle">
              {" "}
              <Typography.Text type="secondary">
                {getLanguageLabel("name")}
              </Typography.Text>
            </Row>
          </Col>
          <Col>
            <Row align="middle">
              <Typography.Text type="secondary">
                {getLanguageLabel("info")}
              </Typography.Text>
            </Row>
          </Col>
        </Row>
        {isLoading && isEmpty(resources) ? (
          <BoslerLoader />
        ) : (
          <div className="resource-list-container">
            {!isEmpty(recentlyViewed) && (
              <ResourceList
                allResources={recentlyViewed}
                setResources={setRecetlyViewed}
                isLoading={isLoading}
                hasMoreDataToShow={true}
                setPage={setPage}
                setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen}
                allusers={allusers}
                totalpage={totalPage}
              />
            )}

            {isEmpty(resources) && isEmpty(recentlyViewed) ? (
              <div className="header-search-nodata">
                <NoData
                  heading={getLanguageLabel("noResourceFound")}
                  icon={<SearchEmptyState />}
                />
              </div>
            ) : (
              <ResourceList
                allResources={resources}
                setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen}
                isLoading={isLoading}
                setResources={setResources}
                hasMoreDataToShow={hasMoreDataToShow}
                setPage={setPage}
                allusers={allusers}
                totalpage={totalPage}
              />
            )}
          </div>
        )}
      </div>

      <HeaderSearchFooter
        applications={applications}
        setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen}
      />
    </div>
  );
};

export default HeaderSearch;
