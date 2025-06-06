import UserInfo from "common/components/UserInfo";
import React, { useEffect, useState } from "react";
import { getTimeDisplay } from "utils/utilities";
import { DeleteSyncBtn } from "./DeleteSyncButton";
import styles from "./Sync.module.scss";
import { TDatasetSync } from "./Sync.types";
import { getDataMartDetailAPI } from "./Sync.apis";

export const SyncHeader = ({
  datasetSyncs,
  setDatasetSyncs,
  datasetSync,
}: {
  datasetSyncs: TDatasetSync[];
  setDatasetSyncs: any;
  datasetSync: TDatasetSync;
}) => {
  const [dataMartName, setDataMartName] = useState();
  useEffect(() => {
    if (datasetSync.dataMartId) {
      getDataMartDetailAPI(datasetSync.dataMartId).then((data) => {
        setDataMartName(data.data.name);
      });
    }
  }, []);
  if (datasetSync.isDataMartSyncSpec) {
    return (
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          <div>{dataMartName}</div>
          {datasetSync.updatedBy ? (
            <UserInfo userId={datasetSync.updatedBy} />
          ) : null}
          {datasetSync.updatedAt ? (
            <div>{getTimeDisplay(datasetSync.updatedAt)}</div>
          ) : null}
        </div>
      </div>
    );
  }
  return (
    <div className={styles.header}>
      <div className={styles.headerLeft}>
        <div>{datasetSync.tableName}</div>
        {datasetSync.updatedBy ? (
          <UserInfo userId={datasetSync.updatedBy} />
        ) : null}
        {datasetSync.updatedAt ? (
          <div>{getTimeDisplay(datasetSync.updatedAt)}</div>
        ) : null}
      </div>
      <div className={styles.headerRight}>
        <DeleteSyncBtn
          datasetSyncs={datasetSyncs}
          setDatasetSyncs={setDatasetSyncs}
          datasetSync={datasetSync}
        />
      </div>
    </div>
  );
};
