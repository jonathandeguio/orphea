import { Resource } from "Apps/explorer/explorer";
import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import React, { useEffect } from "react";
import { useParams } from "react-router";
import TreePopoverTable from "./TreePopoverTable";

export const getPopoverTitle = (node: Resource) => {
  return <>Details</>;
};

export const getPopoverContent = (
  originId: string | undefined,
  node: Resource,
  popoverCache: any,
  setPopoverCache: any
) => {
  const { id } = useParams();

  if (!id) {
    return <></>;
  }

  if (node.subType == ResourceSubTypeEnum.TABLE_CHART && originId) {
    return (
      <TreePopoverTable
        popoverCache={popoverCache}
        setPopoverCache={setPopoverCache}
        sourceId={originId}
        tableNode={node}
      />
    );
  }
  return <></>;
};
