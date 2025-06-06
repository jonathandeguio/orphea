import { deleteSyncAPI } from "./Sync.apis";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import React, { useEffect, useState } from "react";
import { message } from "antd";
import { TDatasetSync } from "./Sync.types";
export const DeleteSyncBtn = ({
  datasetSyncs,
  setDatasetSyncs,
  datasetSync,
}: {
  datasetSyncs: TDatasetSync[];
  setDatasetSyncs: any;
  datasetSync: TDatasetSync;
}) => {
  const [loading, setLoading] = useState(false);
  const onClickDeleteSync = () => {
    if (!datasetSync.id) {
      message.error("Sync id not present!");
      return;
    }
    setLoading(true);
    deleteSyncAPI(datasetSync.datasetId, datasetSync.id)
      .then(() => {
        setDatasetSyncs(
          datasetSyncs.filter((sync) => sync.id !== datasetSync.id)
        );
      })
      .finally(() => {
        setLoading(false);
      });
  };
  return (
    <BoslerButton
      icon={<TrashIcon />}
      loading={loading}
      onClick={onClickDeleteSync}
      intent={"dangerous"}
      disabled={!datasetSync.id}
      icononly
      minimal
    />
  );
};
