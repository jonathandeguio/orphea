import { Form, Row, Select, Typography } from "antd";
import { CrossIcon } from "assets/icons/boslerActionIcons";
import { FilterLinesIcon } from "assets/icons/boslerTableIcons";
import BoslerHeader from "components/CommonUI/Header/BoslerHeader";
import React from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { capitalizeFirstLetter, getLanguageLabel } from "utils/utilities";
import BoslerButton from "../ButtonComponent/BoslerButton";
import {
  TYPES_FILTER_PANEL,
  getTypeBasedDefaultFilters,
} from "./FilterPanel.utils";

const { Text, Title } = Typography;
const { Item } = Form;
const { Option } = Select;
interface Props {
  children: React.ReactNode;
  setFilters: (value: any) => void;
  type: TYPES_FILTER_PANEL;
}

export const FilterPanel: React.FC<Props> = ({
  children,
  setFilters,
  type,
}) => {
  const { user } = useSelector((state: RootState) => state.userDetails);
  return (
    <div className="--flex-col-center">
      <div style={{ height: "100%", width: "100%", overflow: "hidden" }}>
        <BoslerHeader
          heading={
            <Text style={{ fontSize: "20px" }}>
              {capitalizeFirstLetter(getLanguageLabel("filters"))}
            </Text>
          }
          icon={<FilterLinesIcon size={"22px"} />}
          description={getLanguageLabel("filterByThisValue")}
          borderBottom
          muted
        />
        <div
          style={{
            height: "calc(100% - 60px)",
            padding: "20px",
            overflow: "auto",
          }}
        >
          {children}
        </div>
      </div>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          position: "sticky",
          bottom: "0",
          borderTop: "1px solid var(--bosler-border-color-default)",
          padding: "1px",
          backgroundColor: "var(--bosler-bkg-color-muted)",
          width: "100%",
        }}
      >
        <Row justify={"center"}>
          <BoslerButton
            intent={"none"}
            icon={<CrossIcon />}
            minimal
            onClick={() =>
              setFilters(getTypeBasedDefaultFilters(type, user.id))
            }
          >
            {getLanguageLabel("resetFilters")}
          </BoslerButton>
        </Row>
      </div>
    </div>
  );
};
