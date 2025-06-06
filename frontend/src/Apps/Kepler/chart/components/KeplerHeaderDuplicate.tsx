import { Col, Popover, Row, Typography } from "antd";
import { CopyCellIcon } from "assets/icons/boslerTableIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { duplicateChartHandler } from "../charts.utils";

interface TProps {
  id: string;
  chart: any;
  query: any;
}

const KeplerHeaderDuplicate = ({ id, chart, query }: TProps) => {
  return (
    <Popover
      content={
        <>
          <div style={{ alignItems: "center", display: "flex", gap: "10px" }}>
            <BoslerButton
              minimal
              icon={<CopyCellIcon />}
              intent="none"
              onClick={() => duplicateChartHandler(chart, query)}
            >
              {getLanguageLabel("duplicate")}
            </BoslerButton>
            <div className="text-and-icon-center">D</div>
          </div>
          <div
            style={{ alignItems: "center", display: "flex", marginTop: "5px" }}
          >
            <Typography.Text type="secondary">
              <div
                style={{
                  wordWrap: "break-word",
                  fontSize: "10px",
                }}
              >
                {getLanguageLabel("clickToDuplicateCharts")}
              </div>
            </Typography.Text>
          </div>
        </>
      }
      placement="bottomRight"
    >
      <BoslerButton
        minimal
        icon={<CopyCellIcon />}
        intent="none"
        icononly
        onClick={() => duplicateChartHandler(chart, query)}
      />
    </Popover>
  );
};

export default KeplerHeaderDuplicate;
