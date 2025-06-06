import { useNavigateHelper, usePath } from "Apps/explorer/explorer.hooks";
import { getNodeIcon } from "Apps/explorer/explorer.utils";
import { Col, Row, Typography } from "antd";
import { RefreshIcon } from "assets/icons/boslerActionIcons";
import { UserIcon } from "assets/icons/boslerInterfaceIcons";
import { useFileExplorerService } from "hooks/useFileExplorerService";
import useInfiniteScroll from "hooks/useInfiniteScroll";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { IsUUID, getLanguageLabel, getTimeDisplay } from "utils/utilities";
import {
  IResourceCard,
  IResourceList,
} from "layouts/GlobalSearchFilter/HeaderSearch.types";
import BoslerLoader from "components/boslerLoader";
import { getPathApi } from "Apps/Kepler/chart/charts.api";
import { IResource } from "global";

const ResourceList: React.FC<IResourceList> = ({
  allResources,
  isLoading,
  hasMoreDataToShow,
  setPage,
  setResources,
  setIsHeaderSearchModalOpen,
  allusers,
  totalpage,
}) => {
  const navigateResource = useNavigateHelper();

  const { lastElementRef: lastResourceElementRef } = useInfiniteScroll({
    isLoading: isLoading,
    hasMore: hasMoreDataToShow,
    next: () =>
      setPage((prev) => (totalpage && totalpage > prev + 1 ? prev + 1 : prev)),
  });

  const handleResourceClick = (e: any) => {
    setIsHeaderSearchModalOpen(false);
    if (IsUUID(e)) navigateResource(e);
    setResources([]);
  };

  return (
    <>
      {allResources.map((e: IResource, index: number) => (
        <ResourceCard
          lastResourceElementRef={
            index === allResources.length - 1 ? lastResourceElementRef : null
          }
          handleResourceClick={handleResourceClick}
          setIsHeaderSearchModalOpen={setIsHeaderSearchModalOpen}
          resource={e}
          allusers={allusers}
        />
      ))}
      {isLoading ? <BoslerLoader /> : null}
    </>
  );
};

const ResourceCard: React.FC<IResourceCard> = ({
  resource,
  lastResourceElementRef,
  handleResourceClick,
  setIsHeaderSearchModalOpen,
  allusers,
}) => {
  return (
    <Col ref={lastResourceElementRef} span={24} key={resource.id}>
      <div
        className="show-differentiator-on-hover resource-card"
        onClick={() => handleResourceClick(resource.id)}
      >
        <div className="resource-name-and-icon-container">
          <div className="text-and-icon-center">
            {getNodeIcon(
              resource.type,
              resource.subType,
              false,
              22,
              resource.metaData
            )}
          </div>
          <div className="resource-name-path-description-container">
            <Typography.Text>{resource.name}</Typography.Text>

            <WritePath
              closeModal={() => setIsHeaderSearchModalOpen(false)}
              resourceId={resource.id}
            />
            {resource?.description?.length > 0 ? (
              <Typography.Text type="secondary">
                {resource.description}
              </Typography.Text>
            ) : null}
          </div>
        </div>
        <div>
          {resource.updatedAt ? (
            <div className="username-date-view">
              <RefreshIcon />
              {
                allusers?.find((ele: any) => ele.id === resource.updatedBy)
                  ?.name
              }{" "}
              {getTimeDisplay(resource.updatedAt)}
            </div>
          ) : null}{" "}
          <div className="username-date-view">
            <UserIcon />
            {
              allusers?.find((ele: any) => ele.id === resource.createdBy)?.name
            }{" "}
            {getTimeDisplay(resource.createdAt)}
          </div>
        </div>
      </div>
    </Col>
  );
};

const WritePath = ({
  resourceId,
  closeModal,
}: {
  resourceId: string;
  closeModal?: () => void;
}) => {
  const [completePath, setCompletePath] = useState<any>([]);
  const navigate = useNavigate();

  useEffect(() => {
    getPathApi(resourceId).then((res) => {
      if (Array.isArray(res.data)) {
        setCompletePath(res.data);
      }
    });
  }, [resourceId]);

  if (completePath.length == 0) {
    return <></>;
  }
  return (
    <div>
      {completePath?.map((path: any, index: number) => (
        <span
          className="path-span"
          key={path.id}
          onClick={(e) => {
            e.stopPropagation();
            closeModal && closeModal();
            navigate(`/portal/kitab/folder/${path.id}?activeId=${path.id}`);
          }}
        >
          <Typography.Text type="secondary">
            {index === completePath.length - 1 ? "" : path.name + "/"}
          </Typography.Text>
        </span>
      ))}
    </div>
  );
};
export default ResourceList;
