import { FileExplorerContextMenuHandlerType } from "Apps/explorer/FileExplorer";
import { Skeleton } from "antd";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import { useContextMenuState } from "common/components/ContextMenu";
import NoData from "components/CommonUI/NoData";
import { SimpleTreeViewer } from "components/SimpleTreeViewer";
import { SimpleTreeNodeContextMenu } from "components/SimpleTreeViewer/SimpleTreeContextMenu";
import React, { useEffect, useState } from "react";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { getDataMartDatabaseContentMetaData } from "./DataMart.api";

interface IProps {
  dataMartId: string;
  onSingleClick?: (node: any, parent: any) => void;
}

const DataMartTreeViewer = ({ dataMartId, onSingleClick }: IProps) => {
  const [treeData, setTreeData] = useState();
  const [loading, setLoading] = useState(true);
  const [contextMenuNode, setContextMenuNode] = useState<any>();

  const contextMenuStore = useContextMenuState();

  const getTreeData = (dataMartId: string) => {
    getDataMartDatabaseContentMetaData(dataMartId)
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
    if (dataMartId) {
      getTreeData(dataMartId);
    }
  }, [dataMartId]);

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
        defaultActiveId={dataMartId}
        treeData={treeData}
        dynamicFetching={false}
        openOnSingleClick={true}
        onContextMenu={contextMenuHandler}
        onClick={onSingleClick}
        page={"DATA_MART"}
        originId={dataMartId}
      />
      <SimpleTreeNodeContextMenu
        id={contextMenuNode ? contextMenuNode.id : ""}
        page={"DATA_MART"}
        node={contextMenuNode}
        store={contextMenuStore}
      />
    </div>
  );
};

export default React.memo(DataMartTreeViewer);
