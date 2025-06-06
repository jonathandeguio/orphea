import { FileExplorerContextMenuHandlerType } from "Apps/explorer/FileExplorer";
import { Skeleton } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { useContextMenuState } from "common/components/ContextMenu";
import NoData from "components/CommonUI/NoData";
import { SimpleTreeViewer } from "components/SimpleTreeViewer";
import { SimpleTreeNodeContextMenu } from "components/SimpleTreeViewer/SimpleTreeContextMenu";
import React, { useEffect, useState } from "react";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { getSourceContentMetaData } from "./Connect.api";
import { TDatabaseTreePages } from "./Connect.types";

interface IProps {
  sourceId: string;
  page: TDatabaseTreePages;
  onSingleClick?: (node: any, parent: any) => void;
}

const DatabaseTreeViewer = ({ sourceId, page, onSingleClick }: IProps) => {
  const [treeData, setTreeData] = useState();
  const [loading, setLoading] = useState(true);
  const [contextMenuNode, setContextMenuNode] = useState<any>();

  const contextMenuStore = useContextMenuState();

  const getTreeData = (sourceId: string) => {
    getSourceContentMetaData(sourceId)
      .then(({ data }) => {
        setTreeData(data);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const contextMenuHandler: FileExplorerContextMenuHandlerType = (
    x: number,
    y: number,
    node: any
  ) => {
    contextMenuStore.displayContextMenu(x, y);

    if (isDefined(node?.id)) {
      setContextMenuNode(node);
    } else {
      setContextMenuNode(undefined);
    }
  };

  useEffect(() => {
    if (sourceId) {
      getTreeData(sourceId);
    }
  }, [sourceId]);

  if (loading) {
    return (
      <div style={{ margin: "0 1rem" }}>
        <Skeleton active />
      </div>
    );
  } else if (!isDefined(treeData)) {
    return (
      <NoData
        heading={getLanguageLabel("folderIsEmpty")}
        icon={<SearchEmptyState />}
      />
    );
  }

  return (
    <div>
      <SimpleTreeViewer
        defaultActiveId={sourceId}
        treeData={treeData}
        dynamicFetching={false}
        openOnSingleClick={true}
        onContextMenu={
          page != "LINK_SOURCE_SELECTION" ? contextMenuHandler : undefined
        }
        onClick={onSingleClick}
        page={page}
        originId={sourceId}
      />
      {page != "LINK_SOURCE_SELECTION" && (
        <SimpleTreeNodeContextMenu
          id={contextMenuNode ? contextMenuNode.id : ""}
          page={page}
          node={contextMenuNode}
          store={contextMenuStore}
        />
      )}
    </div>
  );
};

export default React.memo(DatabaseTreeViewer);
