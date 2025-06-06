import { Typography } from "antd";
import { AppIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerHeader from "components/CommonUI/Header/BoslerHeader";
import BoslerLoader from "components/boslerLoader";
import React from "react";
import { useSelector } from "react-redux";
import { useParams } from "react-router";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { EDIT_MODE } from "../../../../redux/constants/resourcePermissionConstants";
import "./DashboardAddChartMenu.scss";
import DashboardAddChartMenuChartsTab from "./DashboardAddChartMenuChartsTab";
const { Text } = Typography;

const DashboardAddChartMenu = () => {
  const { id } = useParams();
  if (!isDefined(id)) {
    throw new Error("Id is undefined, unable to load chart!");
  }

  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[id]
  );

  if (!resourcePermission) return <BoslerLoader />;
  const editable = resourcePermission.mode == EDIT_MODE;
  if (!editable) return null;

  return (
    <div className="kepler-container-plane-rightWrapper-addchart">
      <BoslerHeader
        icon={<AppIcon />}
        heading={getLanguageLabel("dashboardElements")}
        description={getLanguageLabel("dragAndDropToDashboard")}
        borderBottom
      />

      <DashboardAddChartMenuChartsTab />
    </div>
  );
};

export default DashboardAddChartMenu;
