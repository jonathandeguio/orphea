import { TTransaction } from "Apps/Dataset/Dataset.contants";
import { Card, Typography } from "antd";
import React from "react";
import { formatTime, getLanguageLabel } from "utils/utilities";
const { Text } = Typography;
interface TProps {
  transactions: TTransaction[];
}

interface TContentCard {
  title: string;
  content: any;
  color: string;
}
const ContentCard = ({ title, content, color }: TContentCard) => {
  return (
    <Card
      style={{
        borderTop: `3px solid ${color}`,
        flex: 1,
      }}
      title={null}
      //   title={<Text color={color}>{title}</Text>}
    >
      <Text
        style={{
          color: "var(--movetodata-font-color-muted)",
        }}
      >
        {title}
      </Text>
      <br />
      {content}
    </Card>
  );
};

const DatasetHistoryStats = ({ transactions }: TProps) => {
  const statusCount: Record<string, number> = {
    SUCCESS: 0,
    ACTIVE: 0,
    ERROR: 0,
    ABORTED: 0,
    CANCELLED: 0,
    FAILED: 0,
    DELETED: 0,
  };

  let averageTime = 0;
  transactions.forEach((item: { buildStatus: string | number }) => {
    statusCount[item.buildStatus]++;
  });

  let totalTime = 0;
  let count = 0;

  transactions.forEach((item: TTransaction) => {
    if (item.createdAt && item.finishedAt) {
      totalTime += item.finishedAt - item.createdAt;
      count++;
    }
  });

  averageTime = count > 0 ? totalTime / count : 0;

  return (
    <div
      style={{
        display: "flex",
        gap: "5px",
      }}
    >
      <ContentCard
        title={getLanguageLabel("success")}
        content={statusCount.SUCCESS}
        color={"var(--SUCCESS_COLOR)"}
      />
      <ContentCard
        title={getLanguageLabel("failed")}
        content={statusCount.FAILED}
        color={"var(--DANGEROUS_COLOR)"}
      />
      <ContentCard
        title={getLanguageLabel("error")}
        content={statusCount.ERROR}
        color={"var(--WARNING_COLOR)"}
      />
      <ContentCard
        title={"Time"}
        content={"~" + formatTime(averageTime)}
        color={"var(--movetodata-border-color-default)"}
      />
    </div>
  );
};

export default DatasetHistoryStats;
