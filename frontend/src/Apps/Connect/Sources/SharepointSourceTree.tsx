import { Skeleton } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import NoData from "components/CommonUI/NoData";
import { SimpleTreeViewer } from "components/SimpleTreeViewer";
import React, { useCallback, useEffect, useState } from "react";
import { isDefined } from "utils/utilities";
import { getSharepointFolderChilderAPI } from "../Connect.api";

export const SharepointSourceTree = ({ source }: { source: any }) => {
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isError, setIsError] = useState<string | false>(false);
  const [treeData, setTreeData] = useState<any>();

  const getFolderChildren = useCallback(
    (folderId: string) => {
      return getSharepointFolderChilderAPI(source.id, folderId);
    },
    [source]
  );

  useEffect(() => {
    if (source.type === "SHAREPOINT") {
      setIsError(false);
      setIsLoading(true);
      getSharepointFolderChilderAPI(source.id, "root")
        .then(({ data }) => {
          console.log("data", data);
          if (data["error"]) {
            setIsError(data["error"]["message"]);
          } else {
            const root = {
              id: "root",
              type: "FOLDER",
              name: "Sharepoint Root (/)",
              children: data.value.map((node: any) => ({
                ...node,
                type: node.hasOwnProperty("folder")
                  ? "FOLDER"
                  : node.file.mimeType,
                subType: node.hasOwnProperty("folder")
                  ? "FOLDER"
                  : node.file.mimeType,
              })),
            };

            setTreeData(root);
          }
        })
        .catch((error) => {
          setIsError(error.message);
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, []);
  if (isError) {
    return <NoData heading={isError} icon={<SearchEmptyState />} />;
  }

  if (isLoading) return <Skeleton />;

  if (!isDefined(treeData))
    return <NoData heading={"No data"} icon={<SearchEmptyState />} />;

  return (
    <SimpleTreeViewer
      treeData={treeData}
      dynamicFetching={getFolderChildren}
      openOnSingleClick={true}
      page={"SHAREPOINT_CONNECTOR"}
      defaultActiveId={"root"}
      originId="root"
    />
  );
};
