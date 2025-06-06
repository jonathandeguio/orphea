import { previewDatasetHistoryAPI } from "Apps/Dataset/Dataset.api";
import { Col, Divider, Row, Tooltip, Typography } from "antd";
import {
  SearchEmptyState,
  WarningState,
} from "assets/Illustrations/EmptyState";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import UserInfo from "common/components/UserInfo";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import NoData from "components/CommonUI/NoData";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";
import {
  copyToClipboard,
  decodeFromBase64,
  getLanguageLabel,
  getTimeDisplay,
} from "utils/utilities";
import { IPreviewDatasetModel } from "./DatasetSql.types";
const { Text } = Typography;
interface IProps {
  datasetId: string;
  branch: string;
}

const HistoryItem = ({ item }: { item: IPreviewDatasetModel }) => {

  const [tooltipTitle, setTooltipTitle] = useState(
    getLanguageLabel("clickToCopyIntoClipboard")
  );
  
  return (
    <div style={{ padding: "5px" }}>
      <div>
        <Row justify={"space-around"}>
          <Col span={22}>
            <Text>
              <pre>{decodeFromBase64(item.query)}</pre>
            </Text>
          </Col>
          <Col style={{ paddingTop: "15px" }} span={2}>
          <Tooltip title={tooltipTitle} style={{ display: "inline" }}>
            <BoslerButton
              intent="action"
              icon={<CopyIcon />}
              onClick={() => {
                copyToClipboard(decodeFromBase64(item.query), setTooltipTitle);
              }}
              icononly
              minimal
            >
              Copy
            </BoslerButton>
            </Tooltip>
          </Col>
        </Row>

        <Row justify={"space-between"}>
          <Col>
            <UserInfo userId={item.userId} /> {getTimeDisplay(item.createdAt)}
          </Col>
        </Row>
      </div>
      <Divider />
    </div>
  );
};

const DatasetSqlHistory = ({ datasetId, branch }: IProps) => {
  const [history, setHistory] = useState<IPreviewDatasetModel[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isError, setIsError] = useState<string | false>(false);

  const getDatasetHistory = (datasetId: string, branch: string) => {
    setIsLoading(true);
    setIsError(false);
    previewDatasetHistoryAPI(datasetId)
      .then(({ data }) => {
        setHistory(data);
      })
      .catch((error) => {
        setIsError(error.message);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    getDatasetHistory(datasetId, branch);
  }, []);

  if (isLoading) {
    return <BoslerLoader />;
  }

  if (isError) {
    return (
      <NoData heading={"Error"} subHeading={isError} icon={<WarningState />} />
    );
  }

  if (!history || history.length == 0) {
    return (
      <NoData
        heading={"No history!"}
        subHeading={"Run some query to generate history"}
        icon={<SearchEmptyState />}
      />
    );
  }
  return (
    <div>
      {history.map((item) => (
        <HistoryItem item={item} />
      ))}
    </div>
  );
};

export default DatasetSqlHistory;
