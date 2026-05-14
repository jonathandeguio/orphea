import { Col, Divider, List, Row, Skeleton, Tooltip, Typography } from "antd";

import React, { useCallback, useEffect, useState } from "react";
import { useSelector } from "react-redux";

import {
  getLanguageLabel,
  getTimeDisplay,
  isDefined,
  timeConverter,
} from "utils/utilities";

import { Resource } from "Apps/explorer/explorer";
import {
  addToFavouritesApi,
  getUpdatedByYou,
  removeFromFavouritesApi,
} from "Apps/explorer/explorer.api";
import { useNavigateHelper } from "Apps/explorer/explorer.hooks";
import { getNodeIcon } from "Apps/explorer/explorer.utils";
import { StarIcon } from "assets/icons/boslerMiscellaneousIcons";
import { BoslerInfiniteScroll } from "components/BoslerInfiniteScroll/BoslerInfiniteScroll.view";
import BoslerLoader from "components/boslerLoader";
import { getDefaultFavicon } from "components/boslerLoader/FavIconLoader";
import { UPDATED_BY_YOU_INFY_DIV_ID } from "./UserActivity.constants";

const { Title, Text } = Typography;

const UpdatedByYou = () => {
  const pageSize = 10;
  const [initLoading, setInitLoading] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMoreDataToShow, setHasMoreDataToShow] = useState(true);
  const [isFav, setIsFav] = useState(new Map());
  const [updatedByYou, setUpdatedByYou] = useState<Resource[]>([]);
  const navigate = useNavigateHelper();

  const { config, loading1 } = useSelector(
    (state) => (state as any).platformConfig
  );

  useEffect(() => {
    document.title = getLanguageLabel("created_by_you");
    let favicon = document.querySelector('link[rel="icon"]') as any;
    favicon.href = getDefaultFavicon();

    return () => {
      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Bosler";
    };
  }, []);

  const resurfaceUpdatedByYou = useCallback(async () => {
    setIsLoading(true);
    const { data } = await getUpdatedByYou(page, pageSize);
    if (data.content.length < pageSize) setHasMoreDataToShow(false);
    setPage((prev) => prev + 1);

    let isFavouriteMap = isFav;
    for (let i = 0; i < data.content.length; i++) {
      const element = data.content[i];
      isFavouriteMap.set(element.id, element.favourite);
    }
    setIsFav(isFavouriteMap);

    setUpdatedByYou((updatedByYou: Resource[]) => {
      const newUpdatedByYou = isDefined(updatedByYou)
        ? (updatedByYou as Resource[])?.concat(data.content)
        : [].concat(data.content);

      return newUpdatedByYou;
    });
    setInitLoading(false);
    setIsLoading(false);
  }, [page, isLoading]);

  useEffect(() => {
    resurfaceUpdatedByYou();
  }, []);

  return (
    <div className="settings-center-block">
      <Title level={3}>{getLanguageLabel("updated_by_you")}</Title>

      <div
        id={UPDATED_BY_YOU_INFY_DIV_ID}
        style={{
          height: "65vh",
          overflowY: "auto",
          border: "1px solid var(--bosler-border-color-default)",
          borderRadius: "5px",
          background: "none",
        }}
        className="--p10"
      >
        <BoslerInfiniteScroll
          pageSize={pageSize}
          isLoading={isLoading}
          next={resurfaceUpdatedByYou}
          hasMore={hasMoreDataToShow}
          loader={<Skeleton avatar paragraph={{ rows: 1 }} active />}
          endMessage={
            <Divider plain>
              All caught up! 🚀 Nothing new for now. Ready for more?
            </Divider>
          }
          scrollableTarget={UPDATED_BY_YOU_INFY_DIV_ID}
        >
          {initLoading ? (
            <BoslerLoader />
          ) : (
            <List
              // pagination={{ position, align }}
              dataSource={updatedByYou}
              renderItem={(item: any, index) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={getNodeIcon(
                      item.type,
                      item.subType,
                      false,
                      16,
                      item.metaData
                    )}
                    title={
                      <Row align={"middle"}>
                        <Col
                          onClick={() => navigate(item.id)}
                          style={{ cursor: "pointer" }}
                        >
                          {item?.name}
                        </Col>
                        <Col
                          onClick={() => {
                            if (isFav.get(item.id)) {
                              setIsFav((prev) => {
                                return new Map(prev.set(item?.id, false));
                              });
                              removeFromFavouritesApi(item.id);
                            } else {
                              setIsFav((prev) => {
                                return new Map(prev.set(item?.id, true));
                              });
                              addToFavouritesApi(item.id);
                            }
                          }}
                        >
                          <StarIcon
                            color={isFav.get(item.id) ? "#ffc940" : "#ffffff"}
                            stroke={isFav.get(item.id) ? "#ffc940" : "#717a94"}
                          />
                        </Col>
                      </Row>
                    }
                    description={
                      item.updatedAt && (
                        <>
                          {getLanguageLabel("youUpdatedIt")}{" "}
                          <Tooltip title={timeConverter(item.updatedAt)}>
                            {getTimeDisplay(item.updatedAt)}
                          </Tooltip>
                        </>
                      )
                    }
                  />
                </List.Item>
              )}
            />
          )}
        </BoslerInfiniteScroll>
      </div>
    </div>
  );
};

export default UpdatedByYou;
