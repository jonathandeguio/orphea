import { Tooltip, message } from "antd";
import { SyncIcon } from "assets/icons/boslerActionIcons";
import BoslerButton, {
  TBoslerButtonIntent,
} from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React, { useState } from "react";
import { performDatasetSyncAPI } from "./Sync.apis";
import { TDatasetSync } from "./Sync.types";

export const SyncNowBtn = ({ datasetSync }: { datasetSync: TDatasetSync }) => {
  const [loading, setLoading] = useState(false);
  const [intent, setIntent] = useState<TBoslerButtonIntent>("none");
  const onClickSync = (e: any) => {
    e.preventDefault();
    e.stopPropagation();
    if (!datasetSync.id) {
      message.error("Sync id not present!");
      return;
    }
    setLoading(true);
    performDatasetSyncAPI(datasetSync.datasetId, datasetSync.id)
      .then(() => {
        setIntent("success");
      })
      .catch(() => {
        setIntent("dangerous");
      })
      .finally(() => {
        setLoading(false);
      });
  };
  return (
    <Tooltip title="Only syncs the datasets and does not updates the config.">
      <BoslerButton
        icon={<SyncIcon />}
        loading={loading}
        onClick={onClickSync}
        intent={intent}
        disabled={!datasetSync.id}
      >
        Sync
      </BoslerButton>
    </Tooltip>
  );
};
