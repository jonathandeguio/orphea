import { Col, Divider, Skeleton } from "antd";
import React, { useEffect, useState } from "react";
import { autoFormatter } from "utils/AutoFormatter";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { fetchStatsAPI } from "./DatasetStats.api";

interface TProps {
  id: string;
  branch: string | undefined;
  transactionId: string | undefined;
}
const DatasetStats = ({ id, branch, transactionId }: TProps) => {
  const [stats, setStats] = useState();

  useEffect(() => {
    if (isDefined(branch) && isDefined(transactionId)) {
      fetchStatsAPI(id, branch, transactionId).then(({ data }) => {
        setStats(data.datasetStatsModel ? data.datasetStatsModel : false);
      });
    }
  }, [id, branch, transactionId]);

  return (
    <div style={{ display: "inline" }}>
      {stats ? (
        <>
          <div className="--flex-row-start">
            <Col>{autoFormatter((stats as any).size, "bytes")}</Col>
            <Divider type="vertical" />
            <Col>
              {autoFormatter((stats as any).files)}
              &nbsp;
              {getLanguageLabel("files").toLowerCase()}
            </Col>
          </div>
        </>
      ) : (
        <Skeleton active title={false} paragraph={{ rows: 1 }} />
      )}
    </div>
  );
};

export default DatasetStats;
