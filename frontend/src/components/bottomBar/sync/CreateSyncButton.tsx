import { Alert } from "antd";
import { AddIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { generateKey, getLanguageLabel } from "utils/utilities";
import { TDatasetSync } from "./Sync.types";

export const CreateSyncBtn = ({
  datasetId,
  branch,
  datasetSyncs,
  setDatasetSyncs,
}: {
  datasetId: string;
  branch: string;
  datasetSyncs: TDatasetSync[];
  setDatasetSyncs: any;
}) => {
  const createNewSync = () => {
    const newSync: TDatasetSync = {
      key: generateKey("sync"),
      tableName: "untitled_table",
      id: undefined,
      datasetId: datasetId,
      branch: branch,
      sourceId: undefined,
      autoSyncOnBuild: false,
      createdAt: undefined,
      createdBy: undefined,
      updatedBy: undefined,
      updatedAt: undefined,
      syncIndexes: [],
      isDataMartSyncSpec: false,
      dataMartId: undefined,
    };
    setDatasetSyncs([...datasetSyncs, newSync]);
  };

  return (
    <Alert
      message={getLanguageLabel("databaseSync")}
      description={getLanguageLabel("createSyncViaSelectingSource")}
      type="info"
      showIcon
      action={
        <BoslerButton onClick={() => createNewSync()} icon={<AddIcon />}>
          {getLanguageLabel("create")}
        </BoslerButton>
      }
    />
  );
};
